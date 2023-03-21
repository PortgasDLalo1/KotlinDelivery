package com.eduardo.kotlinudemydelivery.activities.restaurant.orders.detail

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.eduardo.kotlinudemydelivery.Providers.OrdersProvider
import com.eduardo.kotlinudemydelivery.Providers.UsersProvider
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.activities.restaurant.home.RestaurantHomeActivity
import com.eduardo.kotlinudemydelivery.adapters.OrderProductsAdapter
import com.eduardo.kotlinudemydelivery.databinding.ActivityClientOrdersDetailBinding
import com.eduardo.kotlinudemydelivery.databinding.ActivityRestaurantOrdersDetailBinding
import com.eduardo.kotlinudemydelivery.models.Category
import com.eduardo.kotlinudemydelivery.models.Order
import com.eduardo.kotlinudemydelivery.models.ResponseHttp
import com.eduardo.kotlinudemydelivery.models.User
import com.eduardo.kotlinudemydelivery.utils.SharedPref
import com.eduardo.kotlinudemydelivery.utils.printTicket
import com.google.gson.Gson
import com.mazenrashed.printooth.Printooth
import com.mazenrashed.printooth.data.printable.Printable
import com.mazenrashed.printooth.data.printable.TextPrintable
import com.mazenrashed.printooth.data.printer.DefaultPrinter
import com.mazenrashed.printooth.ui.ScanningActivity
import com.mazenrashed.printooth.utilities.Printing
import com.mazenrashed.printooth.utilities.PrintingCallback
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RestaurantOrdersDetailActivity : AppCompatActivity(), PrintingCallback {
    private lateinit var binding: ActivityRestaurantOrdersDetailBinding
    var order: Order? = null
    val gson = Gson()
    val TAG = "ROrdersDetail"

    var adapter: OrderProductsAdapter? = null

    var toolbar: Toolbar? = null
    var user: User? = null
    var sharedPref: SharedPref? = null

    var usersProvider: UsersProvider? = null
    var ordersProvider: OrdersProvider? = null

    var idDelivery = ""

    private var printing: Printing? = null;
    var printables = ArrayList<Printable>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRestaurantOrdersDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPref = SharedPref(this)

        order = gson.fromJson(intent.getStringExtra("order"), Order::class.java)
        getUserFromSession()
        usersProvider = UsersProvider(user?.sessionToken!!)
        ordersProvider = OrdersProvider(user?.sessionToken!!)
        toolbar = findViewById(R.id.toolbar)
        toolbar?.setTitleTextColor(ContextCompat.getColor(this,R.color.black))
        toolbar?.title = "Order #${order?.id}"
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.textviewClient.text = "${order?.client?.name} ${order?.client?.lastname}"
        binding.textviewAddress.text = order?.address?.address
        binding.textviewDate.text = "${order?.timestamp}"
        binding.textviewStatus.text = order?.status

        binding.textviewDeliveryO.text = "${order?.delivery?.name} ${order?.delivery?.lastname}"

        binding.recyclerviewOrderDetail.layoutManager = LinearLayoutManager(this)

        adapter = OrderProductsAdapter(this, order?.products!!)
        binding.recyclerviewOrderDetail.adapter = adapter

        Log.d(TAG,"odern: ${order.toString()}")

        getTotal()
        getDelivery()
        if (order?.status == "PAGADO"){
            binding.btnAddDelivery.visibility = View.VISIBLE
            binding.textviewDelivery.visibility = View.VISIBLE
            binding.spinnerDelivery.visibility = View.VISIBLE
            binding.btnPrint.visibility = View.VISIBLE
        }else{
            binding.textviewDo.visibility = View.VISIBLE
            binding.textviewDeliveryO.visibility = View.VISIBLE
        }

        binding.btnAddDelivery.setOnClickListener { updateOrder() }
        Printooth.init(this)
        checarPrint()
        binding.btnPrint.setOnClickListener {
            printText(order!!)
        }
    }

    private fun updateOrder(){
        order?.id_delivery = idDelivery
        ordersProvider?.updateToDispatched(order!!)?.enqueue(object : Callback<ResponseHttp>{
            override fun onResponse(call: Call<ResponseHttp>, response: Response<ResponseHttp>) {
                if (response.body() != null){
                    if (response.body()?.isSuccess == true){
                        Toast.makeText(this@RestaurantOrdersDetailActivity, "Repartidor asignado correctamente", Toast.LENGTH_SHORT).show()
                        goToOrders()
                    }else{
                        Toast.makeText(this@RestaurantOrdersDetailActivity, "No se pudo asignar repartidor", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Toast.makeText(this@RestaurantOrdersDetailActivity, "No hubo respuesta del servidor", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(call: Call<ResponseHttp>, t: Throwable) {
                Toast.makeText(this@RestaurantOrdersDetailActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun goToOrders(){
        val i = Intent(this, RestaurantHomeActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(i)
    }

    private fun getDelivery(){
        usersProvider?.getDelivery()?.enqueue(object: Callback<ArrayList<User>>{
            override fun onResponse(
                call: Call<ArrayList<User>>,
                response: Response<ArrayList<User>>
            ) {
                if (response.body() != null){
                    val delivery = response.body()
                    val arrayAdapter = ArrayAdapter<User>(this@RestaurantOrdersDetailActivity, android.R.layout.simple_dropdown_item_1line, delivery!!)
                    binding.spinnerDelivery.adapter = arrayAdapter
                    binding.spinnerDelivery.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
                        override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, position: Int, l: Long) {
                            idDelivery = delivery[position].id!! // seleccionando del spinner el id del delivery
                            Log.d(TAG,"id delivery: $idDelivery")
                        }

                        override fun onNothingSelected(p0: AdapterView<*>?) {

                        }
                    }
                }
            }

            override fun onFailure(call: Call<ArrayList<User>>, t: Throwable) {
                Toast.makeText(this@RestaurantOrdersDetailActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun getTotal(){
        var total = 0.0
        for (p in order?.products!!){
            total = total + (p.price * p.quantity!!)
        }

        binding.textviewTotal.text = "$ ${total}"
    }

    private fun getUserFromSession(){
        val gson = Gson()
        if (!sharedPref?.getData("user").isNullOrBlank()){
            //si el usuario exite en sesion
            user = gson.fromJson(sharedPref?.getData("user"), User::class.java)
        }
    }

    private fun checarPrint(){

        if (printing != null){
            printing!!.printingCallback = this
        }

        if (Printooth.hasPairedPrinter()){
            Toast.makeText(this, "Printer is enable2",Toast.LENGTH_SHORT).show()
        }else{
            Printooth.setPrinter("MP210","DC:0D:51:58:BF:CB")
            Toast.makeText(this, "Printer is enable",Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ScanningActivity.SCANNING_FOR_PRINTER && resultCode == Activity.RESULT_OK) {
            initPrinting()
        }

    }

    private fun initPrinting() {
        if (Printooth.hasPairedPrinter()){
            printing = Printooth.printer()
        }
        if (printing != null){
            printing!!.printingCallback = this
        }
    }

    override fun connectingWithPrinter() {
        Toast.makeText(this,"Conectandose a la impresora",Toast.LENGTH_SHORT).show()
    }

    override fun connectionFailed(error: String) {
        Toast.makeText(this,"Fallido $error",Toast.LENGTH_SHORT).show()
    }

    override fun disconnected() {
        Toast.makeText(this,"Impresora Desconectada",Toast.LENGTH_SHORT).show()
    }

    override fun onError(error: String) {
        Toast.makeText(this,"Error $error",Toast.LENGTH_SHORT).show()
    }

    override fun onMessage(message: String) {
        Toast.makeText(this,"Mensaje $message",Toast.LENGTH_SHORT).show()
    }

    override fun printingOrderSentSuccessfully() {
        Toast.makeText(this,"Ticket Enviado a la impresora",Toast.LENGTH_SHORT).show()
    }

    fun printText(order: Order) {
        var printables = ArrayList<Printable>()

        val str = StringBuilder()
        val str2 = StringBuilder()
        var subTotal = 0.0
        var totalPriceProduct: Double = 0.0
        var orderN = TextPrintable.Builder()
            .setText(
                "N. Orden: # ${order.id}"
            )
            .setFontSize(DefaultPrinter.FONT_SIZE_NORMAL)
            .setLineSpacing(DefaultPrinter.LINE_SPACING_30)
            .setUnderlined(DefaultPrinter.UNDERLINED_MODE_OFF)
            .setCharacterCode(DefaultPrinter.CHARCODE_PC852)
            .setNewLinesAfter(1)
            .build()
        printables.add(orderN)

        var cliente = TextPrintable.Builder()
            .setText(
                "Cliente:\n  ${order.client?.name} ${order.client?.lastname}"
            )
            .setFontSize(DefaultPrinter.FONT_SIZE_NORMAL)
            .setLineSpacing(DefaultPrinter.LINE_SPACING_30)
            .setUnderlined(DefaultPrinter.UNDERLINED_MODE_OFF)
            .setCharacterCode(DefaultPrinter.CHARCODE_PC852)
            .setNewLinesAfter(1)
            .build()
        printables.add(cliente)

        var entregarEn = TextPrintable.Builder()
            .setText(
                "Entregar en:\n  ${order.address?.address}"
            )
            .setFontSize(DefaultPrinter.FONT_SIZE_NORMAL)
            .setLineSpacing(DefaultPrinter.LINE_SPACING_30)
            .setUnderlined(DefaultPrinter.UNDERLINED_MODE_OFF)
            .setCharacterCode(DefaultPrinter.CHARCODE_PC852)
            .setNewLinesAfter(1)
            .build()
        printables.add(entregarEn)

        var fechapedido = TextPrintable.Builder()
            .setText(
                "Estado del pedido: ${order.status}"
            )
            .setFontSize(DefaultPrinter.FONT_SIZE_NORMAL)
            .setLineSpacing(DefaultPrinter.LINE_SPACING_30)
            .setUnderlined(DefaultPrinter.UNDERLINED_MODE_OFF)
            .setCharacterCode(DefaultPrinter.CHARCODE_PC852)
            .setNewLinesAfter(2)
            .build()
        printables.add(fechapedido)

        var encabezado = TextPrintable.Builder()
            .setText(
                "Productos        Cant      Total\n--------------------------------"
            )
            .setFontSize(DefaultPrinter.FONT_SIZE_NORMAL)
            .setLineSpacing(DefaultPrinter.LINE_SPACING_30)
            .setUnderlined(DefaultPrinter.UNDERLINED_MODE_OFF)
            .setCharacterCode(DefaultPrinter.CHARCODE_PC852)
            .setNewLinesAfter(1)
            .build()
        printables.add(encabezado)

        for (p in order?.products!!){
//            Toast.makeText(this, p.name, Toast.LENGTH_SHORT).show()
//            str.append("${p.name}")
//            str.append(rightSpace(32,"${p.name}"))
//            str.append(p.quantity!!)
            str.append(rightSpace(18,"${p.quantity!!}",))
            str.append("${p.quantity!!}")
            totalPriceProduct = p.quantity!! * p.price
            str.append(rightSpace(12,"$ $totalPriceProduct",))
            str.append("$ $totalPriceProduct")

            var productos = TextPrintable.Builder()
                .setText(
                    "- ${p.name}\n$str"
                )
                .setFontSize(DefaultPrinter.FONT_SIZE_NORMAL)
                .setLineSpacing(DefaultPrinter.LINE_SPACING_30)
                .setCharacterCode(DefaultPrinter.CHARCODE_PC852)
                .setNewLinesAfter(1)
                .build()

            printables.add(productos)
            str.setLength(0)

            subTotal += totalPriceProduct
        }

        str2.append("--------------------------------")
        str2.append(rightSpaceWithoutPoint(31,"Total: $ $subTotal"))
        var total = TextPrintable.Builder()
            .setText(
                str2.toString()
            )
            .setFontSize(DefaultPrinter.FONT_SIZE_NORMAL)
            .setLineSpacing(DefaultPrinter.LINE_SPACING_30)
            .setCharacterCode(DefaultPrinter.CHARCODE_PC852)
            .setNewLinesAfter(2)
            .build()
        printables.add(total)



        Printooth.printer().print(printables)
    }


    private fun rightSpace(max: Int, name: String):  StringBuilder{
        val strS = StringBuilder()
        val totalS = name.length
        for (i in 0 .. max - name.length ){
            strS.append(".")
        }
        return strS
    }

    private fun rightSpaceWithoutPoint(max: Int, name: String):  StringBuilder{
        val strS = StringBuilder()
        val totalS = name.length
        for (i in 0 .. max - name.length ){
            strS.append(" ")
        }
        strS.append(name)
        return strS
    }

    private fun rightSpace2(max: Int, precio: String, cantidad: Int):  StringBuilder{
        var c: Int = 0
        c = if (cantidad.toString().length == 1)
            precio.length-3
        else
            precio.length-2

        val strS = StringBuilder()
        for (i in 0 .. max -c){
            strS.append(".")
        }
        return strS
    }
}