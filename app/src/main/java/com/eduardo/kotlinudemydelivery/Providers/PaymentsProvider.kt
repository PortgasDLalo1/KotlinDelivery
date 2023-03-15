package com.eduardo.kotlinudemydelivery.Providers

import com.eduardo.kotlinudemydelivery.api.ApiRoutes
import com.eduardo.kotlinudemydelivery.models.Category
import com.eduardo.kotlinudemydelivery.models.MercadoPagoPayment
import com.eduardo.kotlinudemydelivery.models.ResponseHttp
import com.eduardo.kotlinudemydelivery.routes.CategoriesRoutes
import com.eduardo.kotlinudemydelivery.routes.PaymentsRoutes
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import java.io.File

class PaymentsProvider(val token: String) {

    private var paymentsRoutes: PaymentsRoutes? = null

    init {
        val api = ApiRoutes()
        paymentsRoutes = api.getPaymentsRoutes(token)
    }


    fun create(mercadoPagoPayment: MercadoPagoPayment): Call<ResponseHttp>? {
        return paymentsRoutes?.createPayment(mercadoPagoPayment, token)
    }

}