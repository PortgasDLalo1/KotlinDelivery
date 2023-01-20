package com.eduardo.kotlinudemydelivery.activities.client.address.create

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.eduardo.kotlinudemydelivery.Providers.AddressProvider
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.activities.client.address.list.ClientAddressListActivity
import com.eduardo.kotlinudemydelivery.activities.client.address.map.ClientAddressMapActivity
import com.eduardo.kotlinudemydelivery.databinding.ActivityClientAddressCreateBinding
import com.eduardo.kotlinudemydelivery.models.Address
import com.eduardo.kotlinudemydelivery.models.ResponseHttp
import com.eduardo.kotlinudemydelivery.models.User
import com.eduardo.kotlinudemydelivery.utils.SharedPref
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClientAddressCreateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClientAddressCreateBinding
    var toolbar: Toolbar? = null
    var addressLat = 0.0
    var addressLng = 0.0
    val TAG = "AddressCreate"
    var addressProvider: AddressProvider? = null
    var sharedPref: SharedPref? = null
    var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientAddressCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPref = SharedPref(this)
        getUserFromSession()
        addressProvider = AddressProvider(user?.sessionToken!!)

        toolbar = findViewById(R.id.toolbar)
        toolbar?.setTitleTextColor(ContextCompat.getColor(this,R.color.black))
        toolbar?.title = "Nueva dirección"
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.edittextRefPoint.setOnClickListener { goToAddressMap() }
        binding.btnCreateAddress.setOnClickListener { createAddress() }
    }

    private fun createAddress(){
        val address = binding.edittextAddress.text.toString()
        val neighborhood = binding.edittextNeighborhood.text.toString()

        if (isValidForm(address,neighborhood)){
            // lanza peticion

            val addressModel = Address(
                address = address,
                neighborhood = neighborhood,
                id_user = user?.id,
                lat = addressLat,
                lng = addressLng
            )

            addressProvider?.create(addressModel)?.enqueue(object : Callback<ResponseHttp>{
                override fun onResponse(
                    call: Call<ResponseHttp>,
                    response: Response<ResponseHttp>
                ) {
                    if (response.body() != null){
                        Toast.makeText(this@ClientAddressCreateActivity, response.body()?.message, Toast.LENGTH_LONG).show()
                        
                        goToAddressList()
                    }
                    else{
                        Toast.makeText(this@ClientAddressCreateActivity, "Ocurrio un error en la petición", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                override fun onFailure(call: Call<ResponseHttp>, t: Throwable) {
                    Toast.makeText(this@ClientAddressCreateActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                }

            })
        }
    }
    
    private fun goToAddressList(){
        val i = Intent(this, ClientAddressListActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK //eliminar historial de pantallas
        startActivity(i)
    }

    private fun isValidForm(address: String, neighborhoob:String):Boolean {
        if (address.isNullOrBlank()){
            Toast.makeText(this, "Ingresa la dirección", Toast.LENGTH_SHORT).show()
            return false
        }

        if (neighborhoob.isNullOrBlank()){
            Toast.makeText(this, "Ingresa tu colonia", Toast.LENGTH_SHORT).show()
            return false
        }

        if (addressLat == 0.0){
            Toast.makeText(this, "Selecciona el punto de referencia", Toast.LENGTH_SHORT).show()
            return false
        }

        if (addressLng == 0.0){
            Toast.makeText(this, "Selecciona el punto de referencia", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if (result.resultCode == Activity.RESULT_OK){
            val data = result.data
            val city = data?.getStringExtra("city")
            val address = data?.getStringExtra("address")
            val country = data?.getStringExtra("country")
            addressLat = data?.getDoubleExtra("lat",0.0)!!
            addressLng = data?.getDoubleExtra("lng",0.0)!!

            binding.edittextRefPoint.setText("$address $city")

            Log.d(TAG,"City $city")
            Log.d(TAG,"Address $address")
            Log.d(TAG,"Country $country")
            Log.d(TAG,"Lat $addressLat")
            Log.d(TAG,"lng $addressLng")
        }
    }

    private fun goToAddressMap(){
        val i = Intent(this, ClientAddressMapActivity::class.java)
        resultLauncher.launch(i)
    }

    private fun getUserFromSession(){
        val gson = Gson()
        if (!sharedPref?.getData("user").isNullOrBlank()){
            //si el usuario exite en sesion
            user = gson.fromJson(sharedPref?.getData("user"), User::class.java)
        }
    }
}