package com.eduardo.kotlinudemydelivery.adapters

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.models.Sucursales

class SucursalListAdapter(val context: Activity, val sucursales: ArrayList<Sucursales>): RecyclerView.Adapter<SucursalListAdapter.SucursalListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SucursalListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cardview_sucursal, parent, false)
        return SucursalListViewHolder(view)
    }

    override fun getItemCount(): Int {
        return sucursales.size
    }

    override fun onBindViewHolder(holder: SucursalListViewHolder, position: Int) {
        val sucursal = sucursales[position]
        Log.d("FATAL", "$sucursal")
        holder.textViewSucursal.text = sucursal.neighborhood
        holder.textViewSucursalAddress.text = sucursal.address
        Glide.with(context).load(sucursal.image).into(holder.imageViewSucursal)
    }

    class SucursalListViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val imageViewSucursal: ImageView
        val textViewSucursal: TextView
        val textViewSucursalAddress: TextView

        init {
            imageViewSucursal = view.findViewById(R.id.imageview_sucursal)
            textViewSucursal = view.findViewById(R.id.textview_sucusalname)
            textViewSucursalAddress = view.findViewById(R.id.textview_sucursalAddress)
        }
    }
}