package com.eduardo.kotlinudemydelivery.activities.client.shopping_bag

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.activities.client.address.create.ClientAddressCreateActivity
import com.eduardo.kotlinudemydelivery.activities.client.address.list.ClientAddressListActivity
import com.eduardo.kotlinudemydelivery.activities.client.orders.checkout.ClientOrderCheckOutActivity
import com.eduardo.kotlinudemydelivery.adapters.ShoppingBagAdapter
import com.eduardo.kotlinudemydelivery.databinding.ActivityClientShoppingBagBinding
import com.eduardo.kotlinudemydelivery.models.Product
import com.eduardo.kotlinudemydelivery.utils.SharedPref
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ClientShoppingBagActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClientShoppingBagBinding

    var recyclerViewShoppingBag: RecyclerView? = null

    var toolbar: Toolbar? = null
    var adapter: ShoppingBagAdapter? = null
    var sharedPref: SharedPref? = null
    var gson = Gson()
    var selectedProducts = ArrayList<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientShoppingBagBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPref = SharedPref(this)

        toolbar = findViewById(R.id.toolbar)
        toolbar?.setTitleTextColor(ContextCompat.getColor(this,R.color.black))
        toolbar?.title = "Tu orden"

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.recyclerviewShoppingbag.layoutManager = LinearLayoutManager(this)

        getProductsFromSharedPref()

        binding.btnNext.setOnClickListener { goToAddressList() }
    }

    private fun goToAddressList(){
        val i = Intent(this, ClientOrderCheckOutActivity::class.java)
        startActivity(i)
    }

    fun setTotal(total: Double){
        binding.textviewTotal.text = "$ ${total}"
    }

    private fun getProductsFromSharedPref(){
        if (!sharedPref?.getData("order").isNullOrBlank()){ // si existe una orden en sharedpref
            val type = object: TypeToken<ArrayList<Product>>() {}.type
            selectedProducts = gson.fromJson(sharedPref?.getData("order"), type)
            adapter = ShoppingBagAdapter(this, selectedProducts)
            binding.recyclerviewShoppingbag.adapter = adapter
        }
    }
}