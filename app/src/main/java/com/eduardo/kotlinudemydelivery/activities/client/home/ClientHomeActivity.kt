package com.eduardo.kotlinudemydelivery.activities.client.home

import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.eduardo.kotlinudemydelivery.Providers.UsersProvider
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.activities.client.address.list.ClientAddressListActivity
import com.eduardo.kotlinudemydelivery.adapters.ShoppingBagAdapter
import com.eduardo.kotlinudemydelivery.databinding.ActivityClientHomeBinding
import com.eduardo.kotlinudemydelivery.fragments.client.ClientCategoriesFragment
import com.eduardo.kotlinudemydelivery.fragments.client.ClientOrdersFragment
import com.eduardo.kotlinudemydelivery.fragments.client.ClientProfileFragment
import com.eduardo.kotlinudemydelivery.models.Product
import com.eduardo.kotlinudemydelivery.models.User
import com.eduardo.kotlinudemydelivery.utils.SharedPref
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ClientHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClientHomeBinding
    private val TAG = "ClientHomeActivity1"
    var sharedPref: SharedPref? = null

    var bottomNavigation: BottomNavigationView? = null
    var selectedProducts = ArrayList<Product>()
    var gson = Gson()

    var usersProvider: UsersProvider? = null
    var user: User? = null
    private var myFragment: ClientCategoriesFragment? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        sharedPref = SharedPref(this)

        openFragment(ClientCategoriesFragment())
        myFragment = ClientCategoriesFragment()
        bottomNavigation = findViewById(R.id.bottom_navigation)
        bottomNavigation?.setOnItemSelectedListener {
            when(it.itemId){

                R.id.item_home -> {
                    loadFragment(ClientCategoriesFragment())
//                    openFragment(ClientCategoriesFragment())
                    true
                }

                R.id.item_orders -> {
                    loadFragment(ClientOrdersFragment())
//                    openFragment(ClientOrdersFragment())
                    true
                }

                R.id.item_profile -> {
                    loadFragment(ClientProfileFragment())
//                    openFragment(ClientProfileFragment())
                    true
                }

                else -> false

            }
        }
        getUserFromSession()
        getAddressFromSession()
        //getProductsFromSharedPref()
        usersProvider = UsersProvider(token = user?.sessionToken!!)
        createToken()
       //binding.btnLogout.setOnClickListener { logout() }
    }

    private fun loadFragment(newFragment: Fragment){
        if (myFragment != null){
            val current: Fragment? = supportFragmentManager.findFragmentById(R.id.container)
            if (current == null){
                openFragment(newFragment)
            }else if (!current.javaClass.name.equals(newFragment.javaClass.name)){
                openFragment(newFragment)
            }else{
//                Toast.makeText(this, "${newFragment.javaClass.name}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createToken(){
        usersProvider?.createToken(user!!, this)
    }


    private fun openFragment(fragment: Fragment){

        val transaction = supportFragmentManager.beginTransaction()
//        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()

    }

    private fun getUserFromSession(){
        val gson = Gson()
        if (!sharedPref?.getData("user").isNullOrBlank()){
            //si el usuario exite en sesion
            user = gson.fromJson(sharedPref?.getData("user"),User::class.java)
            //Log.e(TAG, "Usuario: $user")
        }
    }

    private fun getAddressFromSession(){
        if(sharedPref?.getData("address").isNullOrBlank()){
            val i = Intent(this,ClientAddressListActivity::class.java)
            i.putExtra("bandera",1)
            startActivity(i)
        }
    }

    private fun getProductsFromSharedPref(){
        if (!sharedPref?.getData("order").isNullOrBlank()){ // si existe una orden en sharedpref
            val type = object: TypeToken<ArrayList<Product>>() {}.type
            selectedProducts = gson.fromJson(sharedPref?.getData("order"), type)
            //Log.e(TAG, "PRODUCTOS: $selectedProducts")
        }
    }
}