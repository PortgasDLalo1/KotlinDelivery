package com.eduardo.kotlinudemydelivery.activities.client.orders.detail

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.activities.client.orders.map.ClientOrdersMapActivity
import com.eduardo.kotlinudemydelivery.activities.delivery.orders.map.DeliveryOrdersMapActivity
import com.eduardo.kotlinudemydelivery.adapters.OrderProductsAdapter
import com.eduardo.kotlinudemydelivery.databinding.ActivityClientOrdersDetailBinding
import com.eduardo.kotlinudemydelivery.databinding.ActivityClientProductsDetailBinding
import com.eduardo.kotlinudemydelivery.models.Order
import com.google.gson.Gson

class ClientOrdersDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClientOrdersDetailBinding
    var order: Order? = null
    val gson = Gson()
    val TAG = "OrdersDetail"

    var adapter: OrderProductsAdapter? = null

    var toolbar: Toolbar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientOrdersDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        order = gson.fromJson(intent.getStringExtra("order"),Order::class.java)

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

        if(order?.status == "EN CAMINO"){
            binding.btnGoToMap2.visibility = View.VISIBLE
        }

        binding.btnGoToMap2.setOnClickListener { goToMap() }
    }

    private fun goToMap(){
        val i = Intent(this, ClientOrdersMapActivity::class.java)
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
}