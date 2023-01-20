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
import com.eduardo.kotlinudemydelivery.activities.delivery.home.DeliveryHomeActivity
import com.eduardo.kotlinudemydelivery.activities.restaurant.home.RestaurantHomeActivity
import com.eduardo.kotlinudemydelivery.models.Category
import com.eduardo.kotlinudemydelivery.models.Product
import com.eduardo.kotlinudemydelivery.models.Rol
import com.eduardo.kotlinudemydelivery.utils.SharedPref
import com.google.gson.Gson

class ProductsAdapter(val context: Activity, val products: ArrayList<Product>): RecyclerView.Adapter<ProductsAdapter.ProductsViewHolder>() {

    val sharedPref = SharedPref(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cardview_product, parent,false)
        return ProductsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return products.size
    }

    override fun onBindViewHolder(holder: ProductsViewHolder, position: Int) {
        val product = products[position] //cada categoria

        holder.textViewName.text = product.name
        holder.textViewPrice.text = "$${product.price}"
        Glide.with(context).load(product.image1).into(holder.imageViewProduct)

       holder.itemView.setOnClickListener {
           goToDetail(product)
        }
    }

    private fun goToDetail(product: Product){
        val i = Intent(context,ClientProductsDetailActivity::class.java)
        i.putExtra("product",product.toJson())
        context.startActivity(i)
    }

    class ProductsViewHolder(view: View): RecyclerView.ViewHolder(view){

        val textViewName: TextView
        val textViewPrice: TextView
        val imageViewProduct: ImageView

        init {
            textViewName = view.findViewById(R.id.textview_productname)
            textViewPrice = view.findViewById(R.id.textview_productprice)
            imageViewProduct = view.findViewById(R.id.imageview_product)
        }

    }

}