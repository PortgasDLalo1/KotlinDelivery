package com.eduardo.kotlinudemydelivery.activities.client.address.list

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eduardo.kotlinudemydelivery.Providers.AddressProvider
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.activities.client.address.create.ClientAddressCreateActivity
import com.eduardo.kotlinudemydelivery.adapters.AddressAdapter
import com.eduardo.kotlinudemydelivery.databinding.ActivityClientAddressListBinding
import com.eduardo.kotlinudemydelivery.models.Address
import com.eduardo.kotlinudemydelivery.models.User
import com.eduardo.kotlinudemydelivery.utils.SharedPref
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClientAddressListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClientAddressListBinding
    var toolbar: Toolbar? = null

    var recyclerViewAddress: RecyclerView? = null
    var addressProvider: AddressProvider? = null
    var sharedPref: SharedPref? = null
    var user: User? = null
    var address = ArrayList<Address>()
    var adapter: AddressAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientAddressListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPref = SharedPref(this)
        binding.recyclerviewAddress.layoutManager = LinearLayoutManager(this)

        toolbar = findViewById(R.id.toolbar)
        toolbar?.setTitleTextColor(ContextCompat.getColor(this,R.color.black))
        toolbar?.title = "Mis direcciones"
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        getUserFromSession()

        addressProvider = AddressProvider(user?.sessionToken!!)

        binding.fabAdddressCreate.setOnClickListener { goToAddressCreate() }

        getAddress()
    }

    private fun getAddress(){
        addressProvider?.getAddress(user?.id!!)?.enqueue(object: Callback<ArrayList<Address>>{
            override fun onResponse(
                call: Call<ArrayList<Address>>,
                response: Response<ArrayList<Address>>
            ) {
                if (response.body() != null){
                    address = response.body()!!
                    adapter = AddressAdapter(this@ClientAddressListActivity, address)
                    binding.recyclerviewAddress.adapter = adapter
                }
            }

            override fun onFailure(call: Call<ArrayList<Address>>, t: Throwable) {
                Toast.makeText(this@ClientAddressListActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }

        })
    }

    private fun goToAddressCreate(){
        val i = Intent(this,ClientAddressCreateActivity::class.java)
        startActivity(i)
    }

    private fun getUserFromSession(){
        val gson = Gson()
        if (!sharedPref?.getData("user").isNullOrBlank()){
            //si el usuario exite en sesion
            user = gson.fromJson(sharedPref?.getData("user"), User::class.java)
        }
    }
}