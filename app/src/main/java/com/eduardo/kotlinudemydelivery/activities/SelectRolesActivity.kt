package com.eduardo.kotlinudemydelivery.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.adapters.RolesAdapter
import com.eduardo.kotlinudemydelivery.databinding.ActivitySelectRolesBinding
import com.eduardo.kotlinudemydelivery.models.User
import com.eduardo.kotlinudemydelivery.utils.SharedPref
import com.google.gson.Gson

class SelectRolesActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectRolesBinding
    var recyclerViewRoles: RecyclerView? = null
    var user: User? = null
    var adapter: RolesAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectRolesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerViewRoles = findViewById(R.id.recyclerview_roles)
        //binding.recyclerviewRoles.layoutManager = LinearLayoutManager(this)
        recyclerViewRoles?.layoutManager = LinearLayoutManager(this)
        getUserFromSession()
        adapter = RolesAdapter(this, user?.roles!!)
        recyclerViewRoles?.adapter = adapter
    }

    private fun getUserFromSession(){
        val sharedPref = SharedPref(this)
        val gson = Gson()
        if (!sharedPref.getData("user").isNullOrBlank()){
            //si el usuario exite en sesion
            user = gson.fromJson(sharedPref.getData("user"), User::class.java)
        }
    }
}