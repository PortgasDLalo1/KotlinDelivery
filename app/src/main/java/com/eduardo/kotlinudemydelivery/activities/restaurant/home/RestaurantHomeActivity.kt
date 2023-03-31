package com.eduardo.kotlinudemydelivery.activities.restaurant.home

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.eduardo.kotlinudemydelivery.Providers.OrdersProvider
import com.eduardo.kotlinudemydelivery.Providers.UsersProvider
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.adapters.OrdersRestaurantAdapter
import com.eduardo.kotlinudemydelivery.databinding.ActivityRestaurantHomeBinding
import com.eduardo.kotlinudemydelivery.fragments.client.ClientCategoriesFragment
import com.eduardo.kotlinudemydelivery.fragments.client.ClientOrdersFragment
import com.eduardo.kotlinudemydelivery.fragments.client.ClientProfileFragment
import com.eduardo.kotlinudemydelivery.fragments.restaurant.*
import com.eduardo.kotlinudemydelivery.models.Order
import com.eduardo.kotlinudemydelivery.models.SocketEmitPagado
import com.eduardo.kotlinudemydelivery.models.User
import com.eduardo.kotlinudemydelivery.utils.SharedPref
import com.eduardo.kotlinudemydelivery.utils.SocketPaymentHandler
import com.eduardo.kotlinudemydelivery.utils.printTicket
//import com.github.nkzawa.socketio.client.Socket
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.mazenrashed.printooth.Printooth
import com.mazenrashed.printooth.data.printable.Printable
import com.mazenrashed.printooth.data.printable.TextPrintable
import com.mazenrashed.printooth.data.printer.DefaultPrinter
import com.mazenrashed.printooth.ui.ScanningActivity
import com.mazenrashed.printooth.utilities.Printing
import com.mazenrashed.printooth.utilities.PrintingCallback
import io.socket.client.Socket
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RestaurantHomeActivity : AppCompatActivity(), PrintingCallback {

    private lateinit var binding: ActivityRestaurantHomeBinding
    private val TAG = "RestaurantHomeActivity"
    var sharedPref: SharedPref? = null

    var bottomNavigation: BottomNavigationView? = null

    var usersProvider: UsersProvider? = null
    var ordersProvider: OrdersProvider? = null
    var user: User? = null
    var mSocket: Socket? = null
    var gson = Gson()

    private var printing: Printing? = null;
    var printables = ArrayList<Printable>()
    var orders1 : Order?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRestaurantHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPref = SharedPref(this)

        openFragment(RestaurantOrdersFragment())

        bottomNavigation = findViewById(R.id.bottom_navigation)
        bottomNavigation?.setOnItemSelectedListener {
            when(it.itemId){

                R.id.item_home -> {
                    openFragment(RestaurantOrdersFragment())
                    true
                }

                R.id.item_category -> {
                    openFragment(RestaurantCategoryFragment())
                    true
                }

                R.id.item_product -> {
                    openFragment(RestaurantProductFragment())
                    true
                }

                R.id.item_profile -> {
                    openFragment(ClientProfileFragment())
                    true
                }

                R.id.item_build -> {
                    openFragment(RestaurantConfigFragment())
                    true
                }

                else -> false

            }
        }
        getUserFromSession()
        usersProvider = UsersProvider(token = user?.sessionToken!!)
        ordersProvider = OrdersProvider(user?.sessionToken!!)
        createToken()
        //binding.btnLogout.setOnClickListener { logout() }
        checaBluetooth()
        Printooth.init(this)
        checarPrint()
//        getOneOrder("29")
        connectSocket()
    }

    private fun connectSocket(){
        SocketPaymentHandler.setSocket()
//        socket = SocketPaymentHandler.getSocket()
//        socket?.connect()
        SocketPaymentHandler.establishConnection()
        mSocket = SocketPaymentHandler.getSocket()

        mSocket?.on("pagado/1"){args ->
            if (args[0] != null){
                runOnUiThread {
                    val data = gson.fromJson(args[0].toString(), SocketEmitPagado::class.java)
//                    Toast.makeText(this, "Id_Order: ${data.id_order}", Toast.LENGTH_SHORT).show()
                    getOneOrder(data.id_order)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mSocket?.disconnect()
    }
    private fun getOneOrder(idOrder: String){
        ordersProvider?.getOrdersByIdOrder(idOrder)?.enqueue(object :
            Callback<ArrayList<Order>> {
            override fun onResponse(
                call: Call<ArrayList<Order>>,
                response: Response<ArrayList<Order>>
            ) {
                if (response.body() != null){
                    val order = response.body()
//                    Log.d(TAG, "Response: ${response.body()}")
                   /* Log.d(TAG, "id_ORDER: ${order?.get(0)?.id}")
                    Log.d(TAG, "Estatus: ${order?.get(0)?.status}")
                    Log.d(TAG, "Productos: ${order?.get(0)?.products}")
                    Log.d(TAG, "Cliente: ${order?.get(0)?.client}")
                    Log.d(TAG, "address: ${order?.get(0)?.address?.address} Colonia: ${order?.get(0)?.address?.neighborhood}")
                    Toast.makeText(this@RestaurantHomeActivity, "Response: $order", Toast.LENGTH_LONG).show()*/

                    //getOrders()
                    val order2 = order?.get(0)
                    printText(order2!!)
//                    val fragment = Fragment()
//                    (fragment as RestaurantOrdersStatusFragment).getOrders()
                }
            }

            override fun onFailure(call: Call<ArrayList<Order>>, t: Throwable) {
                Toast.makeText(this@RestaurantHomeActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                Log.d(TAG, "Error: ${t.message}")
            }

        })
    }


    private fun createToken(){
        usersProvider?.createToken(user!!, this)
    }

    private fun openFragment(fragment: Fragment){

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()

    }

    private fun logout(){
        sharedPref?.remove("user")
        finish()
    }

    private fun getUserFromSession(){
        val gson = Gson()
        if (!sharedPref?.getData("user").isNullOrBlank()){
            //si el usuario exite en sesion
            user = gson.fromJson(sharedPref?.getData("user"),User::class.java)
            Log.e(TAG, "Usuario: $user")
        }
    }

    private fun checaBluetooth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT))
        }
        else{
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetooth.launch(enableBtIntent)
        }
    }

    private var requestBluetooth = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            //granted
            Toast.makeText(this, "Bluetooth is enable", Toast.LENGTH_SHORT).show()
        }else{
            //deny
            Toast.makeText(this, "Bluetooth is enable", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d("test006", "${it.key} = ${it.value}")
            }
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
            .setNewLinesAfter(3)
            .build()
        printables.add(total)



        Printooth.printer().print(printables)
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