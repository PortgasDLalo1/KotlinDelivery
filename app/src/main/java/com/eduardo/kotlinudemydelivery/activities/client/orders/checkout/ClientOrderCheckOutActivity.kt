package com.eduardo.kotlinudemydelivery.activities.client.orders.checkout

import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.eduardo.kotlinudemydelivery.Providers.SucursalesProvider
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.databinding.ActivityClientOrderCheckOutBinding
import com.eduardo.kotlinudemydelivery.models.Address
import com.eduardo.kotlinudemydelivery.models.Sucursales
import com.eduardo.kotlinudemydelivery.models.SucursalesDistance
import com.eduardo.kotlinudemydelivery.models.User
import com.eduardo.kotlinudemydelivery.utils.SharedPref
import com.example.easywaylocation.EasyWayLocation
import com.example.easywaylocation.Listener
import com.example.easywaylocation.draw_path.DirectionUtil
import com.example.easywaylocation.draw_path.PolyLineDataBean
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClientOrderCheckOutActivity : AppCompatActivity(), OnMapReadyCallback,DirectionUtil.DirectionCallBack, Listener{
    private lateinit var binding: ActivityClientOrderCheckOutBinding
    var googleMap: GoogleMap? = null

    var wayPoints: ArrayList<LatLng> = ArrayList()
    val WAY_POINT_TAG = "way_point_tag"
    private lateinit var directionUtil: DirectionUtil
    var gson = Gson()
    var sharedPref: SharedPref? = null
    var user: User? = null
    private var easyWayLocation: EasyWayLocation? = null
    var markerSucursal: Marker? = null
    var markerAddress: Marker? = null
    val TAG = "ClienteCard"
    var sucursalesProvider: SucursalesProvider? = null
    var address: Address? = null
    var sessionLocation: LatLng? = null
    var sucursalesDistance: ArrayList<SucursalesDistance>? = ArrayList()
    var sucursales1: ArrayList<Sucursales>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientOrderCheckOutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPref = SharedPref(this)
        getUserFromSession()
        sucursalesProvider = SucursalesProvider(user?.sessionToken!!)
        getAddressFromSession()
        getSucursales()
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_check) as? SupportMapFragment
        mapFragment?.getMapAsync(this)


        val locationRequest = LocationRequest.create().apply {
            interval = 0
            fastestInterval = 0
            priority = Priority.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 1f
        }
        easyWayLocation = EasyWayLocation(this,locationRequest,false,false,this)



    }

    private fun getSucursales(){
        sucursalesProvider?.getSucursalAll()?.enqueue(object: Callback<ArrayList<Sucursales>>{
            override fun onResponse(
                call: Call<ArrayList<Sucursales>>,
                response: Response<ArrayList<Sucursales>>
            ) {
                if (response.body() != null){
                    sucursales1 = response.body()
//                    Log.d(TAG,sucursales1.toString())
                    for (s in sucursales1!!){
                        val sucLatLng = LatLng(s.lat,s.lng)
                        val distance = getDistanceBetween(sucLatLng,sessionLocation!!)
//                        Log.d(TAG,distance.toString())
                        val sucu = SucursalesDistance(
                            id = s.id,
                            distance = distance,
                            latlng = sucLatLng
                        )
                        sucursalesDistance?.add(sucu)
                    }
                    Log.d(TAG,"sucursales consulta"+sucursalesDistance.toString())
                    getBetterDistanceSucursal(sucursalesDistance!!)
                }
            }

            override fun onFailure(call: Call<ArrayList<Sucursales>>, t: Throwable) {
                Toast.makeText(this@ClientOrderCheckOutActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun getBetterDistanceSucursal(sucuDistance: ArrayList<SucursalesDistance>){
        var max: Float = 0.0F
        for (i in 0 until sucuDistance.size){
            if (sucuDistance[i].distance!! > max){
                max = sucuDistance[i].distance!!
            }
        }

        var min = max
        var id = ""
        var latLng: LatLng? = null
        for (i in 0 until sucuDistance.size){
            if (sucuDistance[i].distance!! < min){
                min = sucuDistance[i].distance!!
                id = sucuDistance[i].id!!
                latLng = sucuDistance[i].latlng!!
            }
        }
        sucursalesDistance?.clear()
        val better = SucursalesDistance(
            id = id,
            distance = min,
            latlng = latLng
        )
        sucursalesDistance?.add(better)
        Log.d(TAG, "sucursalesDistance: "+sucursalesDistance)
        drawOnMap(sucursalesDistance?.get(0)?.latlng!!)
    }

    private fun getDistanceBetween(fromLatLng: LatLng, toLatLng: LatLng): Float{

        var distance = 0.0f
        val from = Location("")
        val to = Location("")

        from.latitude = fromLatLng.latitude
        from.longitude = fromLatLng.longitude
        to.latitude = toLatLng.latitude
        to.longitude = toLatLng.longitude

        distance = from.distanceTo(to)

        return distance
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isZoomControlsEnabled = true

    }

    private fun drawOnMap(sd: LatLng){

        //Log.d(TAG,"llego aqui"+sd)

        addMyMarker(sessionLocation!!)
        addSucursalMarker(sd)

        var builder = LatLngBounds.Builder()
        builder.include(markerAddress?.position!!)
        builder.include(markerSucursal?.position!!)

        var bounds = builder.build()
        var width = resources.displayMetrics.widthPixels
        var height = resources.displayMetrics.heightPixels
        var padding = (width * 0.30).toInt()
        var ca = CameraUpdateFactory.newLatLngBounds(bounds,width,height,padding)
        googleMap?.animateCamera(ca)
//        googleMap?.moveCamera(
//            CameraUpdateFactory.newCameraPosition(
//                CameraPosition.builder().target(sessionLocation!!).zoom(13f).build()
//            )
//        )
        easyDrawRoute()
    }

    override fun onDestroy() {
        super.onDestroy()
        easyWayLocation?.endUpdates()
    }

    private fun easyDrawRoute(){
        wayPoints.add(sucursalesDistance?.get(0)?.latlng!!)
        wayPoints.add(sessionLocation!!)
        directionUtil = DirectionUtil.Builder()
            .setDirectionKey(resources.getString(R.string.google_map_api_key))
            .setOrigin(sucursalesDistance?.get(0)?.latlng!!)
            .setWayPoints(wayPoints)
            .setGoogleMap(googleMap!!)
            .setPolyLinePrimaryColor(R.color.black)
            .setPolyLineWidth(10)
            .setPathAnimation(true)
            .setCallback(this)
            .setDestination(sessionLocation!!)
            .build()

        directionUtil.initPath()
    }

    override fun pathFindFinish(
        polyLineDetailsMap: HashMap<String, PolyLineDataBean>,
        polyLineDetailsArray: ArrayList<PolyLineDataBean>
    ) {
        directionUtil.drawPath(WAY_POINT_TAG)

    }

    private fun addMyMarker(myLocation: LatLng){
        val myMarker = LatLng(myLocation.latitude,myLocation.longitude)

        markerAddress = googleMap?.addMarker(
            MarkerOptions()
                .position(myMarker)
                .title("Mi posicion")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.house))
        )
    }

    private fun addSucursalMarker(Location: LatLng){
        val myMarker = LatLng(Location.latitude,Location.longitude)

        markerSucursal = googleMap?.addMarker(
            MarkerOptions()
                .position(myMarker)
                .title("Sushi & Bar 31")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.restaurant))
        )
    }


    override fun locationOn() {

    }

    override fun currentLocation(location: Location?) {
    }

    override fun locationCancelled() {
    }
    private fun getUserFromSession(){
        val gson = Gson()
        if (!sharedPref?.getData("user").isNullOrBlank()){
            //si el usuario exite en sesion
            user = gson.fromJson(sharedPref?.getData("user"), User::class.java)
        }
    }
    private fun getAddressFromSession(){
        if (!sharedPref?.getData("address").isNullOrBlank()){
            address = gson.fromJson(sharedPref?.getData("address"), Address::class.java) // si existe una direccion
            sessionLocation = LatLng(address?.lat!!,address?.lng!!)
            Log.d(TAG,"getAddressFromSession $sessionLocation")
            if (sessionLocation!= null){
                Log.d(TAG, "SESSION"+sessionLocation.toString())
            }
        }
    }
}