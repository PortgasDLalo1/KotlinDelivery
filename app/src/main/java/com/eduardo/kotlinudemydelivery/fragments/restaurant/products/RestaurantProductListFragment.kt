package com.eduardo.kotlinudemydelivery.fragments.restaurant.products

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.eduardo.kotlinudemydelivery.Providers.CategoriesProvider
import com.eduardo.kotlinudemydelivery.Providers.ProductsProvider
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.adapters.ProductRestaurantListAdapter
import com.eduardo.kotlinudemydelivery.databinding.FragmentRestaurantProductListBinding
import com.eduardo.kotlinudemydelivery.models.Category
import com.eduardo.kotlinudemydelivery.models.Product
import com.eduardo.kotlinudemydelivery.models.ResponseHttp
import com.eduardo.kotlinudemydelivery.models.User
import com.eduardo.kotlinudemydelivery.utils.SharedPref
import com.faltenreich.skeletonlayout.Skeleton
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.gson.Gson
import com.tommasoberlose.progressdialog.ProgressDialogFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class RestaurantProductListFragment : Fragment() {

//    var myView: View? = null
//    var recyclerProduct: RecyclerView? = null
    var toolbar: Toolbar? = null
//    var abrirFormProduct: TextView? = null
    var user: User? = null
    var products = ArrayList<Product>()
    var productsProvider: ProductsProvider? = null
    var sharedPref: SharedPref? = null
    var adapter: ProductRestaurantListAdapter? = null
    private lateinit var skeleton: Skeleton
    var dialog: AlertDialog? = null
    private var spinnerCategories: Spinner? = null
    var categoriesProvider: CategoriesProvider? = null
    var categories = ArrayList<Category>()
    var idCategory = ""
    private var imageFile: File? = null
    var image: ImageView? = null

    var nameProduct: EditText? = null
    var priceProduct: EditText? = null
    var descriptionProduct: EditText? = null

    private var _binding: FragmentRestaurantProductListBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentRestaurantProductListBinding.inflate(inflater, container, false)
//        myView = inflater.inflate(R.layout.fragment_restaurant_product_list, container, false)


        sharedPref = SharedPref(requireActivity())
        getUserFromSession()
        productsProvider = ProductsProvider(user?.sessionToken!!)
        categoriesProvider = CategoriesProvider(user?.sessionToken!!)
        getProducts()

        binding.abrirFormProduct?.setOnClickListener { showDialogProduct() }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showDialogProduct(){
        var builder = AlertDialog.Builder(requireContext())
        val view2 = layoutInflater.inflate(R.layout.fragment_product_dialog, null)

        builder.setView(view2)
        dialog = builder.create()
        dialog?.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.show()

        spinnerCategories = view2.findViewById(R.id.spinner_categories)
        image = view2.findViewById(R.id.imageview_1)
        val cerrar = view2.findViewById<ImageView>(R.id.btnCerrarlayout)
        val addProduct = view2.findViewById<Button>(R.id.btn_create_product)
        nameProduct = view2.findViewById(R.id.edittext_name)
        priceProduct = view2.findViewById(R.id.edittext_price)
        descriptionProduct = view2.findViewById(R.id.edittext_description)

        cerrar?.setOnClickListener { dialog?.dismiss() }
        image?.setOnClickListener { selectImage() }
        addProduct.setOnClickListener {
            val name = nameProduct?.text.toString()
            val description = descriptionProduct?.text.toString()
            val priceText = priceProduct?.text.toString()
            if (isValidForm(name,description,priceText)){
                val productModel = Product(
                    name = name,
                    description = description,
                    price = priceText.toDouble(),
                    idCategory = idCategory
                )

                createProduct(productModel)
            }
        }

        getCategories()
    }

    private fun createProduct(product: Product){
        ProgressDialogFragment.showProgressBar(requireActivity())

        productsProvider?.create2(imageFile!!, product)?.enqueue(object: Callback<ResponseHttp>{
            override fun onResponse(call: Call<ResponseHttp>, response: Response<ResponseHttp>) {
                ProgressDialogFragment.hideProgressBar(requireActivity())
                if (response.body()?.isSuccess == true){
                    dialog?.dismiss()
                    Toast.makeText(requireContext(), response.body()?.message, Toast.LENGTH_LONG).show()
                    getProducts()
                }
            }

            override fun onFailure(call: Call<ResponseHttp>, t: Throwable) {
                ProgressDialogFragment.hideProgressBar(requireActivity())

                Log.d("FATAL", "Error: ${t.message}")
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_LONG).show()

            }

        })
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
                }
            }

            override fun onFailure(call: Call<ArrayList<Category>>, t: Throwable) {
//                Log.d(TAG, "Error: ${t.message}")
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    fun getProducts(){
        productsProvider?.getAll()?.enqueue(object : Callback<ArrayList<Product>>{
            override fun onResponse(
                call: Call<ArrayList<Product>>,
                response: Response<ArrayList<Product>>
            ) {
                if (response.body() != null ){
                    products = response.body()!!
                    adapter = ProductRestaurantListAdapter(requireActivity(), products)
                    binding.recyclerviewProductsList?.adapter = adapter
                    binding.shimmer?.isVisible = false
                    binding.recyclerviewProductsList.isVisible = true
                }
            }

            override fun onFailure(call: Call<ArrayList<Product>>, t: Throwable) {

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

        if (imageFile == null){
            Toast.makeText(requireContext(), "selecciona la imagen 1", Toast.LENGTH_SHORT).show()
            return false
        }
        if (idCategory.isNullOrBlank()){
            Toast.makeText(requireContext(), "selecciona la categoria del producto", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

}