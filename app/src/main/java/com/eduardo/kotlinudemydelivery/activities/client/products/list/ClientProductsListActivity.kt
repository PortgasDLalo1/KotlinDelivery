package com.eduardo.kotlinudemydelivery.activities.client.products.list

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eduardo.kotlinudemydelivery.Providers.ProductsProvider
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.adapters.ProductsAdapter
import com.eduardo.kotlinudemydelivery.databinding.ActivityClientProductsListBinding
import com.eduardo.kotlinudemydelivery.models.Product
import com.eduardo.kotlinudemydelivery.models.User
import com.eduardo.kotlinudemydelivery.utils.SharedPref
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClientProductsListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClientProductsListBinding

    var recyclerViewProducts: RecyclerView? = null
    var adapter: ProductsAdapter? = null
    var user: User? = null
    var productsProvider: ProductsProvider? = null
    var products: ArrayList<Product> = ArrayList()
    var sharedPref: SharedPref? = null
    var idCategory: String? = null

    val TAG = "ClientProducts"
    var toolbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientProductsListBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_client_products_list)

        toolbar = findViewById(R.id.toolbar)
        toolbar?.title = "Productos"
        toolbar?.setTitleTextColor(ContextCompat.getColor(this,R.color.black))
        setSupportActionBar(toolbar)

        sharedPref = SharedPref(this)
        idCategory = intent.getStringExtra("idCategory")
        getUserFromSession()
        productsProvider = ProductsProvider(user?.sessionToken!!)

        recyclerViewProducts = findViewById(R.id.recyclerview_products)
        recyclerViewProducts?.layoutManager = GridLayoutManager(this,2)

        getProducts()
    }

    private fun getProducts(){
        productsProvider?.findByCategory(idCategory!!)?.enqueue(object: Callback<ArrayList<Product>>{
            override fun onResponse(
                call: Call<ArrayList<Product>>,
                response: Response<ArrayList<Product>>
            ) {
                if (response.body() != null){
                    products = response.body()!!
                    adapter = ProductsAdapter(this@ClientProductsListActivity,products)
                    recyclerViewProducts?.adapter = adapter
                }
            }

            override fun onFailure(call: Call<ArrayList<Product>>, t: Throwable) {
                Toast.makeText(this@ClientProductsListActivity, t.message, Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Error: ${t.message}")
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