package com.eduardo.kotlinudemydelivery.Providers

import com.eduardo.kotlinudemydelivery.api.ApiRoutes
import com.eduardo.kotlinudemydelivery.models.Address
import com.eduardo.kotlinudemydelivery.models.Category
import com.eduardo.kotlinudemydelivery.models.ResponseHttp
import com.eduardo.kotlinudemydelivery.models.Sucursales
import com.eduardo.kotlinudemydelivery.routes.AddressRoutes
import com.eduardo.kotlinudemydelivery.routes.CategoriesRoutes
import com.eduardo.kotlinudemydelivery.routes.SucursalesRoutes
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import java.io.File

class SucursalesProvider(val token: String) {

    private var sucursalesRoutes: SucursalesRoutes? = null

    init {
        val api = ApiRoutes()
        sucursalesRoutes = api.getSucursalesRoutes(token)
    }

    fun getSucursal(idRestaurant: String): Call<ResponseHttp>?{
        return sucursalesRoutes?.getSucursales(idRestaurant,token)
    }

    fun getSucursalAll(): Call<ArrayList<Sucursales>>?{
        return sucursalesRoutes?.getSucursalesAll(token)
    }
    fun create(sucursal: Sucursales): Call<ResponseHttp>? {
        return  sucursalesRoutes?.create(sucursal, token)
    }

    fun getRestaurantsWithImage(): Call<ArrayList<Sucursales>>?{
        return sucursalesRoutes?.getRestaurantsWithImage(token)
    }
}