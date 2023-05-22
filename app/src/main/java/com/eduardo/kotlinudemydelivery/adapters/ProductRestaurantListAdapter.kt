package com.eduardo.kotlinudemydelivery.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.eduardo.kotlinudemydelivery.Providers.ProductsProvider
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.fragments.dialog.productDialog
import com.eduardo.kotlinudemydelivery.models.Product
import com.eduardo.kotlinudemydelivery.models.ResponseHttp
import com.eduardo.kotlinudemydelivery.models.User
import com.eduardo.kotlinudemydelivery.utils.SharedPref
import com.google.gson.Gson
import com.tommasoberlose.progressdialog.ProgressDialogFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProductRestaurantListAdapter(val context: Activity, val products: ArrayList<Product>): RecyclerView.Adapter<ProductRestaurantListAdapter.ProductRestautanteViewHolder>() {
    val sharedPref = SharedPref(context)
    var user: User? = null
    var productProvider: ProductsProvider? = null

    init {
        getUserFromSession()
        productProvider = ProductsProvider(user?.sessionToken!!)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductRestautanteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cardview_product, parent, false)
        return ProductRestautanteViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ProductRestautanteViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        holder.imageViewDelete.visibility = View.VISIBLE
        val product = products[position]

        holder.textViewName.text = product.name
        holder.textViewPrice.text = "$${product.price}"
        Glide.with(context).load(product.image1).into(holder.imageViewProduct)

        holder.itemView.setOnClickListener {
            val df = productDialog()
            val f = (context as AppCompatActivity).supportFragmentManager
            val bundle = Bundle()
            bundle.putString("id", product.id)
            bundle.putString("name", product.name)
            bundle.putString("price", product.price.toString())
            bundle.putString("description", product.description)
            bundle.putString("image", product.image1)
            bundle.putString("idCategory", product.idCategory)

            df.arguments = bundle
            df.isCancelable = false
            df.show(f,"dialogProduct")

        }

        holder.imageViewDelete.setOnClickListener {
            val negative = { dialog: DialogInterface, which: Int ->
                Toast.makeText(context, "Lo quiso eliminar pero no", Toast.LENGTH_SHORT).show()
            }

            val builder = AlertDialog.Builder(context)
            builder.setTitle("Producto: ${product.name}")
            builder.setMessage("Desea eliminar este producto?")
            builder.setPositiveButton(android.R.string.yes){dialog, which ->
                ProgressDialogFragment.showProgressBar(context)
                val imageUrl = product.image1
                val partImage = imageUrl?.split("/")
                val image = partImage?.get(4).toString()

                productProvider?.delete(product.id.toString(),image)?.enqueue(object: Callback<ResponseHttp>{
                    override fun onResponse(
                        call: Call<ResponseHttp>,
                        response: Response<ResponseHttp>
                    ) {
                        if (response.body() != null){
                            deleteItem(position)
                            ProgressDialogFragment.hideProgressBar(context)
                            Toast.makeText(context, response.body()?.message, Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<ResponseHttp>, t: Throwable) {
                        ProgressDialogFragment.hideProgressBar(context)
                        Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                    }

                })
            }
            builder.setNegativeButton(android.R.string.no, negative)

            val dialog = builder.create()
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()

            val button1 = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
            with(button1){
                setBackgroundColor(Color.GREEN)
                setPadding(20,0,20,0)
                setTextColor(Color.WHITE)
            }

            val button2 = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
            with(button2){
                setPadding(20,0,20,0)
                setTextColor(Color.RED)
            }
        }
    }

    override fun getItemCount(): Int {
        return products.size
    }

    class ProductRestautanteViewHolder(view: View): RecyclerView.ViewHolder(view){
        val textViewName: TextView
        val textViewPrice: TextView
        val imageViewProduct: ImageView
        val imageViewDelete: ImageView

        init {
            textViewName = view.findViewById(R.id.textview_productname)
            textViewPrice = view.findViewById(R.id.textview_productprice)
            imageViewProduct = view.findViewById(R.id.imageview_product)
            imageViewDelete = view.findViewById(R.id.btn_delete_product)
        }
    }

    private fun getUserFromSession(){
        val gson = Gson()
        if (!sharedPref?.getData("user").isNullOrBlank()){
            //si el usuario exite en sesion
            user = gson.fromJson(sharedPref?.getData("user"), User::class.java)
        }
    }

    private fun deleteItem(position: Int){
        products.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeRemoved(position, products.size)
    }
}