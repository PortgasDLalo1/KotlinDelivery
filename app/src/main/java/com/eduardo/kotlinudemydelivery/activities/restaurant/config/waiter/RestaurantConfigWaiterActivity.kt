package com.eduardo.kotlinudemydelivery.activities.restaurant.config.waiter

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.eduardo.kotlinudemydelivery.Providers.UsersProvider
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.adapters.RestaurantWaiterAdapter
import com.eduardo.kotlinudemydelivery.databinding.ActivityRestaurantConfigWaiterBinding
import com.eduardo.kotlinudemydelivery.models.User
import com.eduardo.kotlinudemydelivery.utils.SharedPref
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RestaurantConfigWaiterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRestaurantConfigWaiterBinding
    var toolbar: Toolbar? = null
    var usersProvider: UsersProvider? = null
    val gson = Gson()
    var user: User? = null
    var sharedPref: SharedPref? = null
    var adapter: RestaurantWaiterAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRestaurantConfigWaiterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPref = SharedPref(this)
        getUserFromSession()
        usersProvider = UsersProvider(user?.sessionToken!!)
        toolbar = findViewById(R.id.toolbar)
        toolbar?.setTitleTextColor(ContextCompat.getColor(this,R.color.black))
        toolbar?.title = "Meseros"
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.recyclerviewWaiter.layoutManager = LinearLayoutManager(this)

        getWaiter()

        binding.btnRegisterWaiter.setOnClickListener { goToRegisterWaiter() }
    }

    private fun goToRegisterWaiter(){
        val i = Intent(this, RestaurantWaiterFormActivity::class.java)
        startActivity(i)
    }

    fun getWaiter(){
        usersProvider?.getWaiter()?.enqueue(object : Callback<ArrayList<User>>{
            override fun onResponse(
                call: Call<ArrayList<User>>,
                response: Response<ArrayList<User>>
            ) {
                if (response.body() != null){
                    val waiter = response.body()!!
                    adapter = RestaurantWaiterAdapter(this@RestaurantConfigWaiterActivity,waiter)
                    binding.recyclerviewWaiter.adapter = adapter
                }
            }

            override fun onFailure(call: Call<ArrayList<User>>, t: Throwable) {
                Toast.makeText(this@RestaurantConfigWaiterActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
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
        getWaiter()
    }
}