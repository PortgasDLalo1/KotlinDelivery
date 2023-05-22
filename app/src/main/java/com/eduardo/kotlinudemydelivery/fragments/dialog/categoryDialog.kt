package com.eduardo.kotlinudemydelivery.fragments.dialog

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.eduardo.kotlinudemydelivery.Providers.CategoriesProvider
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.fragments.restaurant.categories.RestaurantCategoryListFragment
import com.eduardo.kotlinudemydelivery.models.Category
import com.eduardo.kotlinudemydelivery.models.ResponseHttp
import com.eduardo.kotlinudemydelivery.models.User
import com.eduardo.kotlinudemydelivery.utils.SharedPref
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.tommasoberlose.progressdialog.ProgressDialogFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class categoryDialog: DialogFragment() {
//    var view: View ? = null
    var image : ImageView? = null
    var nameEdit : TextInputEditText? = null
    var btnAddUpdate : Button? = null
    var btnCerrar : ImageView? = null
    private var imageFile: File? = null
    var id: String? = ""
    var categoriesProvider: CategoriesProvider? = null
    var user: User? = null
    var sharedPref: SharedPref? = null
    private lateinit var frag : RestaurantCategoryListFragment
    var imageString = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        return super.onCreateView(inflater, container, savedInstanceState)

        var view: View = inflater.inflate(R.layout.dialog_category, container, false)
        image = view?.findViewById(R.id.imageview_category)
        nameEdit = view?.findViewById(R.id.edittext_category)
        btnCerrar = view?.findViewById(R.id.btnCerrarlayout)
        btnAddUpdate = view?.findViewById(R.id.btn_create_category)
        sharedPref = SharedPref(requireActivity())

        getUserFromSession()
        categoriesProvider = CategoriesProvider(user?.sessionToken!!)

        arguments?.let {
            id=it.getString("id")
            nameEdit?.setText(it.getString("name"))
            Glide.with(requireContext()).load(it.getString("image")).into(image!!)
            btnAddUpdate?.setText("modificar")
            imageString=it.getString("image")!!
        }

        image?.setOnClickListener { selectImage() }

        btnCerrar?.setOnClickListener { dismiss() }

        dialog?.window?.setBackgroundDrawableResource(R.drawable.card_radius2)

        btnAddUpdate?.setOnClickListener {
            ProgressDialogFragment.showProgressBar(requireActivity())
            if (imageFile != null){
                val partImage = imageString.split("/")
                val image = partImage?.get(4).toString()

                val category = Category(
                    id = id,
                    name = nameEdit?.text.toString()
                )

                categoriesProvider?.update(image,imageFile!!,category)?.enqueue(object: Callback<ResponseHttp>{
                    override fun onResponse(
                        call: Call<ResponseHttp>,
                        response: Response<ResponseHttp>
                    ) {
                        if (response.body() != null){
                            ProgressDialogFragment.hideProgressBar(requireActivity())
                            Toast.makeText(requireContext(), response.body()?.message, Toast.LENGTH_LONG).show()
                            //llamar una funcion de otro fragment dentro del contenedor
                            var frag: RestaurantCategoryListFragment = fragmentManager?.findFragmentById(R.id.container) as RestaurantCategoryListFragment
                            frag.getCategories()
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
                categoriesProvider?.updateWithOutImage(id.toString(),nameEdit?.text.toString())?.enqueue(object: Callback<ResponseHttp>{
                    override fun onResponse(
                        call: Call<ResponseHttp>,
                        response: Response<ResponseHttp>
                    ) {
                        if (response.body() != null){
                            ProgressDialogFragment.hideProgressBar(requireActivity())
                            Toast.makeText(requireContext(), response.body()?.message, Toast.LENGTH_LONG).show()
                            //llamar una funcion de otro fragment dentro del contenedor
                            var frag: RestaurantCategoryListFragment = fragmentManager?.findFragmentById(R.id.container) as RestaurantCategoryListFragment
                            frag.getCategories()
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

//        Toast.makeText(requireContext(), "$imageFile", Toast.LENGTH_SHORT).show()
        return view
    }

    val startImageForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result: ActivityResult ->
        val resultCode = result.resultCode
        val data = result.data

        if (resultCode == Activity.RESULT_OK){
            val fileUri = data?.data
            imageFile = File(fileUri?.path) // el archivo que vamos a guardar como imagen en el servidor
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

    private fun getUserFromSession(){
        val gson = Gson()
        if (!sharedPref?.getData("user").isNullOrBlank()){
            //si el usuario exite en sesion
            user = gson.fromJson(sharedPref?.getData("user"), User::class.java)
        }
    }
}