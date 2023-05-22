package com.eduardo.kotlinudemydelivery.fragments.dialog

import android.app.Activity
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
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.eduardo.kotlinudemydelivery.Providers.CategoriesProvider
import com.eduardo.kotlinudemydelivery.Providers.ProductsProvider
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.fragments.restaurant.products.RestaurantProductListFragment
import com.eduardo.kotlinudemydelivery.models.Category
import com.eduardo.kotlinudemydelivery.models.Product
import com.eduardo.kotlinudemydelivery.models.ResponseHttp
import com.eduardo.kotlinudemydelivery.models.User
import com.eduardo.kotlinudemydelivery.utils.SharedPref
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.gson.Gson
import com.tommasoberlose.progressdialog.ProgressDialogFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File


class productDialog : DialogFragment() {

    var nameProduct: EditText? = null
    var priceProduct: EditText? = null
    var descriptionProduct: EditText? = null
    var imageProduct: ImageView? = null
    var id: String? = null
    var idCategory: String? = null
    var idCategory2: String? = null
    private var spinnerCategories: Spinner? = null
    var categoriesProvider: CategoriesProvider? = null
    var productsProvider: ProductsProvider? = null
    var sharedPref: SharedPref? = null
    var user: User? = null
    var categories = ArrayList<Category>()
    var btnUpdate: Button? = null
    var btnCerrar: ImageView? = null
    var imageString = ""
    private var imageFile: File? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view: View = inflater.inflate(R.layout.fragment_product_dialog, container, false)

        nameProduct = view?.findViewById(R.id.edittext_name)
        priceProduct = view?.findViewById(R.id.edittext_price)
        descriptionProduct = view?.findViewById(R.id.edittext_description)
        imageProduct = view?.findViewById(R.id.imageview_1)
        spinnerCategories = view?.findViewById(R.id.spinner_categories)
        btnUpdate = view?.findViewById(R.id.btn_create_product)
        btnCerrar = view?.findViewById(R.id.btnCerrarlayout)

        sharedPref = SharedPref(requireActivity())
        getUserFromSession()
        categoriesProvider = CategoriesProvider(user?.sessionToken!!)
        productsProvider = ProductsProvider(user?.sessionToken!!)

        arguments?.let {
            id = it.getString("id")
            idCategory2 = it.getString("idCategory")
            nameProduct?.setText(it.getString("name"))
            priceProduct?.setText(it.getString("price"))
            descriptionProduct?.setText(it.getString("description"))
            Glide.with(requireContext()).load(it.getString("image")).into(imageProduct!!)
            btnUpdate?.setText("Modificar")
            imageString=it.getString("image")!!
        }
        getCategories()

        dialog?.window?.setBackgroundDrawableResource(R.drawable.card_radius2)

        imageProduct?.setOnClickListener { selectImage() }

        btnCerrar?.setOnClickListener { dismiss() }

        btnUpdate?.setOnClickListener {
            val price = priceProduct?.text.toString()
            val product = Product(
                id = id,
                name = nameProduct?.text.toString(),
                description = descriptionProduct?.text.toString(),
                price = price.toDouble(),
                idCategory = idCategory.toString(),

                )
            ProgressDialogFragment.showProgressBar(requireActivity())
            if (imageFile != null){
                val partImage = imageString.split("/")
                val image = partImage?.get(4).toString()

                productsProvider?.update(image,imageFile!!, product)?.enqueue(object: Callback<ResponseHttp>{
                    override fun onResponse(
                        call: Call<ResponseHttp>,
                        response: Response<ResponseHttp>
                    ) {
                        if (response.body() != null){
                            ProgressDialogFragment.hideProgressBar(requireActivity())
                            Toast.makeText(requireContext(), response.body()?.message, Toast.LENGTH_LONG).show()

                            val frag: RestaurantProductListFragment = fragmentManager?.findFragmentById(R.id.container) as RestaurantProductListFragment
                            frag.getProducts()
                            dismiss()
                        }
                    }

                    override fun onFailure(call: Call<ResponseHttp>, t: Throwable) {
                        Log.d("FATAL", "Error: ${t.message}")
                        ProgressDialogFragment.hideProgressBar(requireActivity())
                        Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                })
            }else{
                productsProvider?.updateWithOutImage(product)?.enqueue(object : Callback<ResponseHttp>{
                    override fun onResponse(
                        call: Call<ResponseHttp>,
                        response: Response<ResponseHttp>
                    ) {
                        if (response.body() != null){
                            ProgressDialogFragment.hideProgressBar(requireActivity())
                            Toast.makeText(requireContext(), response.body()?.message, Toast.LENGTH_LONG).show()

                            val frag: RestaurantProductListFragment = fragmentManager?.findFragmentById(R.id.container) as RestaurantProductListFragment
                            frag.getProducts()
                            dismiss()
                        }
                    }

                    override fun onFailure(call: Call<ResponseHttp>, t: Throwable) {
                        Log.d("FATAL", "Error: ${t.message}")
                        ProgressDialogFragment.hideProgressBar(requireActivity())
                        Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_LONG).show()
                    }

                })
            }
        }
        return view
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
//                            Log.d(TAG,"id Category: $idCategory")
                        }

                        override fun onNothingSelected(p0: AdapterView<*>?) {

                        }
                    }
                    var contador = 1
                    var existe = 0
                    categories.forEach {
                        if(it.id != idCategory2){
                            contador += 1
                        }else{
                            existe = contador - 1
                        }
                    }
                    spinnerCategories?.setSelection(existe)
                }
            }

            override fun onFailure(call: Call<ArrayList<Category>>, t: Throwable) {
//                Log.d(TAG, "Error: ${t.message}")
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getUserFromSession(){
        val gson = Gson()
        if (!sharedPref?.getData("user").isNullOrBlank()){
            //si el usuario exite en sesion
            user = gson.fromJson(sharedPref?.getData("user"), User::class.java)
        }
    }

    val startImageForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result: ActivityResult ->
        val resultCode = result.resultCode
        val data = result.data

        if (resultCode == Activity.RESULT_OK){
            val fileUri = data?.data
            imageFile = File(fileUri?.path) // el archivo que vamos a guardar como imagen en el servidor
            imageProduct?.setImageURI(fileUri)
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

}