package com.eduardo.kotlinudemydelivery.Providers

import com.eduardo.kotlinudemydelivery.api.ApiRoutes
import com.eduardo.kotlinudemydelivery.models.Category
import com.eduardo.kotlinudemydelivery.models.Product
import com.eduardo.kotlinudemydelivery.models.ResponseHttp
import com.eduardo.kotlinudemydelivery.routes.CategoriesRoutes
import com.eduardo.kotlinudemydelivery.routes.ProductsRoutes
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import java.io.File

class ProductsProvider(val token: String) {

    private var productsRoutes: ProductsRoutes? = null

    init {
        val api = ApiRoutes()
        productsRoutes = api.getProductsRoutes(token)
    }

    fun getAll(): Call<ArrayList<Product>>?{
        return productsRoutes?.getAll(token)
    }

    fun findByCategory(idCategory: String): Call<ArrayList<Product>>?{
        return productsRoutes?.findByCategory(idCategory,token)
    }

    fun create(files: List<File>, product: Product): Call<ResponseHttp>? {

        val images = arrayOfNulls<MultipartBody.Part>(files.size)

        for (i in 0 until files.size){
            val reqFile = RequestBody.create(MediaType.parse("image/*"), files[i])
            images[i] = MultipartBody.Part.createFormData("images",files[i].name, reqFile)
        }


        val requestBody = RequestBody.create(MediaType.parse("text/plain"), product.toJson())

        return  productsRoutes?.create(images,requestBody, token)
    }

    fun create2(file: File, product: Product): Call<ResponseHttp>?{
        val reqFile = RequestBody.create(MediaType.parse("image/*"), file)
        val image = MultipartBody.Part.createFormData("image",file.name, reqFile)
        val requestBody = RequestBody.create(MediaType.parse("text/plain"), product.toJson())

        return productsRoutes?.create2(image,requestBody,token)
    }

    fun delete(id_product: String, imageUrl: String): Call<ResponseHttp>?{
        return productsRoutes?.deleteProduct(id_product,imageUrl)
    }

    fun updateWithOutImage(product: Product): Call<ResponseHttp>?{
//        val requestBody = RequestBody.create(MediaType.parse("text/plain"), product.toJson())
        return productsRoutes?.updateWithOutImage(product,token!!)
    }

    fun update(imageUrl: String, file: File, product: Product): Call<ResponseHttp>?{
        val reqFile = RequestBody.create(MediaType.parse("image/*"), file)
        val image = MultipartBody.Part.createFormData("image",file.name, reqFile)
        val requestBody = RequestBody.create(MediaType.parse("text/plain"), product.toJson())
        return productsRoutes?.update(imageUrl,image,requestBody,token!!)
    }

}