package com.eduardo.kotlinudemydelivery.fragments.restaurant

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.eduardo.kotlinudemydelivery.Providers.CategoriesProvider
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.models.Category
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

class RestaurantCategoryFragment : Fragment() {

    var myView: View? = null

    var imageViewCategory: ImageView? = null
    var editTextCategory: EditText? = null
    var btnCreate: Button? = null

    private var imageFile: File? = null

    var categoriesProvider: CategoriesProvider? = null
    var sharedPref: SharedPref? = null
    var user: User? = null

    val TAG = "CategoryFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        myView = inflater.inflate(R.layout.fragment_restaurant_category, container, false)

        imageViewCategory = myView?.findViewById(R.id.imageview_category)
        editTextCategory = myView?.findViewById(R.id.edittext_category)
        btnCreate = myView?.findViewById(R.id.btn_create)

        imageViewCategory?.setOnClickListener { selectImage() }
        btnCreate?.setOnClickListener { createCategory() }

        sharedPref = SharedPref(requireActivity())

        getUserFromSession()

        categoriesProvider = CategoriesProvider(user?.sessionToken!!)

        return myView
    }

    private fun getUserFromSession(){
        val gson = Gson()
        if (!sharedPref?.getData("user").isNullOrBlank()){
            //si el usuario exite en sesion
            user = gson.fromJson(sharedPref?.getData("user"), User::class.java)
        }
    }

    private fun createCategory(){
        val name = editTextCategory?.text.toString()
        if (imageFile != null){

            val category = Category(name = name)

            ProgressDialogFragment.showProgressBar(requireActivity())

            categoriesProvider?.create(imageFile!!, category)?.enqueue(object : Callback<ResponseHttp> {
                override fun onResponse(
                    call: Call<ResponseHttp>,
                    response: Response<ResponseHttp>
                ) {

                    ProgressDialogFragment.hideProgressBar(requireActivity())

                    Log.d(TAG, "RESPONSE: $response")
                    Log.d(TAG, "BODY: ${response.body()}")

                    Toast.makeText(requireContext(), response.body()?.message, Toast.LENGTH_LONG).show()

                    if (response.body()?.isSuccess == true){
                        clearForm()
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

    private fun clearForm(){
        editTextCategory?.setText("")
        imageFile = null
        imageViewCategory?.setImageResource(R.drawable.ic_image)
    }

    private val startImageForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result: ActivityResult ->

        val resultCode = result.resultCode
        val data = result.data

        if (resultCode == Activity.RESULT_OK){
            val fileUri = data?.data
            imageFile = File(fileUri?.path) // el archivo que vamos a guardar como imagen en el servidor
            imageViewCategory?.setImageURI(fileUri)
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