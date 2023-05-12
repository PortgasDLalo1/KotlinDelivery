package com.eduardo.kotlinudemydelivery.fragments.restaurant.categories

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.eduardo.kotlinudemydelivery.Providers.CategoriesProvider
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.adapters.CategoriesAdapter
import com.eduardo.kotlinudemydelivery.adapters.CategoriesListAdapter
import com.eduardo.kotlinudemydelivery.models.Category
import com.eduardo.kotlinudemydelivery.models.ResponseHttp
import com.eduardo.kotlinudemydelivery.models.Sucursales
import com.eduardo.kotlinudemydelivery.models.User
import com.eduardo.kotlinudemydelivery.utils.SharedPref
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.tommasoberlose.progressdialog.ProgressDialogFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class RestaurantCategoryListFragment : Fragment() {

    var myView: View? = null
    val TAG = "CatListFragment"
    var recyclerViewCategories: RecyclerView? = null
    var floatingButton: FloatingActionButton? = null
    var toolbar: Toolbar? = null
    var user: User? = null
    var sucursal: Sucursales? = null
    var sharedPref: SharedPref? = null
    var categories = ArrayList<Category>()
    var adapter: CategoriesListAdapter? = null
    var categoriesProvider: CategoriesProvider? = null

    var linearCategory: CardView? = null
    var abrirFormCategory: TextView? = null
    var btnCerrarlayout: ImageView? = null
    var image: ImageView? = null
    var edittext_category: EditText? = null
    var dialog: AlertDialog? = null
    var editCategory: EditText? = null
    var height = 0

    private var imageFile: File? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        myView = inflater.inflate(R.layout.fragment_restaurant_category_list, container, false)

        recyclerViewCategories = myView?.findViewById(R.id.recyclerview_categories_list)
        linearCategory = myView?.findViewById(R.id.linearCategory)
        abrirFormCategory = myView?.findViewById(R.id.abrirFormCategory)
        btnCerrarlayout = myView?.findViewById(R.id.btnCerrarlayout)
//        floatingButton = myView?.findViewById(R.id.fab_category_create)

        toolbar = myView?.findViewById(R.id.toolbar)
        toolbar?.setTitleTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        toolbar?.title = "Categorias"
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        sharedPref = SharedPref(requireActivity())

        getUserFromSession()
        getSucursalFromSession()
        categoriesProvider = CategoriesProvider(user?.sessionToken!!)
        getCategories()

        abrirFormCategory?.setOnClickListener { showDialog() }
        return myView
    }

    private fun showDialog(){
        val builder = AlertDialog.Builder(requireContext())
        val view2 = layoutInflater.inflate(R.layout.dialog_category,null)

        builder.setView(view2)
        dialog = builder.create()
        dialog?.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.show()

        editCategory = view2.findViewById<EditText>(R.id.edittext_category)
        image = view2.findViewById<ImageView>(R.id.imageview_category)
        val titulo = view2.findViewById<TextView>(R.id.titulo_category2)
        val cerrar = view2.findViewById<ImageView>(R.id.btnCerrarlayout)
        val agregar = view2.findViewById<Button>(R.id.btn_create_category)

        cerrar.setOnClickListener { dialog?.dismiss() }
        image?.setOnClickListener { selectImage() }
        agregar.setOnClickListener { createCategory(editCategory?.text.toString()) }
    }


    private fun createCategory(name: String){
        if (imageFile != null){
            val category = Category(name = name)
            ProgressDialogFragment.showProgressBar(requireActivity())

            categoriesProvider?.create(imageFile!!, category)?.enqueue(object : Callback<ResponseHttp>{
                override fun onResponse(
                    call: Call<ResponseHttp>,
                    response: Response<ResponseHttp>
                ) {
                    ProgressDialogFragment.hideProgressBar(requireActivity())
                    Toast.makeText(requireContext(), response.body()?.message, Toast.LENGTH_LONG).show()
                    if (response.body()?.isSuccess == true){
                        clearForm()
                        getCategories()
                        dialog?.dismiss()
                    }
                }

                override fun onFailure(call: Call<ResponseHttp>, t: Throwable) {
                    Log.d(TAG, "Error: ${t.message}")
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_LONG).show()
                }

            })
        }else{
            Toast.makeText(requireContext(),"Selecciona una imagen",Toast.LENGTH_LONG).show()
        }
    }
    fun getCategories(){

        categoriesProvider?.getAll()?.enqueue(object: Callback<ArrayList<Category>> {
            override fun onResponse(
                call: Call<ArrayList<Category>>,
                response: Response<ArrayList<Category>>
            ) {
                if (response.body() != null){
                    categories = response.body()!!
                    Log.d(TAG, "categories: $categories")
                    adapter = CategoriesListAdapter(requireActivity(),categories)
                    recyclerViewCategories?.adapter = adapter
                    Log.d("FATAL","entro")
                }
            }

            override fun onFailure(call: Call<ArrayList<Category>>, t: Throwable) {
                Log.d(TAG, "Error: ${t.message}")
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }

        })
    }

    val startImageForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result: ActivityResult ->

        val resultCode = result.resultCode
        val data = result.data

        if (resultCode == Activity.RESULT_OK){
            val fileUri = data?.data
            imageFile = File(fileUri?.path) // el archivo que vamos a guardar como imagen en el servidor
//            imageview_category?.setImageURI(fileUri)
            image?.setImageURI(fileUri)
        }
        else if (resultCode == ImagePicker.RESULT_ERROR){
            Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_LONG).show()
        }
        else {
            Toast.makeText(requireContext(), "La tarea se cancelo", Toast.LENGTH_LONG).show()
        }

    }

    private fun selectImage(){
        ImagePicker.with(this)
            .crop()
            .compress(1024)
            .maxResultSize(1080,1080)
            .createIntent { intent ->
                startImageForResult.launch(intent)
            }
    }

    private fun clearForm(){
//        edittext_category?.setText("")
        editCategory?.setText("")
        imageFile = null
//        imageview_category?.setImageResource(R.drawable.ic_image)
        image?.setImageResource(R.drawable.ic_image)
    }

    private fun getUserFromSession(){
        val gson = Gson()
        if (!sharedPref?.getData("user").isNullOrBlank()){
            //si el usuario exite en sesion
            user = gson.fromJson(sharedPref?.getData("user"), User::class.java)
        }
    }

    private fun getSucursalFromSession(){
        val gson = Gson()
        if (!sharedPref?.getData("sucursal").isNullOrBlank()){
            sucursal = gson.fromJson(sharedPref?.getData("sucursal"), Sucursales::class.java)
        }
    }


}