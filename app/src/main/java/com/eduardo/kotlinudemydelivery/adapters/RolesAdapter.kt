package com.eduardo.kotlinudemydelivery.adapters

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.activities.client.home.ClientHomeActivity
import com.eduardo.kotlinudemydelivery.activities.delivery.home.DeliveryHomeActivity
import com.eduardo.kotlinudemydelivery.activities.restaurant.home.RestaurantHomeActivity
import com.eduardo.kotlinudemydelivery.models.Rol
import com.eduardo.kotlinudemydelivery.utils.SharedPref

class RolesAdapter(val context: Activity, val roles: ArrayList<Rol>): RecyclerView.Adapter<RolesAdapter.RolesViewHolder>() {

    val sharedPref = SharedPref(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RolesViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_roles, parent,false)
        return RolesViewHolder(view)
    }

    override fun getItemCount(): Int {
        return roles.size
    }

    override fun onBindViewHolder(holder: RolesViewHolder, position: Int) {
        val rol = roles[position] //cada rol

        holder.textViewRol.text = rol.name
        Glide.with(context).load(rol.image).into(holder.imageView)

        holder.itemView.setOnClickListener {
            goToRol(rol)
        }
    }

    private fun goToRol(rol: Rol){
        if (rol.name == "RESTAURANTE"){
            sharedPref.save("rol","RESTAURANTE")
            val i = Intent(context,RestaurantHomeActivity::class.java)
            context.startActivity(i)
        }
        else if (rol.name == "CLIENTE"){
            sharedPref.save("rol","CLIENTE")
            val i = Intent(context,ClientHomeActivity::class.java)
            context.startActivity(i)
        }
        else if (rol.name == "REPARTIDOR"){
            sharedPref.save("rol","REPARTIDOR")
            val i = Intent(context,DeliveryHomeActivity::class.java)
            context.startActivity(i)
        }
    }

    class RolesViewHolder(view: View): RecyclerView.ViewHolder(view){

        val textViewRol: TextView
        val imageView: ImageView

        init {
            textViewRol = view.findViewById(R.id.textview_rol)
            imageView = view.findViewById(R.id.imageview_rol)
        }

    }

}