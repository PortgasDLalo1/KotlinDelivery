package com.eduardo.kotlinudemydelivery.models

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

class Address(
    @SerializedName("id") val id: String? = null,
    @SerializedName("id_user") val id_user: String? = null,
    @SerializedName("address") val address: String,
    @SerializedName("neighborhood") val neighborhood: String,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double
) {

    override fun toString(): String {
        return "Address(id=$id, id_user=$id_user, address='$address', neighborhood='$neighborhood', lat=$lat, lng=$lng)"
    }

    fun toJson(): String{
        return Gson().toJson(this)
    }
}