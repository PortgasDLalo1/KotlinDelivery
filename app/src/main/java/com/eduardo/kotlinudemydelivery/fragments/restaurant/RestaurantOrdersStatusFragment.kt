package com.eduardo.kotlinudemydelivery.fragments.restaurant

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eduardo.kotlinudemydelivery.Providers.OrdersProvider
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.activities.restaurant.home.RestaurantHomeActivity
import com.eduardo.kotlinudemydelivery.adapters.OrdersClientAdapter
import com.eduardo.kotlinudemydelivery.adapters.OrdersRestaurantAdapter
import com.eduardo.kotlinudemydelivery.models.Order
import com.eduardo.kotlinudemydelivery.models.SocketEmit
import com.eduardo.kotlinudemydelivery.models.SocketEmitPagado
import com.eduardo.kotlinudemydelivery.models.Sucursales
import com.eduardo.kotlinudemydelivery.models.User
import com.eduardo.kotlinudemydelivery.utils.SharedPref
import com.eduardo.kotlinudemydelivery.utils.SocketPaymentHandler
//import com.github.nkzawa.socketio.client.Socket
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.mazenrashed.printooth.data.printable.Printable
import com.mazenrashed.printooth.utilities.Printing
import com.mazenrashed.printooth.utilities.PrintingCallback
import io.socket.client.Socket
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class RestaurantOrdersStatusFragment : Fragment() {
    var myView: View? = null
    var ordersProvider: OrdersProvider? = null
    var user: User? = null
    var sucursal: Sucursales? = null
    var sharedPref: SharedPref? = null
    var recyclerViewOrders: RecyclerView? = null
    var adapter: OrdersRestaurantAdapter? = null
    var fabReaload: FloatingActionButton? = null
    var status = ""

    var mSocket: Socket? = null
    var gson = Gson()

    val TAG = "RestaurantO"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        myView = inflater.inflate(R.layout.fragment_restaurant_orders_status, container, false)

        sharedPref = SharedPref(requireActivity())
        status = arguments?.getString("status")!!

        getUserFromSession()
        getSucursalFromSession()
        ordersProvider = OrdersProvider(user?.sessionToken!!)
        recyclerViewOrders = myView?.findViewById(R.id.recyclerview_orders)
        fabReaload = myView?.findViewById(R.id.fab_reload)
        recyclerViewOrders?.layoutManager = LinearLayoutManager(requireContext())

        getOrders()
//        getOneOrder("13")
        connectSocket()
//        SocketPaymentHandler.setSocket()
        fabReaload?.setOnClickListener { getOrders() }
//        SocketPaymentHandler.setSocket()
//        SocketPaymentHandler.establishConnection()
//        mSocket = SocketPaymentHandler.getSocket()
//        Log.d(TAG,"chee2   ${sucursal?.id}")
//        mSocket?.on("pagado/${sucursal?.id.toString()}"){args ->
//            if (args[0] != null){
//                activity?.runOnUiThread {
//                    val data = gson.fromJson(args[0].toString(), SocketEmitPagado::class.java)
//                    Log.d(TAG,"chee3   ${data}")
//                    Toast.makeText(context, "Id_Order: ${data.id_order}", Toast.LENGTH_SHORT).show()
//                    getOrders()
//                }
//            }
//        }
        return myView
    }

    fun getOrders(){
        ordersProvider?.getOrdersByStatus(status,sucursal?.id!!)?.enqueue(object :
            Callback<ArrayList<Order>> {
            override fun onResponse(
                call: Call<ArrayList<Order>>,
                response: Response<ArrayList<Order>>
            ) {
                if (response.body() != null){
                    val orders = response.body()
                    adapter = OrdersRestaurantAdapter(requireActivity(),orders!!)
                    recyclerViewOrders?.adapter = adapter
                }
            }

            override fun onFailure(call: Call<ArrayList<Order>>, t: Throwable) {
                Toast.makeText(requireActivity(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun connectSocket(){
        SocketPaymentHandler.setSocket()
        SocketPaymentHandler.establishConnection()
        mSocket = SocketPaymentHandler.getSocket()
//        mSocket?.connect()

        mSocket?.on("pagado/${sucursal?.id.toString()}"){args ->
            if (args[0] != null){
                activity?.runOnUiThread {
                    val data = gson.fromJson(args[0].toString(), SocketEmitPagado::class.java)
                    Toast.makeText(context, "Id_Order: ${data.id_order}", Toast.LENGTH_SHORT).show()
//                    getOneOrder(data.id_order)
                    (activity as RestaurantHomeActivity).getOneOrder(data.id_order)
                    getOrders()
                }
            }
        }
    }

    private fun getOneOrder1(idOrder: String){
        ordersProvider?.getOrdersByIdOrder(idOrder)?.enqueue(object :
            Callback<ArrayList<Order>>{
            override fun onResponse(
                call: Call<ArrayList<Order>>,
                response: Response<ArrayList<Order>>
            ) {
                if (response.body() != null){
                    val order = response.body()
//                    Log.d(TAG, "Response: ${response.body()}")
                    Log.d(TAG, "id_ORDER: ${order?.get(0)?.id}")
                    Log.d(TAG, "Estatus: ${order?.get(0)?.status}")
                    Log.d(TAG, "Productos: ${order?.get(0)?.products}")
                    Log.d(TAG, "Cliente: ${order?.get(0)?.client}")
                    Log.d(TAG, "address: ${order?.get(0)?.address?.address} Colonia: ${order?.get(0)?.address?.neighborhood}")
                    Toast.makeText(requireActivity(), "Response: $order", Toast.LENGTH_LONG).show()

                    //getOrders()
                }
            }

            override fun onFailure(call: Call<ArrayList<Order>>, t: Throwable) {
                Toast.makeText(requireActivity(), "Error: ${t.message}", Toast.LENGTH_LONG).show()
                Log.d(TAG, "Error: ${t.message}")
            }

        })
    }

    private fun getUserFromSession(){
        val gson = Gson()
        if (!sharedPref?.getData("user").isNullOrBlank()){
            //si el usuario exite en sesion
            user = gson.fromJson(sharedPref?.getData("user"), User::class.java)
        }
    }

    private fun getSucursalFromSession(){
        val gson = Gson()
        if (!sharedPref?.getData("sucursal").isNullOrBlank()){
            sucursal = gson.fromJson(sharedPref?.getData("sucursal"),Sucursales::class.java)
        }
    }

}