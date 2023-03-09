package com.eduardo.kotlinudemydelivery.routes

import com.eduardo.kotlinudemydelivery.models.MercadoPagoCardTokenBody
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface MercadoPagoRoutes {

    @GET("v1/payment_methods/installments?access_token=TEST-4244782955703418-020814-6f685db66518a0d52aee98fc0659a90c-213239053")
    fun getInstallments(@Query("bin") bin: String, @Query("amount") amount: String): Call<JsonArray>


    @POST("v1/card_tokens?public_key=TEST-e1a2a507-0478-43a3-8cfb-c4be74c1c7b9")
    fun createCardToken(@Body body: MercadoPagoCardTokenBody): Call<JsonObject>

}