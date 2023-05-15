package com.eduardo.kotlinudemydelivery.routes

import com.eduardo.kotlinudemydelivery.models.Category
import com.eduardo.kotlinudemydelivery.models.Product
import com.eduardo.kotlinudemydelivery.models.ResponseHttp
import com.eduardo.kotlinudemydelivery.models.User
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface ProductsRoutes {
    @GET("products/getAll")
    fun getAll(
        @Header("Authorization") token: String
    ):Call<ArrayList<Product>>

   @GET("products/findByCategory/{id_category}")
    fun findByCategory(
       @Path("id_category") idCategory: String,
        @Header("Authorization") token: String
    ):Call<ArrayList<Product>>

    @Multipart
    @POST("products/create")
    fun create(
        @Part images: Array<MultipartBody.Part?>,
        @Part("product") product: RequestBody,
        @Header("Authorization") token: String
    ): Call<ResponseHttp>

    @Multipart
    @POST("products/create2")
    fun create2(
        @Part image: MultipartBody.Part,
        @Part("product") product: RequestBody,
        @Header("Authorization") token: String
    ): Call<ResponseHttp>

    @DELETE("products/deleteProduct/{id_product}/{imageUrl}")
    fun deleteProduct(
        @Path("id_product") id_product: String,
        @Path("imageUrl") imageUrl: String,
    ): Call<ResponseHttp>
}