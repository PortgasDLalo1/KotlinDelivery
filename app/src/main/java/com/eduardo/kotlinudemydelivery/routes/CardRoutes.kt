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

interface CardRoutes {

    @GET("card/cardFindByUser/{id_client}")
    fun getCards(
        @Path("id_client") idclient: String,
        @Header("Authorization") token: String
    ):Call<ArrayList<Cards>>

    @POST("card/create")
    fun create(
        @Body card: Cards,
        @Header("Authorization") token: String
    ): Call<ResponseHttp>
}