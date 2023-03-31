package com.eduardo.kotlinudemydelivery.adapters

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.eduardo.kotlinudemydelivery.Providers.ProductsProvider
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.activities.client.home.ClientHomeActivity
import com.eduardo.kotlinudemydelivery.activities.client.products.list.ClientProductsListActivity
import com.eduardo.kotlinudemydelivery.activities.delivery.home.DeliveryHomeActivity
import com.eduardo.kotlinudemydelivery.activities.restaurant.home.RestaurantHomeActivity
import com.eduardo.kotlinudemydelivery.fragments.waiter.WaiterSaleFragment
import com.eduardo.kotlinudemydelivery.models.Category
import com.eduardo.kotlinudemydelivery.models.Product
import com.eduardo.kotlinudemydelivery.models.Rol
import com.eduardo.kotlinudemydelivery.models.User
import com.eduardo.kotlinudemydelivery.utils.SharedPref
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CategoriesSaleAdapter(val context: Activity, val categories: ArrayList<Category>): RecyclerView.Adapter<CategoriesSaleAdapter.CategoriesSaleViewHolder>() {

    val sharedPref = SharedPref(context)
    val fragmentWaiterSale = WaiterSaleFragment()
    val TAG = "CategoriesSale"

    var user: User? = null
    var productsProvider: ProductsProvider? = null
    init {
        getUserFromSession()
        productsProvider = ProductsProvider(user?.sessionToken!!)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriesSaleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cardview_sale_categories, parent,false)
        return CategoriesSaleViewHolder(view)
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    override fun onBindViewHolder(holder: CategoriesSaleViewHolder, position: Int) {
        val category = categories[position] //cada categoria

        holder.textViewCategory.text = category.name
        Glide.with(context).load(category.image).into(holder.imageViewCategory)

       holder.itemView.setOnClickListener {
           //Toast.makeText(context, "${category.id}", Toast.LENGTH_SHORT).show()
//           (context as WaiterSaleFragment).getProducts(category.id!!)
//           fragmentWaiterSale.getProducts(category.id!!)
           productsProvider?.findByCategory(category.id!!)?.enqueue(object : Callback<ArrayList<Product>>{
               override fun onResponse(
                   call: Call<ArrayList<Product>>,
                   response: Response<ArrayList<Product>>
               ) {
                   if (response.body() != null){
                       val products = response.body()!!
                       //Log.d(TAG,"ENTRO ACA ${products}")
                       fragmentWaiterSale.getProducts(products)

                   }
               }

               override fun onFailure(call: Call<ArrayList<Product>>, t: Throwable) {

               }

           })
        }
    }

    private fun goToProducts(category: Category){
        val i = Intent(context,ClientProductsListActivity::class.java)
        i.putExtra("idCategory", category.id)
        context.startActivity(i)
    }

    class CategoriesSaleViewHolder(view: View): RecyclerView.ViewHolder(view){

        val textViewCategory: TextView
        val imageViewCategory: ImageView

        init {
            textViewCategory = view.findViewById(R.id.textview_category_sale)
            imageViewCategory = view.findViewById(R.id.imageview_sale)
        }

    }

    private fun getUserFromSession(){
        val gson = Gson()
        if (!sharedPref?.getData("user").isNullOrBlank()){
            //si el usuario exite en sesion
            user = gson.fromJson(sharedPref?.getData("user"), User::class.java)
        }
    }

}