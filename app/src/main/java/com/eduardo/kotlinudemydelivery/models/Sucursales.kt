package com.eduardo.kotlinudemydelivery.models

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

class Sucursales(
    @SerializedName("id") val id: String? = null,
    @SerializedName("id_restaurant") val id_restaurant: String? = null,
    @SerializedName("image") val image: String? = null,
    @SerializedName("address") val address: String,
    @SerializedName("neighborhood") val neighborhood: String,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double
) {



    fun toJson(): String{
        return Gson().toJson(this)
    }

    override fun toString(): String {
        return "Sucursales(id=$id, id_restaurant=$id_restaurant, image=$image, address='$address', neighborhood='$neighborhood', lat=$lat, lng=$lng)"
    }
}