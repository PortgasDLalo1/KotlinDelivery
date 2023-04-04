package com.eduardo.kotlinudemydelivery.models

import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

class SucursalesDistance(
    @SerializedName("id") val id: String? = null,
    @SerializedName("distance") val distance: Float? = null,
    @SerializedName("neighborhood") val neighborhood: String? = null,
    @SerializedName("latlng") val latlng: LatLng? = null
) {

    override fun toString(): String {
        return "SucursalesDistance(id=$id, distance=$distance, latlng=$latlng)"
    }

    fun toJson(): String{
        return Gson().toJson(this)
    }

}