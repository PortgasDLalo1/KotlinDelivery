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
import com.eduardo.kotlinudemydelivery.activities.client.products.list.ClientProductsListActivity
import com.eduardo.kotlinudemydelivery.activities.delivery.home.DeliveryHomeActivity
import com.eduardo.kotlinudemydelivery.activities.restaurant.home.RestaurantHomeActivity
import com.eduardo.kotlinudemydelivery.models.Address
import com.eduardo.kotlinudemydelivery.models.Category
import com.eduardo.kotlinudemydelivery.models.Rol
import com.eduardo.kotlinudemydelivery.utils.SharedPref

class AddressAdapter(val context: Activity, val address: ArrayList<Address>): RecyclerView.Adapter<AddressAdapter.AddressViewHolder>() {

    val sharedPref = SharedPref(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cardview_address, parent,false)
        return AddressViewHolder(view)
    }

    override fun getItemCount(): Int {
        return address.size
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        val a = address[position] //cada categoria

        holder.textViewAddress.text = a.address
        holder.textViewNeighborhood.text = a.neighborhood

    }

    class AddressViewHolder(view: View): RecyclerView.ViewHolder(view){

        val textViewAddress: TextView
        val textViewNeighborhood: TextView
        val imageViewCheck: ImageView

        init {
            textViewAddress = view.findViewById(R.id.textview_addressD)
            textViewNeighborhood = view.findViewById(R.id.textview_neighborhood)
            imageViewCheck = view.findViewById(R.id.imageview_check)
        }

    }

}