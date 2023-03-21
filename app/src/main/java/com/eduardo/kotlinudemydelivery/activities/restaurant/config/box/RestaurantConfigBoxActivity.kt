package com.eduardo.kotlinudemydelivery.activities.restaurant.config.box

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.activities.restaurant.config.box.sales.RestaurantConfigBoxSalesActivity
import com.eduardo.kotlinudemydelivery.databinding.ActivityRestaurantConfigBoxBinding

class RestaurantConfigBoxActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRestaurantConfigBoxBinding
    var toolbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRestaurantConfigBoxBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toolbar = findViewById(R.id.toolbar)
        toolbar?.setTitleTextColor(ContextCompat.getColor(this,R.color.black))
        toolbar?.title = "Caja"
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.cardSaleToDay.setOnClickListener { goToSales() }
    }

    private fun goToSales(){
        val i = Intent(this,RestaurantConfigBoxSalesActivity::class.java)
        startActivity(i)
    }

}