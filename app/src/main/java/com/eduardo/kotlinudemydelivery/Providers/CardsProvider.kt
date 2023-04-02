package com.eduardo.kotlinudemydelivery.Providers

import com.eduardo.kotlinudemydelivery.api.ApiRoutes
import com.eduardo.kotlinudemydelivery.models.*
import com.eduardo.kotlinudemydelivery.routes.AddressRoutes
import com.eduardo.kotlinudemydelivery.routes.CardRoutes
import com.eduardo.kotlinudemydelivery.routes.CategoriesRoutes
import com.eduardo.kotlinudemydelivery.routes.SucursalesRoutes
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import java.io.File

class CardsProvider(val token: String) {

    private var cardRoutes: CardRoutes? = null

    init {
        val api = ApiRoutes()
        cardRoutes = api.getCardsRoutes(token)
    }

    fun getCards(idClient: String): Call<ArrayList<Cards>>?{
        return cardRoutes?.getCards(idClient,token)
    }

    fun create(card: Cards): Call<ResponseHttp>? {
        return  cardRoutes?.create(card, token)
    }

}