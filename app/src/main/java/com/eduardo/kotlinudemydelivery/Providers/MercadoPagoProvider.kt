package com.eduardo.kotlinudemydelivery.Providers

import com.eduardo.kotlinudemydelivery.api.MercadoPagoApiRoutes
import com.eduardo.kotlinudemydelivery.models.MercadoPagoCardTokenBody
import com.eduardo.kotlinudemydelivery.routes.MercadoPagoRoutes
import com.google.gson.JsonObject
import retrofit2.Call

class MercadoPagoProvider {

    var mercadoPagoRoutes: MercadoPagoRoutes? = null

    init {
        val api = MercadoPagoApiRoutes()
        mercadoPagoRoutes = api.getMercadoPagoRoutes()
    }

    fun createCardToken(mercadoPagoCardTokenBody: MercadoPagoCardTokenBody): Call<JsonObject>? {
        return mercadoPagoRoutes?.createCardToken(mercadoPagoCardTokenBody)
    }

}