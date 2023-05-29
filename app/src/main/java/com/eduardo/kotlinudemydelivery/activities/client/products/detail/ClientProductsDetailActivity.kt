package com.eduardo.kotlinudemydelivery.activities.client.products.detail

import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.databinding.ActivityClientProductsDetailBinding
import com.eduardo.kotlinudemydelivery.models.Product
import com.eduardo.kotlinudemydelivery.utils.SharedPref
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ClientProductsDetailActivity : AppCompatActivity() {

    var product: Product? = null
    val gson = Gson()
    private lateinit var binding: ActivityClientProductsDetailBinding

    var counter = 1
    var productPrice = 0.0

    var sharedPref: SharedPref? = null
    var selectedProducts = ArrayList<Product>()

    val TAG = "ProductsDetail"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientProductsDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        product = gson.fromJson(intent.getStringExtra("product"), Product::class.java)
        sharedPref = SharedPref(this)

        val imageList = ArrayList<SlideModel>()
        imageList.add(SlideModel(product?.image1, ScaleTypes.CENTER_CROP))
        imageList.add(SlideModel(product?.image2, ScaleTypes.CENTER_CROP))
        imageList.add(SlideModel(product?.image3, ScaleTypes.CENTER_CROP))

//        binding.imagesalider.setImageList(imageList)
        Glide.with(this).load(product?.image1!!).into(binding.imageProduct)
        binding.toolbarProducts.title = "${product?.name}"
        binding.toolbarProducts.setTitleTextColor(ContextCompat.getColor(this,R.color.black))
        setSupportActionBar(binding.toolbarProducts)
        binding.toolbarProducts.setNavigationOnClickListener { finish() }
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        binding.textviewProductlistname.text = product?.name
        binding.textviewProductlistdescription.text = product?.description
        binding.textviewPrice.text = "$ ${product?.price}"

        binding.imageviewAdd.setOnClickListener { addItem() }
        binding.imageviewRemove.setOnClickListener { removeItem() }
        binding.btnAddProduct.setOnClickListener { addToBag() }

        getProductsFromSharedPref()
    }

    private fun addToBag(){
        val index = getIndexOf(product?.id!!) // indice del producto si es que existe en shared pref

        if (index == -1){ // este producto no existe en shared pref
            if (product?.quantity == null){
                product?.quantity = 1
            }
            selectedProducts.add(product!!)
        } else { // si ya existe el producto en shared pref, debemos editar la cantidad
            selectedProducts[index].quantity = counter
        }

        sharedPref?.save("order",selectedProducts)
        Toast.makeText(this, "Producto Agregado", Toast.LENGTH_LONG).show()
        Log.d(TAG,"Productosssss: ${product}")
    }

    private fun getProductsFromSharedPref(){
        if (!sharedPref?.getData("order").isNullOrBlank()){ // si existe una orden en sharedpref
            val type = object: TypeToken<ArrayList<Product>>() {}.type
            selectedProducts = gson.fromJson(sharedPref?.getData("order"), type)
            val index = getIndexOf(product?.id!!)

            if (index != -1){
                product?.quantity = selectedProducts[index].quantity
                binding.textviewCounter.text = "${product?.quantity}"
                productPrice = product?.price!! * product?.quantity!!
                binding.textviewPrice.text = "$ ${productPrice}"
                binding.btnAddProduct.setText("Editar Producto")
                binding.btnAddProduct.backgroundTintList = ColorStateList.valueOf(Color.RED)
                counter = product?.quantity!!
            }

            for (p in selectedProducts){
                Log.d(TAG, "Shared pref: $p")
            }
        }
    }

    // para comparar si un producto ya existe en shared pref y asi solo editar la cantidad del producto seleccionado
    private fun getIndexOf(idProduct: String): Int{
        var pos = 0
        for (p in selectedProducts){
            if (p.id == idProduct){
                return pos
            }
            pos ++
        }
        return -1
    }

    private fun addItem(){
        counter ++
        productPrice = product?.price!! * counter
        product?.quantity = counter
        binding.textviewCounter.text = "${product?.quantity}"
        binding.textviewPrice.text = "$ ${productPrice}"
    }

    private fun removeItem(){
        if (counter > 1){
            counter --
            productPrice = product?.price!! * counter
            product?.quantity = counter
            binding.textviewCounter.text = "${product?.quantity}"
            binding.textviewPrice.text = "$ ${productPrice}"
        }
    }
}