package com.eduardo.kotlinudemydelivery.routes

import com.eduardo.kotlinudemydelivery.models.Ingrediente
import com.eduardo.kotlinudemydelivery.models.ResponseHttp
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface IngredienteRoutes {

    @POST("ingredientes/createIngre")
    fun createIngrediente(
        @Body ingrediente: Ingrediente,
        @Header("Authorization") token: String
    ): Call<ResponseHttp>

    @GET("ingredientes/getAll")
    fun getAll(
        @Header("Authorization") token: String
    ):Call<ArrayList<Ingrediente>>
}