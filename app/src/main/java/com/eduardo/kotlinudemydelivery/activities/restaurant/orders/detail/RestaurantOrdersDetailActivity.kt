package com.eduardo.kotlinudemydelivery.activities.restaurant.orders.detail

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
import com.eduardo.kotlinudemydelivery.activities.restaurant.home.RestaurantHomeActivity
import com.eduardo.kotlinudemydelivery.adapters.OrderProductsAdapter
import com.eduardo.kotlinudemydelivery.databinding.ActivityClientOrdersDetailBinding
import com.eduardo.kotlinudemydelivery.databinding.ActivityRestaurantOrdersDetailBinding
import com.eduardo.kotlinudemydelivery.models.Category
import com.eduardo.kotlinudemydelivery.models.Order
import com.eduardo.kotlinudemydelivery.models.ResponseHttp
import com.eduardo.kotlinudemydelivery.models.User
import com.eduardo.kotlinudemydelivery.utils.SharedPref
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RestaurantOrdersDetailActivity : AppCompatActivity() {
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
        }else{
            binding.textviewDo.visibility = View.VISIBLE
            binding.textviewDeliveryO.visibility = View.VISIBLE
        }

        binding.btnAddDelivery.setOnClickListener { updateOrder() }
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
}