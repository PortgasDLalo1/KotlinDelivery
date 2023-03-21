package com.eduardo.kotlinudemydelivery.activities.restaurant.home

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.eduardo.kotlinudemydelivery.Providers.UsersProvider
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.databinding.ActivityRestaurantHomeBinding
import com.eduardo.kotlinudemydelivery.fragments.client.ClientCategoriesFragment
import com.eduardo.kotlinudemydelivery.fragments.client.ClientOrdersFragment
import com.eduardo.kotlinudemydelivery.fragments.client.ClientProfileFragment
import com.eduardo.kotlinudemydelivery.fragments.restaurant.RestaurantCategoryFragment
import com.eduardo.kotlinudemydelivery.fragments.restaurant.RestaurantConfigFragment
import com.eduardo.kotlinudemydelivery.fragments.restaurant.RestaurantOrdersFragment
import com.eduardo.kotlinudemydelivery.fragments.restaurant.RestaurantProductFragment
import com.eduardo.kotlinudemydelivery.models.User
import com.eduardo.kotlinudemydelivery.utils.SharedPref
import com.eduardo.kotlinudemydelivery.utils.printTicket
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson

class RestaurantHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRestaurantHomeBinding
    private val TAG = "RestaurantHomeActivity"
    var sharedPref: SharedPref? = null

    var bottomNavigation: BottomNavigationView? = null

    var usersProvider: UsersProvider? = null
    var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRestaurantHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPref = SharedPref(this)

        openFragment(RestaurantOrdersFragment())

        bottomNavigation = findViewById(R.id.bottom_navigation)
        bottomNavigation?.setOnItemSelectedListener {
            when(it.itemId){

                R.id.item_home -> {
                    openFragment(RestaurantOrdersFragment())
                    true
                }

                R.id.item_category -> {
                    openFragment(RestaurantCategoryFragment())
                    true
                }

                R.id.item_product -> {
                    openFragment(RestaurantProductFragment())
                    true
                }

                R.id.item_profile -> {
                    openFragment(ClientProfileFragment())
                    true
                }

                R.id.item_build -> {
                    openFragment(RestaurantConfigFragment())
                    true
                }

                else -> false

            }
        }
        getUserFromSession()
        usersProvider = UsersProvider(token = user?.sessionToken!!)
        createToken()
        //binding.btnLogout.setOnClickListener { logout() }
        checaBluetooth()

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

    private fun logout(){
        sharedPref?.remove("user")
        finish()
    }

    private fun getUserFromSession(){
        val gson = Gson()
        if (!sharedPref?.getData("user").isNullOrBlank()){
            //si el usuario exite en sesion
            user = gson.fromJson(sharedPref?.getData("user"),User::class.java)
            Log.e(TAG, "Usuario: $user")
        }
    }

    private fun checaBluetooth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT))
        }
        else{
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetooth.launch(enableBtIntent)
        }
    }

    private var requestBluetooth = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            //granted
            Toast.makeText(this, "Bluetooth is enable", Toast.LENGTH_SHORT).show()
        }else{
            //deny
            Toast.makeText(this, "Bluetooth is enable", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d("test006", "${it.key} = ${it.value}")
            }
        }

}