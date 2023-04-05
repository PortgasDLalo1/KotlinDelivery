package com.eduardo.kotlinudemydelivery.activities.client.orders.checkout

import android.content.Intent
import android.content.res.Configuration
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.eduardo.kotlinudemydelivery.Providers.SucursalesProvider
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.activities.client.address.list.ClientAddressListActivity
import com.eduardo.kotlinudemydelivery.activities.client.card.list.ClientCardListActivity
import com.eduardo.kotlinudemydelivery.adapters.CheckOutPAdapter
import com.eduardo.kotlinudemydelivery.adapters.ShoppingBagAdapter
import com.eduardo.kotlinudemydelivery.databinding.ActivityClientOrderCheckOutBinding
import com.eduardo.kotlinudemydelivery.models.*
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
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Integer.min

class ClientOrderCheckOutActivity : AppCompatActivity(), OnMapReadyCallback,DirectionUtil.DirectionCallBack, Listener{
    private lateinit var binding: ActivityClientOrderCheckOutBinding
    var googleMap: GoogleMap? = null
    var toolbar: Toolbar? = null
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
    var selectedProducts = ArrayList<Product>()
    var adapterProduct: CheckOutPAdapter? = null
    var cardSession: Cards? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientOrderCheckOutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPref = SharedPref(this)
        getUserFromSession()
        sucursalesProvider = SucursalesProvider(user?.sessionToken!!)
//        getAddressFromSession()
//        getSucursales()
        binding.recyclerviewCheck.layoutManager = LinearLayoutManager(this)
//        getProductsFromSharedPref()
//        getCardsFromSharedPref()
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_check) as? SupportMapFragment
        mapFragment?.getMapAsync(this)


        val locationRequest = LocationRequest.create().apply {
            interval = 0
            fastestInterval = 0
            priority = Priority.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 1f
        }
        easyWayLocation = EasyWayLocation(this,locationRequest,false,false,this)

        toolbar = findViewById(R.id.toolbar)
        toolbar?.setTitleTextColor(ContextCompat.getColor(this,R.color.black))
        toolbar?.title = "Pedido"
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.layoutAddressCheck.setOnClickListener { goToAddressList() }

        binding.layoutCardCheck.setOnClickListener {
            binding.edittextCvvCheck?.setText("")
            goToCardList()
        }


    }

    private fun goToCardList(){
        val i = Intent(this,ClientCardListActivity::class.java)
        startActivity(i)
    }

    private fun goToAddressList(){
        val i = Intent(this,ClientAddressListActivity::class.java)
        startActivity(i)
    }


    override fun onResume() {
        super.onResume()
        getProductsFromSharedPref()
        getAddressFromSession()
        getCardsFromSharedPref()
        getSucursales()

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
                            neighborhood = s.neighborhood,
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
        var neigh = ""
        for (i in 0 until sucuDistance.size){
            if (sucuDistance[i].distance!! < min){
                min = sucuDistance[i].distance!!
                id = sucuDistance[i].id!!
                latLng = sucuDistance[i].latlng!!
                neigh = sucuDistance[i].neighborhood!!
            }
        }
        sucursalesDistance?.clear()
        val better = SucursalesDistance(
            id = id,
            distance = min,
            neighborhood = neigh,
            latlng = latLng
        )
        sucursalesDistance?.add(better)
        Log.d(TAG, "sucursalesDistance: "+sucursalesDistance)
//        getAddressFromSession()
        drawOnMap(sucursalesDistance?.get(0)?.latlng!!, sessionLocation!!)
        binding.textViewNameRestaurant.text = "31 Sushi & Bar(${sucursalesDistance?.get(0)!!.neighborhood})"
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

    private fun drawOnMap(sd: LatLng, sa: LatLng){

        //Log.d(TAG,"llego aqui"+sd)
        googleMap?.clear()
        addMyMarker(sa)
        addSucursalMarker(sd)

        var builder = LatLngBounds.Builder()
        builder.include(markerAddress?.position!!)
        builder.include(markerSucursal?.position!!)

        var bounds: LatLngBounds? = null
        var width = 0
        var height = 0
        var padding = 0
        /*if(resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT){
            bounds = builder.build()
            width = resources.displayMetrics.widthPixels
            height = resources.displayMetrics.heightPixels
            padding = (width * 0.40).toInt()
            var ca = CameraUpdateFactory.newLatLngBounds(bounds,width,height,padding)
            googleMap?.animateCamera(ca)
        }else{
            bounds = builder.build()
            width = resources.displayMetrics.widthPixels
            height = resources.displayMetrics.heightPixels
            padding = (width * 0.40).toInt()
            var ca = CameraUpdateFactory.newLatLngBounds(bounds,width,height,padding)
            googleMap?.animateCamera(ca)
        }*/

        bounds = builder.build()
        width = resources.displayMetrics.widthPixels
        height = resources.displayMetrics.heightPixels
        val minMetric = min(width,height)
        padding = minMetric.div(4)
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
        wayPoints.clear()
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
                .title("31 Sushi & Bar\n ${sucursalesDistance?.get(0)?.neighborhood}")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.restaurant))
        )
    }


    override fun locationOn() {

    }

    override fun currentLocation(location: Location?) {
    }

    override fun locationCancelled() {
    }

    fun setTotalCheck(total: Double){
        binding.textviewTotalPriceCheck.text = "$ ${total} MX"
    }
    private fun getUserFromSession(){
        val gson = Gson()
        if (!sharedPref?.getData("user").isNullOrBlank()){
            //si el usuario exite en sesion
            user = gson.fromJson(sharedPref?.getData("user"), User::class.java)
            binding.textViewNameClientCheck.text = "${user?.name} ${user?.lastname}"
            binding.textViewPhoneClientCheck.text = "${user?.phone}"
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

            binding.textViewAddressCheck.text = "${address?.address}, ${address?.neighborhood}"
        }
    }

    private fun getProductsFromSharedPref(){
        if (!sharedPref?.getData("order").isNullOrBlank()){ // si existe una orden en sharedpref
            val type = object: TypeToken<ArrayList<Product>>() {}.type
            selectedProducts = gson.fromJson(sharedPref?.getData("order"), type)
            adapterProduct = CheckOutPAdapter(this, selectedProducts)
            binding.recyclerviewCheck.adapter = adapterProduct
        }
    }

    private fun getCardsFromSharedPref(){
        if(!sharedPref?.getData("card").isNullOrBlank()){
            cardSession =  gson.fromJson(sharedPref?.getData("card"), Cards::class.java)
            if(cardSession?.number_card == "Efectivo"){
                binding.textviewCard.text = cardSession?.number_card
                binding.imageViewIconCard.setImageResource(R.drawable.efectivo)
                binding.cardviewCvv?.visibility = View.GONE
            }else {
                val ultimo4 = cardSession?.number_card?.substring(15)
                val primerDigito = cardSession?.number_card?.substring(0, 1)
                binding.textviewCard.text = "**** $ultimo4"
                if (primerDigito == "5") {
                    binding.imageViewIconCard.setImageResource(R.drawable.mastercard)
                } else if (primerDigito == "4") {
                    binding.imageViewIconCard.setImageResource(R.drawable.visa)
                } else if (primerDigito == "4") {
                    binding.imageViewIconCard.setImageResource(R.drawable.american_express)
                } else {
                    binding.imageViewIconCard.setImageResource(R.drawable.tarjeta)
                }
                binding.cardviewCvv?.visibility = View.VISIBLE
            }

        }
    }
}