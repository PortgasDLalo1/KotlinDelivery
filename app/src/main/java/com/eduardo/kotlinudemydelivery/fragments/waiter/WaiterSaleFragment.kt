package com.eduardo.kotlinudemydelivery.fragments.waiter

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eduardo.kotlinudemydelivery.Providers.CategoriesProvider
import com.eduardo.kotlinudemydelivery.Providers.ProductsProvider
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.adapters.CategoriesSaleAdapter
import com.eduardo.kotlinudemydelivery.adapters.ProductsSaleAdapter
import com.eduardo.kotlinudemydelivery.models.Category
import com.eduardo.kotlinudemydelivery.models.Product
import com.eduardo.kotlinudemydelivery.models.User
import com.eduardo.kotlinudemydelivery.utils.SharedPref
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class WaiterSaleFragment : Fragment() {

    var myView: View? = null
    var recyclerViewSale: RecyclerView? = null
    var recyclerViewProducts: RecyclerView? = null
    var categoriesProvider: CategoriesProvider? = null
    var adapter: CategoriesSaleAdapter? = null
    var user: User? = null
    var sharedPref: SharedPref? = null
    var categories = ArrayList<Category>()
    val TAG = "CategoriesSale"
    var toolbar: Toolbar? = null
    var productsProvider: ProductsProvider? = null
    var products: ArrayList<Product> = ArrayList()
    var idCategory: String? = null
    var adapter2: ProductsSaleAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        myView = inflater.inflate(R.layout.fragment_waiter_sale, container, false)
        toolbar = myView?.findViewById(R.id.toolbar)
        toolbar?.setTitleTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        toolbar?.title = "Venta"
        (activity as AppCompatActivity).setSupportActionBar(toolbar)

        recyclerViewSale = myView?.findViewById(R.id.recyclerview_categories_sale)
        recyclerViewSale?.layoutManager = GridLayoutManager(activity,3)

        recyclerViewProducts = myView?.findViewById(R.id.recyclerview_products_sale)
        recyclerViewProducts?.layoutManager = GridLayoutManager(activity,3)

        sharedPref = SharedPref(requireActivity())

        getUserFromSession()

        categoriesProvider = CategoriesProvider(user?.sessionToken!!)
        productsProvider = ProductsProvider(user?.sessionToken!!)

        getCategories()
        //getProducts("5")
        return myView
    }

    private fun getCategories(){
        categoriesProvider?.getAll()?.enqueue(object: Callback<ArrayList<Category>> {
            override fun onResponse(
                call: Call<ArrayList<Category>>,
                response: Response<ArrayList<Category>>
            ) {
                if (response.body() != null){
                    categories = response.body()!!
                    adapter = CategoriesSaleAdapter(requireActivity(),categories)
                    recyclerViewSale?.adapter = adapter
                }
            }

            override fun onFailure(call: Call<ArrayList<Category>>, t: Throwable) {
                Log.d(TAG, "Error: ${t.message}")
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }

        })
    }

    fun getProducts(product: ArrayList<Product>){
        Log.d(TAG,"SI ENTRAAAAAAAA: $product")
        /*productsProvider?.findByCategory(id_category)?.enqueue(object: Callback<ArrayList<Product>>{
            override fun onResponse(
                call: Call<ArrayList<Product>>,
                response: Response<ArrayList<Product>>
            ) {
                if (response.body() != null){
                    val products = response.body()!!
                    adapter2 = ProductsSaleAdapter(requireActivity(),products)
                    recyclerViewProducts?.adapter = adapter2
                    Log.d(TAG,"SI ENTRAAAAAAAA 222222222")
                }
            }

            override fun onFailure(call: Call<ArrayList<Product>>, t: Throwable) {
                Toast.makeText(requireContext(), t.message, Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Error: ${t.message}")
            }

        })*/

        adapter2 = ProductsSaleAdapter(requireActivity(),product)
        recyclerViewProducts?.adapter = adapter2
    }

    private fun getUserFromSession(){
        val gson = Gson()
        if (!sharedPref?.getData("user").isNullOrBlank()){
            //si el usuario exite en sesion
            user = gson.fromJson(sharedPref?.getData("user"), User::class.java)
        }
    }
}