package com.eduardo.kotlinudemydelivery.routes

import com.eduardo.kotlinudemydelivery.models.Category
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

interface CategoriesRoutes {

    @GET("categories/getAll")
    fun getAll(
        @Header("Authorization") token: String
    ):Call<ArrayList<Category>>

    @Multipart
    @POST("categories/create")
    fun create(
        @Part image: MultipartBody.Part,
        @Part("category") category: RequestBody,
        @Header("Authorization") token: String
    ): Call<ResponseHttp>

    @DELETE("categories/deleteCategory/{id_category}/{imageUrl}")
    fun deleteCategory(
        @Path("id_category") id_category: String,
        @Path("imageUrl") imageUrl: String,
        @Header("Authorization") token: String
    ): Call<ResponseHttp>
}