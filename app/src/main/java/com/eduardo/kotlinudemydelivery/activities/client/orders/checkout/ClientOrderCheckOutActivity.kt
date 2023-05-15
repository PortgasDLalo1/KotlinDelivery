package com.eduardo.kotlinudemydelivery.activities.client.orders.checkout

import android.content.Intent
import android.content.res.Configuration
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.eduardo.kotlinudemydelivery.Providers.MercadoPagoProvider
import com.eduardo.kotlinudemydelivery.Providers.OrdersProvider
import com.eduardo.kotlinudemydelivery.Providers.PaymentsProvider
import com.eduardo.kotlinudemydelivery.Providers.SucursalesProvider
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.activities.client.address.list.ClientAddressListActivity
import com.eduardo.kotlinudemydelivery.activities.client.card.list.ClientCardListActivity
import com.eduardo.kotlinudemydelivery.activities.client.payments.mercado_pago.installments.ClientPaymentsInstallmentsActivity
import com.eduardo.kotlinudemydelivery.activities.client.payments.mercado_pago.status.ClientPaymentsStatusActivity
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
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.tommasoberlose.progressdialog.ProgressDialogFragment
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

    var cardNumberG = ""
    var cardHolder = ""
    var cardExpiration = ""
    var cardCvv = ""
    var mercadoPagoProvider: MercadoPagoProvider = MercadoPagoProvider()
    var paymentsProvider: PaymentsProvider? = null

    var edittextCvvCheck: EditText ?=null
    var paymentMethodId = ""
    var paymentTypeId = ""
    var issuerId = ""
    var cardToken = ""
    var firstSixDigits = ""
    var total = 0.0
    var installmentsSelected = ""
    var idRestaurant = ""
    var orderProvider: OrdersProvider? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientOrderCheckOutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPref = SharedPref(this)
        getUserFromSession()
        sucursalesProvider = SucursalesProvider(user?.sessionToken!!)
        paymentsProvider = PaymentsProvider(user?.sessionToken!!)
        orderProvider = OrdersProvider(user?.sessionToken!!)
        binding.recyclerviewCheck.layoutManager = LinearLayoutManager(this)
        edittextCvvCheck = findViewById(R.id.edittext_cvv_check)
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
            edittextCvvCheck?.setText("")
            goToCardList()
        }

        validCvvInput()
        binding.cardviewInstallment.visibility = View.GONE

        binding.btnPay?.setOnClickListener {
            if(binding.textviewCard.text == "Efectivo"){
                Toast.makeText(this, "EFECTIVO", Toast.LENGTH_SHORT).show()
                createPaymentEfectivo()
            }else{
                createPayment() 
            }
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
        Log.d(TAG,"entra aqui")
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
                        Log.d(TAG,"sesion location"+sessionLocation.toString())
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
        drawOnMap(sucursalesDistance?.get(0)?.latlng!!, sessionLocation!!)

        binding.textViewNameRestaurant.text = "31 Sushi & Bar(${sucursalesDistance?.get(0)!!.neighborhood})"
        sucuDistance.clear()
        idRestaurant = better.id!!
        sucursalesDistance?.clear()
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

        var width = 0
        var height = 0
        var padding = 0

        val bounds = builder.build()
        width = resources.displayMetrics.widthPixels
        height = resources.displayMetrics.heightPixels
        val minMetric = min(width,height)
        padding = minMetric.div(10)
        var ca = CameraUpdateFactory.newLatLngBounds(bounds,padding)
        googleMap?.animateCamera(ca)
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

    fun setTotalCheck(totalA: Double){
        total = totalA
        binding.textviewTotalPriceCheck.text = "$ ${totalA} MX"
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

                cardNumberG = cardSession?.number_card!!
                cardExpiration = cardSession?.expiration!!
                cardHolder = cardSession?.name_client!!
            }

        }else{
            binding.textviewCard.text = "Efectivo"
            binding.imageViewIconCard.setImageResource(R.drawable.efectivo)
            binding.cardviewCvv?.visibility = View.GONE

            val efectivo = Cards(
                number_card = "Efectivo"
            )

            sharedPref?.save("card",efectivo)
        }
    }

    private fun createCardToken(){

        val expiration = cardExpiration.split("/").toTypedArray()
        val month = expiration[0]
        val year = "20${expiration[1]}"
        val ch = Cardholder(name = cardHolder)

        cardNumberG = cardNumberG.replace(" ","")

        val mercadoPagoCardTokenBody = MercadoPagoCardTokenBody(
            securityCode = edittextCvvCheck?.text.toString(),
            expirationYear = year,
            expirationMonth = month.toInt(),
            cardNumber = cardNumberG,
            cardHolder = ch
        )

        Log.d(TAG, "$mercadoPagoCardTokenBody")

        mercadoPagoProvider.createCardToken(mercadoPagoCardTokenBody)?.enqueue(object : Callback<JsonObject>{
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
               if (response.body()!=null){
                   cardToken = response.body()?.get("id")?.asString!!
                   firstSixDigits = response.body()?.get("first_six_digits")?.asString!!
                  // goToInstallments(cardToken!!,firstSixDigits!!)
                   getInstallments()
                   Log.d(TAG,"Cardtoken: $cardToken,  First: $firstSixDigits")
               }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                Toast.makeText(this@ClientOrderCheckOutActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }

        })
    }

    private fun validCvvInput(){
        edittextCvvCheck?.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
               if (s?.length == 3){
                   Log.d(TAG, "son 3")
                   createCardToken()
               }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
    }

    private fun getInstallments(){
        mercadoPagoProvider.getInstallments(firstSixDigits,"$total")?.enqueue(object : Callback<JsonArray>{
            override fun onResponse(call: Call<JsonArray>, response: Response<JsonArray>) {
                if (response.body() != null){
                    val jsonInstallments = response.body()!!.get(0).asJsonObject.get("payer_costs").asJsonArray

                    val type = object: TypeToken<ArrayList<MercadoPagoInstallments>>(){}.type
                    val installments = gson.fromJson<ArrayList<MercadoPagoInstallments>>(jsonInstallments, type)

                    paymentMethodId = response.body()?.get(0)?.asJsonObject?.get("payment_method_id")?.asString!!
                    paymentTypeId = response.body()?.get(0)?.asJsonObject?.get("payment_type_id")?.asString!!
                    issuerId = response.body()?.get(0)?.asJsonObject?.get("issuer")?.asJsonObject?.get("id")?.asString!!

                    Log.d(TAG, "response: $response")
                    Log.d(TAG, "installments: $installments")
                    Log.d(TAG, "paymentMethodId: $paymentMethodId")
                    Log.d(TAG, "paymentTypeId: $paymentTypeId")

                    if (paymentTypeId=="credit_card"){
                        binding.cardviewInstallment.visibility = View.VISIBLE
                        Log.d(TAG, "entrar")
                    }else{
                        binding.cardviewInstallment.visibility = View.GONE
                    }
                    val arrayAdapter = ArrayAdapter<MercadoPagoInstallments>(this@ClientOrderCheckOutActivity, android.R.layout.simple_dropdown_item_1line, installments)
                    binding.spinnerInstallmentsCheck?.adapter = arrayAdapter
                    binding.spinnerInstallmentsCheck?.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
                        override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, position: Int, l: Long) {
                            installmentsSelected = "${installments[position].installments}"
                            Log.d(TAG,"Coutas seleccionadas: $installmentsSelected")
                            binding.textviewDescriptionCheck.text = installments[position].recommendedMessage
                        }

                        override fun onNothingSelected(p0: AdapterView<*>?) {

                        }
                    }
                }
            }

            override fun onFailure(call: Call<JsonArray>, t: Throwable) {

            }

        })
    }

    private fun createPaymentEfectivo(){
        val order = Order(
            products = selectedProducts,
            id_client = user?.id!!,
            id_address = address?.id!!,
            id_restaurant =  idRestaurant,
            installments_type = "Efectivo"
        )

        paymentMethodId = "Efectivo"
        ProgressDialogFragment.showProgressBar(this)

        orderProvider?.create(order)?.enqueue(object : Callback<ResponseHttp>{
            override fun onResponse(call: Call<ResponseHttp>, response: Response<ResponseHttp>) {
                ProgressDialogFragment.hideProgressBar(this@ClientOrderCheckOutActivity)
                if (response.body()!=null){
                    if (response.body()?.isSuccess == true){
                        if (response.body()?.isSuccess== true){
                            sharedPref?.remove("order")
                        }
                        Toast.makeText(this@ClientOrderCheckOutActivity, response.body()?.message, Toast.LENGTH_LONG)
                            .show()
                        val idOrder = response.body()?.data?.get("id")?.asString
                        goToPaymentsStatus(paymentMethodId,"approved","",idOrder!!)
                    }
                }else{
                    goToPaymentsStatus(paymentMethodId,"denied","","")
                    Toast.makeText(this@ClientOrderCheckOutActivity, "No hubo una respuesta exitosa", Toast.LENGTH_LONG)
                        .show()
                }
            }

            override fun onFailure(call: Call<ResponseHttp>, t: Throwable) {
                ProgressDialogFragment.hideProgressBar(this@ClientOrderCheckOutActivity)
                Toast.makeText(this@ClientOrderCheckOutActivity, "Error ${t.message}", Toast.LENGTH_LONG)
                    .show()
            }

        })
    }

    private fun createPayment(){
        Log.d(TAG,"Create $paymentTypeId")
        val order = Order(
            products = selectedProducts,
            id_client = user?.id!!,
            id_address = address?.id!!,
            id_restaurant =  idRestaurant,
            installments_type = paymentTypeId.toString()!!
        )

        val payer = Payer(
            email = user?.email!!
        )

        val mercadoPagoPayment = MercadoPagoPayment(
            order = order,
            token = cardToken,
            description = "Kotlin Delivery",
            paymentMethodId = paymentMethodId,
            paymentTypeId = paymentTypeId,
            issuerId = issuerId,
            payer = payer,
            transactionAmount = total,
            installments = installmentsSelected.toInt()
        )

        ProgressDialogFragment.showProgressBar(this)

        paymentsProvider?.create(mercadoPagoPayment)?.enqueue(object : Callback<ResponseHttp>{
            override fun onResponse(call: Call<ResponseHttp>, response: Response<ResponseHttp>) {
                ProgressDialogFragment.hideProgressBar(this@ClientOrderCheckOutActivity)
                if (response.body()!=null){
                    if (response.body()?.isSuccess== true){
                        sharedPref?.remove("order")
                    }
                    Toast.makeText(this@ClientOrderCheckOutActivity, response.body()?.message, Toast.LENGTH_LONG)
                        .show()

                    val status = response.body()?.data?.get("dataPayment")?.asJsonObject?.get("status")?.asString
                    //Log.d(TAG, "Response body: ${response.body()?.data?.get("id_order")?.asString}")
//                    val lastFour = response.body()?.data?.get("card")?.asJsonObject?.get("last_four_digits")?.asString
                    val lastFour = response.body()?.data?.get("dataPayment")?.asJsonObject?.get("card")?.asJsonObject?.get("last_four_digits")?.asString
                    val idOrder = response.body()?.data?.get("id_order")?.asString
                    //Log.d(TAG, "Response body: ${response.body()?.data?.get("id_order")?.asString} status: $status lastfour: $lastFour")
                    goToPaymentsStatus(paymentMethodId,status!!,lastFour!!,idOrder!!)

                }else{
                    goToPaymentsStatus(paymentMethodId,"denied","","")
                    Toast.makeText(this@ClientOrderCheckOutActivity, "No hubo una respuesta exitosa", Toast.LENGTH_LONG)
                        .show()
                }
            }

            override fun onFailure(call: Call<ResponseHttp>, t: Throwable) {
                ProgressDialogFragment.hideProgressBar(this@ClientOrderCheckOutActivity)
                Toast.makeText(this@ClientOrderCheckOutActivity, "Error ${t.message}", Toast.LENGTH_LONG)
                    .show()
            }

        })

    }

    private fun goToPaymentsStatus(paymentMethodId: String, paymentStatus: String, lastFourDigits: String, id_order: String){
        val i = Intent(this, ClientPaymentsStatusActivity::class.java)
        i.putExtra("paymentMethodId", paymentMethodId)
        i.putExtra("paymentStatus", paymentStatus)
        i.putExtra("lastFourDigits", lastFourDigits)
        i.putExtra("idOrder", id_order)
        i.putExtra("idRestaurant", idRestaurant )
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
    }
}