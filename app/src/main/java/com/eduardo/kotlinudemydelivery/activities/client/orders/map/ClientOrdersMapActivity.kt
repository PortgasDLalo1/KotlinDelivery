package com.eduardo.kotlinudemydelivery.activities.client.orders.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.eduardo.kotlinudemydelivery.Providers.OrdersProvider
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.activities.delivery.home.DeliveryHomeActivity
import com.eduardo.kotlinudemydelivery.databinding.ActivityClientOrdersMapBinding
import com.eduardo.kotlinudemydelivery.databinding.ActivityDeliveryOrdersMapBinding
import com.eduardo.kotlinudemydelivery.models.Order
import com.eduardo.kotlinudemydelivery.models.ResponseHttp
import com.eduardo.kotlinudemydelivery.models.SocketEmit
import com.eduardo.kotlinudemydelivery.models.User
import com.eduardo.kotlinudemydelivery.utils.SharedPref
import com.eduardo.kotlinudemydelivery.utils.SocketHandler
import com.example.easywaylocation.EasyWayLocation
import com.example.easywaylocation.Listener
import com.example.easywaylocation.draw_path.DirectionUtil
import com.example.easywaylocation.draw_path.PolyLineDataBean
import com.github.nkzawa.socketio.client.Socket
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.maps.route.extensions.drawRouteOnMap
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.ln

class ClientOrdersMapActivity : AppCompatActivity(), OnMapReadyCallback, Listener, DirectionUtil.DirectionCallBack {
    private lateinit var binding: ActivityClientOrdersMapBinding
    var googleMap: GoogleMap? = null
    val PERMISSION_ID = 42
    var fusedLocationClient: FusedLocationProviderClient? = null
    var city = ""
    var country = ""
    var address = ""
    var addressLatLng: LatLng? = null
    val TAG = "DeliveryOrdersMap"

    var markerDelivery: Marker? = null
    var markerAddress: Marker? = null
    var myLocationLatLng: LatLng? = null
    var deliveryLatLng: LatLng? = null

    var order: Order? = null
    var gson = Gson()

    var wayPoints: ArrayList<LatLng> = ArrayList()
    val WAY_POINT_TAG = "way_point_tag"
    private lateinit var directionUtil: DirectionUtil

    val REQUEST_PHONE_CALL = 18

    var ordersProvider: OrdersProvider? = null
    var user: User? = null
    var sharedPref: SharedPref? = null

    var socket: Socket? = null

    private var easyWayLocation: EasyWayLocation? = null

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            //obtenemos la localizacion en tiempo real
            var lastLocation = locationResult.lastLocation
            myLocationLatLng = LatLng(lastLocation?.latitude!!,lastLocation.longitude)

            /*googleMap?.moveCamera(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.builder().target(
                        LatLng(myLocationLatLng?.latitude!!, myLocationLatLng?.longitude!!)
                    ).zoom(15f).build()
                )
            )*/

//            distanceBetween = getDistanceBetween(myLocationLatLng!!, addressLatLng!!)

//            removeDeliveryMarker()
//            addDeliveryMarker()
            Log.d("LOCALIZACION", "Callback: $lastLocation")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientOrdersMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPref = SharedPref(this)
        getUserFromSession()
        ordersProvider = OrdersProvider(user?.sessionToken!!)
        order = gson.fromJson(intent.getStringExtra("order"), Order::class.java)
        addressLatLng = LatLng(order?.address?.lat!!,order?.address?.lng!!)
        if (order?.lat != null && order?.lng != null){
            deliveryLatLng = LatLng(order?.lat!!, order?.lng!!)
        }
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        getLastLocation()
        connectSocket()

        binding.textviewClient2.text = "${order?.delivery?.name} ${order?.delivery?.lastname}"
        binding.textviewDireccion.text = order?.address?.address
        binding.textviewNb.text = order?.address?.neighborhood

        if (!order?.delivery?.image.isNullOrBlank()) {
            Glide.with(this).load(order?.delivery?.image).into(binding.circleImageUser2)
        }

        binding.imageviewPhone.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CALL_PHONE),REQUEST_PHONE_CALL)
            }else{
                call()
            }
        }

        val locationRequest = LocationRequest.create().apply {
            interval = 0
            fastestInterval = 0
            priority = Priority.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 1f
        }
        easyWayLocation = EasyWayLocation(this,locationRequest,false,false,this)
    }

    private fun connectSocket(){
        SocketHandler.setSocket()
        socket = SocketHandler.getSocket()
        socket?.connect()

        socket?.on("position/${order?.id}"){args ->
            if (args[0] != null){
                runOnUiThread{
                    val data = gson.fromJson(args[0].toString(), SocketEmit::class.java)
                    removeDeliveryMarker()
                    addDeliveryMarker(data.lat,data.lng)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (locationCallback != null && fusedLocationClient != null){
            fusedLocationClient?.removeLocationUpdates(locationCallback)
        }

        socket?.disconnect()
    }

    private fun goToHome(){
        val i = Intent(this, DeliveryHomeActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK //eliminar historial de pantallas
        startActivity(i)
    }

    private fun call(){
        val i = Intent(Intent.ACTION_CALL)
        i.data = Uri.parse("tel:${order?.delivery?.phone}")
        Log.d("PHONEEES","tel:${order?.delivery?.phone}")
        if(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "Permiso denegado para realizar la llamada", Toast.LENGTH_SHORT).show()
            return
        }
        startActivity(i)
    }

    private fun drawRoute(){
        if (deliveryLatLng != null) {
            val addressLocation = LatLng(order?.address?.lat!!, order?.address?.lng!!)

            googleMap?.drawRouteOnMap(
                getString(R.string.google_map_api_key),
                source = deliveryLatLng!!,
                destination = addressLocation,
                context = this,
                color = Color.BLACK,
                polygonWidth = 10,
                boundMarkers = false,
                markers = false
            )
        }
    }

    private fun easyDrawRoute(myPosition2: LatLng){
        val addressLocation = LatLng(order?.address?.lat!!,order?.address?.lng!!)
        wayPoints.clear()
        wayPoints.add(myPosition2)
        wayPoints.add(addressLocation)
        directionUtil = DirectionUtil.Builder()
            .setDirectionKey(resources.getString(R.string.google_map_api_key))
            .setOrigin(myLocationLatLng!!)
            .setWayPoints(wayPoints)
            .setGoogleMap(googleMap!!)
            .setPolyLinePrimaryColor(R.color.black)
            .setPolyLineWidth(10)
            .setPathAnimation(false)
            .setCallback(this)
            .setDestination(addressLocation)
            .build()

        directionUtil!!.initPath()
    }

    private fun removeDeliveryMarker(){
        markerDelivery?.remove()
    }

    private fun addDeliveryMarker(lat: Double, lng: Double){

        val location = LatLng(lat, lng)

        markerDelivery = googleMap?.addMarker(
            MarkerOptions()
                .position(location)
                .title("Posición del repartidor")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.delivery))
        )
    }

    private fun addAddressMarker(){

        val addressLocation = LatLng(order?.address?.lat!!,order?.address?.lng!!)

        markerAddress = googleMap?.addMarker(
            MarkerOptions()
                .position(addressLocation)
                .title("Entregar aqui")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.home))
        )
    }

    private fun goToCreateAddress() {
        val i = Intent()
        i.putExtra("city", city)
        i.putExtra("address", address)
        i.putExtra("country", country)
        i.putExtra("lat", addressLatLng?.latitude)
        i.putExtra("lng", addressLatLng?.longitude)
        setResult(RESULT_OK, i)
        finish() // volver hacia atras
    }


    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isZoomControlsEnabled = true
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnable()) {


                fusedLocationClient?.lastLocation?.addOnCompleteListener { task ->

                    //obtiene la localizacion 1 sola vez
                    var location = task.result

                    if (location != null){
                        myLocationLatLng = LatLng(location.latitude, location.longitude)

                        removeDeliveryMarker()
                        addDeliveryMarker(deliveryLatLng?.latitude!!, deliveryLatLng?.longitude!!)
                        addAddressMarker()
//                        drawRoute()
                        easyDrawRoute(myLocationLatLng!!)
                        if (deliveryLatLng != null) {
                            googleMap?.moveCamera(
                                CameraUpdateFactory.newCameraPosition(
                                    CameraPosition.builder().target(
                                        LatLng(deliveryLatLng?.latitude!!, deliveryLatLng?.longitude!!)
                                    ).zoom(15f).build()
                                )
                            )
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Habilita la localizacion", Toast.LENGTH_LONG).show()
                val i = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(i)
            }
        } else {
            requestPermissions()
        }
    }

    private fun requestNewLocationData() {
        val locationRequest = LocationRequest.create().apply {
            interval = 100
            fastestInterval = 50
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient?.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper()) // inicializa la posicion en tiempo real
    }

    private fun isLocationEnable(): Boolean {
        var locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }

        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_ID
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_ID) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation()
            }
        }

        if (requestCode == REQUEST_PHONE_CALL){
            call()
        }
    }

    private fun getUserFromSession(){
        val gson = Gson()
        if (!sharedPref?.getData("user").isNullOrBlank()){
            //si el usuario exite en sesion
            user = gson.fromJson(sharedPref?.getData("user"), User::class.java)
        }
    }

    override fun locationOn() {

    }

    override fun currentLocation(location: Location?) {
    }

    override fun locationCancelled() {
    }

    override fun pathFindFinish(
        polyLineDetailsMap: HashMap<String, PolyLineDataBean>,
        polyLineDetailsArray: ArrayList<PolyLineDataBean>
    ) {
        directionUtil?.drawPath(WAY_POINT_TAG)
    }
}