package com.eduardo.kotlinudemydelivery.Providers

import com.eduardo.kotlinudemydelivery.api.ApiRoutes
import com.eduardo.kotlinudemydelivery.models.Ingrediente
import com.eduardo.kotlinudemydelivery.models.ResponseHttp
import com.eduardo.kotlinudemydelivery.routes.IngredienteRoutes
import retrofit2.Call

class IngredienteProvider(val token: String) {

    private var ingredienteRoute: IngredienteRoutes? = null

    init {
        val api = ApiRoutes()
        ingredienteRoute = api.getIngredientesRoutes(token)
    }

    fun createIngre(ingrediente: Ingrediente): Call<ResponseHttp>?{
        return ingredienteRoute?.createIngrediente(ingrediente, token)
    }

    fun getAll(): Call<ArrayList<Ingrediente>>?{
        return ingredienteRoute?.getAll(token)
    }
}