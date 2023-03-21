package com.eduardo.kotlinudemydelivery.activities.restaurant.config.delivery

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eduardo.kotlinudemydelivery.Providers.UsersProvider
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.adapters.RestaurantDeliveryAdapter
import com.eduardo.kotlinudemydelivery.databinding.ActivityRestaurantDeliveryConfigBinding
import com.eduardo.kotlinudemydelivery.models.User
import com.eduardo.kotlinudemydelivery.utils.SharedPref
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RestaurantDeliveryConfigActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRestaurantDeliveryConfigBinding
    var toolbar: Toolbar? = null

    var usersProvider: UsersProvider? = null
    val gson = Gson()
    var user: User? = null
    var sharedPref: SharedPref? = null
    var adapter: RestaurantDeliveryAdapter? = null

    var recyclerDeliveries: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRestaurantDeliveryConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPref = SharedPref(this)
        getUserFromSession()
        usersProvider = UsersProvider(user?.sessionToken!!)
        toolbar = findViewById(R.id.toolbar)
        toolbar?.setTitleTextColor(ContextCompat.getColor(this,R.color.black))
        toolbar?.title = "Repartidores"
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.recyclerviewDelivery.layoutManager = LinearLayoutManager(this)

        getDelivery()

        binding.btnRegisterDelivery.setOnClickListener { goToRegisterDelivery() }

    }

    private fun goToRegisterDelivery(){
        val i = Intent(this,RestaurantDeliveryFormActivity::class.java)
//        i.putExtra("bandera","1")
        startActivity(i)
    }

    fun getDelivery(){
        usersProvider?.getDelivery()?.enqueue(object: Callback<ArrayList<User>> {
            override fun onResponse(
                call: Call<ArrayList<User>>,
                response: Response<ArrayList<User>>
            ) {
                if (response.body() != null){
                    val delivery = response.body()!!
                    adapter = RestaurantDeliveryAdapter(this@RestaurantDeliveryConfigActivity,delivery)
                    binding.recyclerviewDelivery.adapter = adapter
                }
            }

            override fun onFailure(call: Call<ArrayList<User>>, t: Throwable) {
                Toast.makeText(this@RestaurantDeliveryConfigActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
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

    override fun onResume() {
        super.onResume()
        getDelivery()
    }
}