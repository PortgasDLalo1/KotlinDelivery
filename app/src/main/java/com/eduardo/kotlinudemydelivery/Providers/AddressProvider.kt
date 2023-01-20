package com.eduardo.kotlinudemydelivery.Providers

import com.eduardo.kotlinudemydelivery.api.ApiRoutes
import com.eduardo.kotlinudemydelivery.models.Address
import com.eduardo.kotlinudemydelivery.models.Category
import com.eduardo.kotlinudemydelivery.models.ResponseHttp
import com.eduardo.kotlinudemydelivery.routes.AddressRoutes
import com.eduardo.kotlinudemydelivery.routes.CategoriesRoutes
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import java.io.File

class AddressProvider(val token: String) {

    private var addressRoutes: AddressRoutes? = null

    init {
        val api = ApiRoutes()
        addressRoutes = api.getAddressRoutes(token)
    }

    fun getAddress(idUser: String): Call<ArrayList<Address>>?{
        return addressRoutes?.getAddress(idUser,token)
    }

    fun create(address: Address): Call<ResponseHttp>? {
        return  addressRoutes?.create(address, token)
    }

}