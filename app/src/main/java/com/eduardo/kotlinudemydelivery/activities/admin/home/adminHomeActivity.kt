package com.eduardo.kotlinudemydelivery.activities.admin.home

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import com.eduardo.kotlinudemydelivery.Providers.UsersProvider
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.databinding.ActivityAdminHomeBinding
import com.eduardo.kotlinudemydelivery.databinding.ActivityRestaurantHomeBinding
import com.eduardo.kotlinudemydelivery.fragments.client.ClientProfileFragment
import com.eduardo.kotlinudemydelivery.fragments.restaurant.RestaurantConfigFragment
import com.eduardo.kotlinudemydelivery.fragments.restaurant.RestaurantOrdersStatusFragment
import com.eduardo.kotlinudemydelivery.fragments.restaurant.categories.RestaurantCategoryListFragment
import com.eduardo.kotlinudemydelivery.fragments.restaurant.products.RestaurantProductListFragment
import com.eduardo.kotlinudemydelivery.models.User
import com.eduardo.kotlinudemydelivery.utils.SharedPref
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson

class adminHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminHomeBinding
    var sharedPref: SharedPref?=null
    var bottomNavigation: BottomNavigationView? = null
    var gson = Gson()
    var user: User? = null
    var usersProvider: UsersProvider? = null
    private lateinit var myFragment: RestaurantCategoryListFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPref = SharedPref(this)

        getUserFromSession()
        createToken()
        usersProvider = UsersProvider(user?.sessionToken!!)
        openFragment(RestaurantCategoryListFragment())
        myFragment = RestaurantCategoryListFragment()

        bottomNavigation = findViewById(R.id.bottom_navigation)
        bottomNavigation?.setOnItemSelectedListener {
            when(it.itemId){
                R.id.item_category -> {
//                    openFragment(RestaurantCategoryFragment())
//                    openFragment(RestaurantCategoryListFragment())
                    loadFragment(RestaurantCategoryListFragment())
                    true
                }

                R.id.item_product -> {
                    loadFragment(RestaurantProductListFragment())
//                    openFragment(RestaurantProductListFragment())
//                    openFragment(RestaurantProductFragment())
                    true
                }

                R.id.item_build -> {
                    loadFragment(RestaurantConfigFragment())
//                    openFragment(RestaurantConfigFragment())
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
    }

    private fun getUserFromSession(){
        val gson = Gson()
        if (!sharedPref?.getData("user").isNullOrBlank()){
            //si el usuario exite en sesion
            user = gson.fromJson(sharedPref?.getData("user"), User::class.java)
            Log.e("FATAL", "Usuario: $user")
        }
    }

    private fun createToken(){
        usersProvider?.createToken(user!!, this)
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

    private fun openFragment(fragment: Fragment){

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment, fragment.javaClass.name)
        transaction.addToBackStack("F_MAIN")
        transaction.commit()

    }
}