package com.eduardo.kotlinudemydelivery.activities.waiter.home

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import com.eduardo.kotlinudemydelivery.Providers.UsersProvider
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.databinding.ActivityWaiterHomeBinding
import com.eduardo.kotlinudemydelivery.fragments.client.ClientProfileFragment
import com.eduardo.kotlinudemydelivery.fragments.waiter.WaiterSaleFragment
import com.eduardo.kotlinudemydelivery.models.User
import com.eduardo.kotlinudemydelivery.utils.SharedPref
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson

class WaiterHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWaiterHomeBinding
    private val TAG = "WaiterHomeActivity"
    var sharedPref: SharedPref? = null

    var bottomNavigation: BottomNavigationView? = null

    var usersProvider: UsersProvider? = null
    var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWaiterHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPref = SharedPref(this)

        openFragment(WaiterSaleFragment())

        bottomNavigation = findViewById(R.id.bottom_navigation)
        bottomNavigation?.setOnItemSelectedListener {
            when(it.itemId){
                R.id.item_sale -> {
                    openFragment(WaiterSaleFragment())
                    true
                }
                R.id.item_profile -> {
                    openFragment(ClientProfileFragment())
                    true
                }
                else -> false
            }
        }

        getUserFromSession()
        usersProvider = UsersProvider(token = user?.sessionToken!!)
        createToken()
    }

    private fun createToken(){
        usersProvider?.createToken(user!!, this)
    }

    private fun openFragment(fragment: Fragment){

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()

    }

    private fun getUserFromSession(){
        val gson = Gson()
        if (!sharedPref?.getData("user").isNullOrBlank()){
            //si el usuario exite en sesion
            user = gson.fromJson(sharedPref?.getData("user"),User::class.java)
            Log.e(TAG, "Usuario: $user")
        }
    }
}