package com.eduardo.kotlinudemydelivery.fragments.client

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eduardo.kotlinudemydelivery.Providers.CategoriesProvider
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.activities.client.shopping_bag.ClientShoppingBagActivity
import com.eduardo.kotlinudemydelivery.adapters.CategoriesAdapter
import com.eduardo.kotlinudemydelivery.adapters.ShoppingBagAdapter
import com.eduardo.kotlinudemydelivery.databinding.FragmentClientCategoriesBinding
import com.eduardo.kotlinudemydelivery.models.Category
import com.eduardo.kotlinudemydelivery.models.Product
import com.eduardo.kotlinudemydelivery.models.User
import com.eduardo.kotlinudemydelivery.utils.SharedPref
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClientCategoriesFragment : Fragment() {
    private lateinit var binding: FragmentClientCategoriesBinding
    var myView: View? = null
    var recyclerViewCategories: RecyclerView? = null
    var categoriesProvider: CategoriesProvider? = null
    var adapter: CategoriesAdapter? = null
    var user: User? = null
    var sharedPref: SharedPref? = null
    var categories = ArrayList<Category>()
    val TAG = "CategoriesFragment"
    var toolbar: Toolbar? = null
    private lateinit var badge: BadgeDrawable
    private var contador = 0
    var selectedProducts = ArrayList<Product>()
    var gson = Gson()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentClientCategoriesBinding.inflate(inflater,container,false)
//        myView = inflater.inflate(R.layout.fragment_client_categories, container, false)
        val view = binding.root
        badge = BadgeDrawable.create(requireContext())

        setHasOptionsMenu(true)
//        toolbar = myView?.findViewById(R.id.toolbar)
        binding.toolbar?.setTitleTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        binding. toolbar?.title = "Categorias"
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)

//        recyclerViewCategories = myView?.findViewById(R.id.recyclerview_categories)
//        recyclerViewCategories?.layoutManager = LinearLayoutManager(requireContext()) //los elementos se mostraran de manera vertical

        sharedPref = SharedPref(requireActivity())

        getUserFromSession()

        categoriesProvider = CategoriesProvider(user?.sessionToken!!)
        configSwipe()
        getCategories()
        return view
    }

    private fun configSwipe() {
        binding.swipe?.setColorSchemeResources(R.color.green, R.color.blue, R.color.orange)
        binding.swipe?.setOnRefreshListener {
            binding.shimmerCat?.isVisible = true
            binding.recyclerviewCategories?.isVisible = false
            getCategories()
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_badge_toolbar, menu)
        BadgeUtils.attachBadgeDrawable(badge,binding.toolbar!!, R.id.menu_badge)
        //badge.number = contador
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.menu_badge){
            goToShoppingBag()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun goToShoppingBag(){
        val i = Intent(requireContext(),ClientShoppingBagActivity::class.java)
        startActivity(i)
    }

    private fun getCategories(){
        categoriesProvider?.getAll()?.enqueue(object: Callback<ArrayList<Category>> {
            override fun onResponse(
                call: Call<ArrayList<Category>>,
                response: Response<ArrayList<Category>>
            ) {
                if (response.body() != null){
                    categories = response.body()!!
                    adapter = CategoriesAdapter(requireActivity(),categories)
                    binding.recyclerviewCategories?.adapter = adapter
                    binding.shimmerCat?.isVisible = false
                    binding.recyclerviewCategories?.isVisible = true
                    binding.swipe?.isRefreshing = false
                }
            }

            override fun onFailure(call: Call<ArrayList<Category>>, t: Throwable) {
                Log.d(TAG, "Error: ${t.message}")
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun getUserFromSession(){
//        val gson = Gson()
        if (!sharedPref?.getData("user").isNullOrBlank()){
            //si el usuario exite en sesion
            user = gson.fromJson(sharedPref?.getData("user"), User::class.java)
        }
    }

    override fun onResume() {
        getProductsFromSharedPref()
        super.onResume()
    }

    private fun getProductsFromSharedPref(){
        if (!sharedPref?.getData("order").isNullOrBlank()){ // si existe una orden en sharedpref
            val type = object: TypeToken<ArrayList<Product>>() {}.type
            selectedProducts = gson.fromJson(sharedPref?.getData("order"), type)
            contador = selectedProducts.size
            badge.number = contador
        }else{
            badge.number = 0
        }
    }
}