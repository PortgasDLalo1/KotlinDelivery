package com.eduardo.kotlinudemydelivery.activities.delivery.orders.detail

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.eduardo.kotlinudemydelivery.Providers.OrdersProvider
import com.eduardo.kotlinudemydelivery.Providers.UsersProvider
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.activities.delivery.home.DeliveryHomeActivity
import com.eduardo.kotlinudemydelivery.activities.delivery.orders.map.DeliveryOrdersMapActivity
import com.eduardo.kotlinudemydelivery.activities.restaurant.home.RestaurantHomeActivity
import com.eduardo.kotlinudemydelivery.adapters.OrderProductsAdapter
import com.eduardo.kotlinudemydelivery.databinding.ActivityDeliveryOrdersDetailBinding
import com.eduardo.kotlinudemydelivery.databinding.ActivityRestaurantOrdersDetailBinding
import com.eduardo.kotlinudemydelivery.models.Order
import com.eduardo.kotlinudemydelivery.models.ResponseHttp
import com.eduardo.kotlinudemydelivery.models.User
import com.eduardo.kotlinudemydelivery.utils.SharedPref
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DeliveryOrdersDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDeliveryOrdersDetailBinding
    var order: Order? = null
    val gson = Gson()
    val TAG = "DeliveryOrders"

    var adapter: OrderProductsAdapter? = null

    var toolbar: Toolbar? = null
    var user: User? = null
    var sharedPref: SharedPref? = null

    var usersProvider: UsersProvider? = null
    var ordersProvider: OrdersProvider? = null

    var idDelivery = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeliveryOrdersDetailBinding.inflate(layoutInflater)
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


        binding.recyclerviewOrderDetail.layoutManager = LinearLayoutManager(this)

        adapter = OrderProductsAdapter(this, order?.products!!)
        binding.recyclerviewOrderDetail.adapter = adapter

        Log.d(TAG,"odern: ${order.toString()}")

        getTotal()
        if (order?.status == "DESPACHADO"){
            binding.btnAddDelivery.visibility = View.VISIBLE
        }

        if (order?.status == "EN CAMINO"){
            binding.btnGoToMap.visibility = View.VISIBLE
        }

        binding.btnAddDelivery.setOnClickListener { updateOrder() }
        binding.btnGoToMap.setOnClickListener { goToMap() }
    }

    private fun updateOrder(){
        ordersProvider?.updateToOnTheWay(order!!)?.enqueue(object : Callback<ResponseHttp> {
            override fun onResponse(call: Call<ResponseHttp>, response: Response<ResponseHttp>) {
                if (response.body() != null){
                    if (response.body()?.isSuccess == true){
                        Toast.makeText(this@DeliveryOrdersDetailActivity, "Entrega iniciada", Toast.LENGTH_LONG).show()
                        goToMap()
                    }else{
                        Toast.makeText(this@DeliveryOrdersDetailActivity, "No se pudo asignar repartidor", Toast.LENGTH_LONG).show()
                    }
                }else{
                    Toast.makeText(this@DeliveryOrdersDetailActivity, "No hubo respuesta del servidor", Toast.LENGTH_LONG)
                        .show()
                }
            }

            override fun onFailure(call: Call<ResponseHttp>, t: Throwable) {
                Toast.makeText(this@DeliveryOrdersDetailActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }

        })
    }

    private fun goToMap(){
        val i = Intent(this, DeliveryOrdersMapActivity::class.java)
        i.putExtra("order",order?.toJson())
        startActivity(i)
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
}