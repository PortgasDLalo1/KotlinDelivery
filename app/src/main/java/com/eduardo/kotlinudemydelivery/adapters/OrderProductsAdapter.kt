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
import com.eduardo.kotlinudemydelivery.activities.client.products.detail.ClientProductsDetailActivity
import com.eduardo.kotlinudemydelivery.activities.client.shopping_bag.ClientShoppingBagActivity
import com.eduardo.kotlinudemydelivery.activities.delivery.home.DeliveryHomeActivity
import com.eduardo.kotlinudemydelivery.activities.restaurant.home.RestaurantHomeActivity
import com.eduardo.kotlinudemydelivery.models.Category
import com.eduardo.kotlinudemydelivery.models.Product
import com.eduardo.kotlinudemydelivery.models.Rol
import com.eduardo.kotlinudemydelivery.utils.SharedPref
import com.google.gson.Gson

class OrderProductsAdapter(val context: Activity, val products: ArrayList<Product>): RecyclerView.Adapter<OrderProductsAdapter.OrderProductsViewHolder>() {

    val sharedPref = SharedPref(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderProductsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cardview_order_products, parent,false)
        return OrderProductsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return products.size
    }

    override fun onBindViewHolder(holder: OrderProductsViewHolder, position: Int) {
        val product = products[position] //cada categoria

        holder.textViewProduct.text = product.name

        if (product.quantity != null){
            holder.textViewQuantity.text = "${product.quantity!!}"
        }
        holder.textViewTotalP.text = "$ ${product.price * product.quantity!!}"
        Glide.with(context).load(product.image1).into(holder.imageViewProduct)

    }

    class OrderProductsViewHolder(view: View): RecyclerView.ViewHolder(view){

        val textViewProduct: TextView
        val textViewQuantity: TextView
        val textViewTotalP: TextView
        val imageViewProduct: ImageView


        init {
            textViewProduct = view.findViewById(R.id.textview_name)
            textViewQuantity = view.findViewById(R.id.textview_quantity)
            textViewTotalP = view.findViewById(R.id.textview_totalP)
            imageViewProduct = view.findViewById(R.id.imageview_product)
        }

    }

}