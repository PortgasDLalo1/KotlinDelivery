package com.eduardo.kotlinudemydelivery.activities.sucursal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.isVisible
import com.eduardo.kotlinudemydelivery.Providers.SucursalesProvider
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.adapters.SucursalListAdapter
import com.eduardo.kotlinudemydelivery.databinding.ActivityAdminHomeBinding
import com.eduardo.kotlinudemydelivery.databinding.ActivitySucursalHomeBinding
import com.eduardo.kotlinudemydelivery.models.Sucursales
import com.eduardo.kotlinudemydelivery.models.User
import com.eduardo.kotlinudemydelivery.utils.SharedPref
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SucursalHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySucursalHomeBinding
    var adapter: SucursalListAdapter? = null
    var user: User? = null
    var sharedPref: SharedPref? = null
    var sucursaProvider: SucursalesProvider? = null
    var sucursales = ArrayList<Sucursales>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySucursalHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPref = SharedPref(this)
        getUserFromSession()
        sucursaProvider = SucursalesProvider(user?.sessionToken!!)
        getSucursal()
    }

    private fun getSucursal(){
        sucursaProvider?.getRestaurantsWithImage()?.enqueue(object : Callback<ArrayList<Sucursales>>{
            override fun onResponse(
                call: Call<ArrayList<Sucursales>>,
                response: Response<ArrayList<Sucursales>>
            ) {
                sucursales = response.body()!!
                adapter = SucursalListAdapter(this@SucursalHomeActivity,sucursales)
                binding.recyclerviewSucursalList?.adapter = adapter
                binding.shimmer.isVisible = false
                binding.recyclerviewSucursalList.isVisible = true
            }

            override fun onFailure(call: Call<ArrayList<Sucursales>>, t: Throwable) {

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
}