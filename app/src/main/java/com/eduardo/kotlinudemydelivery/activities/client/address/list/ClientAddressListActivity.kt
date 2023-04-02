package com.eduardo.kotlinudemydelivery.activities.client.address.list

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eduardo.kotlinudemydelivery.Providers.AddressProvider
import com.eduardo.kotlinudemydelivery.Providers.OrdersProvider
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.activities.client.address.create.ClientAddressCreateActivity
import com.eduardo.kotlinudemydelivery.activities.client.card.list.ClientCardListActivity
import com.eduardo.kotlinudemydelivery.activities.client.payments.mercado_pago.form.ClientPaymentFormActivity
import com.eduardo.kotlinudemydelivery.activities.client.payments.payment_method.ClientPaymentMethodActivity
import com.eduardo.kotlinudemydelivery.adapters.AddressAdapter
import com.eduardo.kotlinudemydelivery.adapters.ShoppingBagAdapter
import com.eduardo.kotlinudemydelivery.databinding.ActivityClientAddressListBinding
import com.eduardo.kotlinudemydelivery.models.*
import com.eduardo.kotlinudemydelivery.utils.SharedPref
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClientAddressListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClientAddressListBinding
    var toolbar: Toolbar? = null

    var recyclerViewAddress: RecyclerView? = null
    var addressProvider: AddressProvider? = null
    var ordersProvider: OrdersProvider? = null
    var sharedPref: SharedPref? = null
    var user: User? = null
    var address = ArrayList<Address>()
    var adapter: AddressAdapter? = null
    var gson = Gson()
    var selectedProducts = ArrayList<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientAddressListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPref = SharedPref(this)
        getProductsFromSharedPref()

        binding.recyclerviewAddress.layoutManager = LinearLayoutManager(this)

        toolbar = findViewById(R.id.toolbar)
        toolbar?.setTitleTextColor(ContextCompat.getColor(this,R.color.black))
        toolbar?.title = "Mis direcciones"
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        getUserFromSession()

        addressProvider = AddressProvider(user?.sessionToken!!)
        ordersProvider = OrdersProvider(user?.sessionToken!!)

        binding.fabAdddressCreate.setOnClickListener { goToAddressCreate() }

        binding.btnNextAddress.setOnClickListener { getAddressFromSession() }
        getAddress()
    }

    private fun getProductsFromSharedPref(){
        if (!sharedPref?.getData("order").isNullOrBlank()){ // si existe una orden en sharedpref
            val type = object: TypeToken<ArrayList<Product>>() {}.type
            selectedProducts = gson.fromJson(sharedPref?.getData("order"), type)
        }
    }

    private fun createOrder(idAddress: String){
        val order = Order(
            products = selectedProducts,
            id_client = user?.id!!,
            id_address = idAddress
        )

        ordersProvider?.create(order)?.enqueue(object: Callback<ResponseHttp>{
            override fun onResponse(call: Call<ResponseHttp>, response: Response<ResponseHttp>) {
                if (response.body() != null){
                    Toast.makeText(this@ClientAddressListActivity, "${response.body()?.message}", Toast.LENGTH_LONG).show()
                }
                else{
                    Toast.makeText(this@ClientAddressListActivity, "Ocurrio un error en la peticion", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ResponseHttp>, t: Throwable) {
                Toast.makeText(this@ClientAddressListActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }

        })
    }

    private fun getAddressFromSession(){
        if (!sharedPref?.getData("address").isNullOrBlank()){
            val a = gson.fromJson(sharedPref?.getData("address"),Address::class.java) // si existe una direccion
            goToPaymentsForm()
            //createOrder(a.id!!)
        }else{
            Toast.makeText(this, "Selecciona una direccion para continuar", Toast.LENGTH_LONG).show()
        }
    }

    private fun goToPaymentsForm(){
//        val i = Intent(this, ClientPaymentMethodActivity::class.java)
        val i = Intent(this, ClientCardListActivity::class.java)
        startActivity(i)
    }

    fun resetValue(position: Int){
        val viewHolder = binding.recyclerviewAddress.findViewHolderForAdapterPosition(position)
        val view = viewHolder?.itemView
        val imageViewCheck = view?.findViewById<ImageView>(R.id.imageview_check)
        imageViewCheck?.visibility = View.GONE
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