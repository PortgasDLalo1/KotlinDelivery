package com.eduardo.kotlinudemydelivery.fragments.restaurant

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.eduardo.kotlinudemydelivery.Providers.CategoriesProvider
import com.eduardo.kotlinudemydelivery.Providers.ProductsProvider
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.adapters.CategoriesAdapter
import com.eduardo.kotlinudemydelivery.models.Category
import com.eduardo.kotlinudemydelivery.models.Product
import com.eduardo.kotlinudemydelivery.models.ResponseHttp
import com.eduardo.kotlinudemydelivery.models.User
import com.eduardo.kotlinudemydelivery.utils.SharedPref
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class RestaurantProductFragment : Fragment() {

    var myView: View? = null
    var editTextName: EditText? = null
    var editTextDescription: EditText? = null
    var editTextPrice: EditText? = null

    var imageView1: ImageView? = null
    var imageView2: ImageView? = null
    var imageView3: ImageView? = null

    var btnCreate: Button? = null

    private var imageFile1: File? = null
    private var imageFile2: File? = null
    private var imageFile3: File? = null

    private var spinnerCategories: Spinner? = null

    var user: User? = null
    var sharedPref: SharedPref? = null
    var categories = ArrayList<Category>()
    var categoriesProvider: CategoriesProvider? = null
    var productsProvider: ProductsProvider? = null
    var idCategory = ""

    val TAG = "ProductoFRagment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        myView = inflater.inflate(R.layout.fragment_restaurant_product, container, false)

        editTextName = myView?.findViewById(R.id.edittext_name)
        editTextDescription = myView?.findViewById(R.id.edittext_description)
        editTextPrice = myView?.findViewById(R.id.edittext_price)

        imageView1 = myView?.findViewById(R.id.imageview_1)
        imageView2 = myView?.findViewById(R.id.imageview_2)
        imageView3 = myView?.findViewById(R.id.imageview_3)

        btnCreate = myView?.findViewById(R.id.btn_create)

        spinnerCategories = myView?.findViewById(R.id.spinner_categories)

        btnCreate?.setOnClickListener { createProduct() }
        imageView1?.setOnClickListener { selectImage(101) }
        imageView2?.setOnClickListener { selectImage(102) }
        imageView3?.setOnClickListener { selectImage(103) }

        sharedPref = SharedPref(requireActivity())

        getUserFromSession()

        categoriesProvider = CategoriesProvider(user?.sessionToken!!)
        productsProvider = ProductsProvider(user?.sessionToken!!)

        getCategories()

        return myView
    }

    private fun createProduct(){
        val name = editTextName?.text.toString()
        val description = editTextDescription?.text.toString()
        val priceText = editTextPrice?.text.toString()

        val files = ArrayList<File>()

        if (isValidForm(name,description,priceText)){

            val product = Product(
                name = name,
                description = description,
                price = priceText.toDouble(),
                idCategory = idCategory
            )

            files.add(imageFile1!!)
            files.add(imageFile2!!)
            files.add(imageFile3!!)

            productsProvider?.create(files, product)?.enqueue(object: Callback<ResponseHttp>{
                override fun onResponse(
                    call: Call<ResponseHttp>,
                    response: Response<ResponseHttp>
                ) {
                    Log.d(TAG, "Reponse: $response")
                    Log.d(TAG, "Body: ${response.body()}")
                    Toast.makeText(requireContext(), response.body()?.message, Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(call: Call<ResponseHttp>, t: Throwable) {
                    Log.d(TAG, "Error: ${t.message}")
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    private fun isValidForm(name: String, description: String, price: String): Boolean {
        if (name.isNullOrBlank()){
            Toast.makeText(requireContext(), "Ingresa el nombre del producto", Toast.LENGTH_SHORT).show()
            return false
        }
        else if (description.isNullOrBlank()){
            Toast.makeText(requireContext(), "Ingresa la descripcion del producto", Toast.LENGTH_SHORT).show()
            return false
        }
        else if (price.isNullOrBlank()){
            Toast.makeText(requireContext(), "Ingresa el precio del producto", Toast.LENGTH_SHORT).show()
            return false
        }

        if (imageFile1 == null){
            Toast.makeText(requireContext(), "selecciona la imagen 1", Toast.LENGTH_SHORT).show()
            return false
        }
        if (imageFile2 == null){
            Toast.makeText(requireContext(), "selecciona la imagen 2", Toast.LENGTH_SHORT).show()
            return false
        }
        if (imageFile3 == null){
            Toast.makeText(requireContext(), "selecciona la imagen 3", Toast.LENGTH_SHORT).show()
            return false
        }

        if (idCategory.isNullOrBlank()){
            Toast.makeText(requireContext(), "selecciona la categoria del producto", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun getCategories(){
        categoriesProvider?.getAll()?.enqueue(object: Callback<ArrayList<Category>> {
            override fun onResponse(
                call: Call<ArrayList<Category>>,
                response: Response<ArrayList<Category>>
            ) {
                if (response.body() != null){
                    categories = response.body()!!
                    val arrayAdapter = ArrayAdapter<Category>(requireActivity(), android.R.layout.simple_dropdown_item_1line, categories)
                    spinnerCategories?.adapter = arrayAdapter
                    spinnerCategories?.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
                        override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, position: Int, l: Long) {
                            idCategory = categories[position].id!!
                            Log.d(TAG,"id Category: $idCategory")
                        }

                        override fun onNothingSelected(p0: AdapterView<*>?) {

                        }
                    }
                }
            }

            override fun onFailure(call: Call<ArrayList<Category>>, t: Throwable) {
                Log.d(TAG, "Error: ${t.message}")
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val fileUri = data?.data
            if (requestCode == 101){
                imageFile1 = File(fileUri?.path) // el archivo que vamos a guardar como imagen en el servidor
                imageView1?.setImageURI(fileUri)
            }
            else if (requestCode == 102){
                imageFile2 = File(fileUri?.path) // el archivo que vamos a guardar como imagen en el servidor
                imageView2?.setImageURI(fileUri)
            }
            else if (requestCode == 103){
                imageFile3 = File(fileUri?.path) // el archivo que vamos a guardar como imagen en el servidor
                imageView3?.setImageURI(fileUri)
            }

        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Task Cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun selectImage(requestCode: Int){
        ImagePicker.with(this)
            .crop()
            .compress(1024)
            .maxResultSize(1080,1080)
            .start(requestCode)
    }

    private fun getUserFromSession(){
        val gson = Gson()
        if (!sharedPref?.getData("user").isNullOrBlank()){
            //si el usuario exite en sesion
            user = gson.fromJson(sharedPref?.getData("user"), User::class.java)
        }
    }
}