package com.eduardo.kotlinudemydelivery.activities.client.orders.checkout

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.databinding.ActivityClientOrderCheckOutBinding
import com.eduardo.kotlinudemydelivery.models.User
import com.eduardo.kotlinudemydelivery.utils.SharedPref
import com.example.easywaylocation.EasyWayLocation
import com.example.easywaylocation.Listener
import com.example.easywaylocation.draw_path.DirectionUtil
import com.example.easywaylocation.draw_path.PolyLineDataBean
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson

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
    val PERMISSION_ID = 42
    var fusedLocationClient: FusedLocationProviderClient? = null
    var markerSucursal: Marker? = null
    var markerAddress: Marker? = null
    var myLocationLatLng: LatLng? = null
    val TAG = "ClienteCard"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientOrderCheckOutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPref = SharedPref(this)
        getUserFromSession()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_check) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLastLocation()

        val locationRequest = LocationRequest.create().apply {
            interval = 0
            fastestInterval = 0
            priority = Priority.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 1f
        }
        easyWayLocation = EasyWayLocation(this,locationRequest,false,false,this)
    }

    private fun getUserFromSession(){
        val gson = Gson()
        if (!sharedPref?.getData("user").isNullOrBlank()){
            //si el usuario exite en sesion
            user = gson.fromJson(sharedPref?.getData("user"), User::class.java)
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isZoomControlsEnabled = true
    }

    override fun pathFindFinish(
        polyLineDetailsMap: HashMap<String, PolyLineDataBean>,
        polyLineDetailsArray: ArrayList<PolyLineDataBean>
    ) {

    }

    private fun addMyMarker(myLocation: LatLng){
        val myMarker = LatLng(myLocation.latitude,myLocation.longitude)

        markerAddress = googleMap?.addMarker(
            MarkerOptions()
                .position(myMarker)
                .title("Mi posicion")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.home))
        )
    }
    @SuppressLint("MissingPermission")
    private fun getLastLocation(){
        Log.d(TAG,"LOCATION 1")
        if (checkPermissions()){
            Log.d(TAG,"LOCATION 2")
            if (isLocationEnable()){
                Log.d(TAG,"LOCATION 3")
                fusedLocationClient?.lastLocation?.addOnCompleteListener { task ->

                    var location = task.result
                    Log.d(TAG,"LOCATION $location")
                    if (location != null){
                        myLocationLatLng = LatLng(location.latitude,location.longitude)
                        Log.d(TAG,"LOCATION $myLocationLatLng")
                        addMyMarker(myLocationLatLng!!)
                        if (myLocationLatLng != null){
                            googleMap?.moveCamera(
                                CameraUpdateFactory.newCameraPosition(
                                    CameraPosition.builder().target(
                                        myLocationLatLng!!
                                    ).zoom(15f).build()
                                )
                            )
                        }
                    }
                }
            }else {
                Toast.makeText(this, "Habilita la localizacion", Toast.LENGTH_LONG).show()
                val i = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(i)
            }
        } else{
            requestPermissions()
        }
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
    }

    override fun locationOn() {

    }

    override fun currentLocation(location: Location?) {
    }

    override fun locationCancelled() {
    }
}