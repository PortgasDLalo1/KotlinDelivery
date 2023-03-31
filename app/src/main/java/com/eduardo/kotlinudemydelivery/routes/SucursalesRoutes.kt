package com.eduardo.kotlinudemydelivery.routes

import com.eduardo.kotlinudemydelivery.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface SucursalesRoutes {

    @GET("sucursales/findByRestaurant/{id_restaurant}")
    fun getSucursales(
        @Path("id_restaurant") idRestaurant: String,
        @Header("Authorization") token: String
    ):Call<ResponseHttp>

    @POST("sucursales/create")
    fun create(
        @Body sucursales: Sucursales,
        @Header("Authorization") token: String
    ): Call<ResponseHttp>
}