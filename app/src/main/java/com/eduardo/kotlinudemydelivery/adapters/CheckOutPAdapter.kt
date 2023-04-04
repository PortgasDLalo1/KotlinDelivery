package com.eduardo.kotlinudemydelivery.adapters

import android.app.Activity
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.activities.client.orders.checkout.ClientOrderCheckOutActivity
import com.eduardo.kotlinudemydelivery.models.Product

class CheckOutPAdapter(val context: Activity, val products: ArrayList<Product>): RecyclerView.Adapter<CheckOutPAdapter.CheckOutPViewHolder>() {

    init {
        (context as ClientOrderCheckOutActivity).setTotalCheck(getTotal())
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckOutPViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cardview_products_check, parent,false)
        return CheckOutPViewHolder(view)
    }

    override fun onBindViewHolder(holder: CheckOutPViewHolder, position: Int) {
        val product = products[position]

        Glide.with(context).load(product.image1).into(holder.imageViewProduct)
        holder.textViewProduct.text = product.name
        holder.textViewDescription.text = product.description
        holder.textViewQuantity.text = "x${product.quantity}"
        holder.textViewPrice.text = "$ ${product.price * product.quantity!!} MX"
    }

    override fun getItemCount(): Int {
        return products.size
    }

    class CheckOutPViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val imageViewProduct: ImageView
        val textViewProduct: TextView
        val textViewDescription: TextView
        val textViewQuantity: TextView
        val textViewPrice: TextView

        init {
            imageViewProduct = view.findViewById(R.id.imageview_productcheck)
            textViewProduct = view.findViewById(R.id.textViewNameProductRecyclerCheck)
            textViewDescription = view.findViewById(R.id.textViewNameDescriptionProductRecyclerCheck)
            textViewQuantity = view.findViewById(R.id.textViewQuantityRecyclerCheck)
            textViewPrice = view.findViewById(R.id.textViewPriceRecyclerCheck)
            textViewDescription.movementMethod = ScrollingMovementMethod()
        }
    }

    private fun getTotal(): Double{
        var total = 0.0
        for (p in products){
            if (p.quantity != null) {
                total = total + (p.quantity!! * p.price)
            }
        }
        return total
    }

}