package com.eduardo.kotlinudemydelivery.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.Image
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.eduardo.kotlinudemydelivery.Providers.CategoriesProvider
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.activities.client.products.list.ClientProductsListActivity
import com.eduardo.kotlinudemydelivery.activities.client.shopping_bag.ClientShoppingBagActivity
import com.eduardo.kotlinudemydelivery.fragments.restaurant.categories.RestaurantCategoryListFragment
import com.eduardo.kotlinudemydelivery.models.Category
import com.eduardo.kotlinudemydelivery.models.ResponseHttp
import com.eduardo.kotlinudemydelivery.models.User
import com.eduardo.kotlinudemydelivery.utils.SharedPref
import com.google.gson.Gson
import com.tommasoberlose.progressdialog.ProgressDialogFragment
import org.w3c.dom.Text
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CategoriesListAdapter(val context: Activity, val categories: ArrayList<Category>): RecyclerView.Adapter<CategoriesListAdapter.CategoriesViewHolder>() {

    val sharedPref = SharedPref(context)
    var user: User? = null
    var categoryProvider: CategoriesProvider? = null

    init {
        getUserFromSession()
        categoryProvider = CategoriesProvider(user?.sessionToken!!)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriesViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cardview_category_list, parent,false)
        return CategoriesViewHolder(view)
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    override fun onBindViewHolder(holder: CategoriesViewHolder, position: Int) {
        val category = categories[position] //cada categoria

        holder.textViewCategory.text = category.name
        Glide.with(context).load(category.image).into(holder.imageViewCategory)

       holder.itemView.setOnClickListener {
//           goToProducts(category)
//           var showPopUp = PopUpCategoryFragment()
//           showPopUp.show((context as AppCompatActivity).supportFragmentManager, "showPopUp")
           val builder = AlertDialog.Builder(context)
           val view2 = LayoutInflater.from(context).inflate(R.layout.dialog_category, null)

           builder.setView(view2)

           val dialog = builder.create()
           dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
           dialog.setCanceledOnTouchOutside(false)
           dialog.show()

           val editCategory = view2.findViewById<EditText>(R.id.edittext_category)
           val image = view2.findViewById<ImageView>(R.id.imageview_category)
           val titulo = view2.findViewById<TextView>(R.id.titulo_category2)
           val cerrar = view2.findViewById<ImageView>(R.id.btnCerrarlayout)

            editCategory.setText("${category.name}")
           Glide.with(context).load(category.image).into(image)
           titulo.setText("Modificar Categoria")

           cerrar.setOnClickListener { dialog.dismiss() }
        }

        holder.imageViewDelete.setOnClickListener {
            val positive = { dialog: DialogInterface, which: Int ->
//                Toast.makeText(context, "Lo quiso eliminar", Toast.LENGTH_SHORT).show()
                val imageUrl = category.image
                Log.d("FATAL",imageUrl.toString())
//                categoryProvider?.delete(category.id,)
            }

            val negative = { dialog: DialogInterface, which: Int ->
                Toast.makeText(context, "Lo quiso eliminar pero no", Toast.LENGTH_SHORT).show()
            }

            val builder = AlertDialog.Builder(context)
            builder.setTitle("Categoria: ${category.name}")
            builder.setMessage("Desea eliminar esta categoria?\n*Al eliminar esta categoria, se eliminar los productos de la misma*")
            builder.setPositiveButton(android.R.string.yes){dialog,which ->
                ProgressDialogFragment.showProgressBar(context)
                val imageUrl = category.image
                val partImage = imageUrl?.split("/")
                Log.d("FATAL",partImage?.get(4).toString())
                val image = partImage?.get(4).toString()

                categoryProvider?.delete(category.id.toString(),image)?.enqueue(object : Callback<ResponseHttp>{
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

    private fun goToProducts(category: Category){
        val i = Intent(context,ClientProductsListActivity::class.java)
        i.putExtra("idCategory", category.id)
        context.startActivity(i)
    }

    class CategoriesViewHolder(view: View): RecyclerView.ViewHolder(view){

        val textViewCategory: TextView
        val imageViewCategory: ImageView
        val imageViewDelete: ImageView

        init {
            textViewCategory = view.findViewById(R.id.textview_category_list)
            imageViewCategory = view.findViewById(R.id.imageview_category_list)
            imageViewDelete = view.findViewById(R.id.btn_delete_category)
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
        categories.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeRemoved(position, categories.size)
    }

}