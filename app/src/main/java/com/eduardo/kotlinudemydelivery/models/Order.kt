package com.eduardo.kotlinudemydelivery.models

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

class Order(
    @SerializedName("id") val id: String? = null,
    @SerializedName("id_client") val id_client: String,
    @SerializedName("id_delivery") var id_delivery: String? = null,
    @SerializedName("id_address") val id_address: String,
    @SerializedName("id_restaurant") val id_restaurant: String? = null,
    @SerializedName("installments_type") val installments_type: String,
    @SerializedName("status") val status: String? = null,
    @SerializedName("timestamp") val timestamp: String? = null,
    @SerializedName("products") val products: ArrayList<Product>,
    @SerializedName("client") val client: User? = null,
    @SerializedName("delivery") val delivery: User? = null,
    @SerializedName("address") val address: Address? = null,
    @SerializedName("lat") var lat: Double? = null,
    @SerializedName("lng") var lng: Double? = null
) {



    fun toJson(): String{
        return Gson().toJson(this)
    }

    override fun toString(): String {
        return "Order(id=$id, id_client='$id_client', id_delivery=$id_delivery, id_address='$id_address', id_restaurant=$id_restaurant, installments_type=$installments_type, status=$status, timestamp=$timestamp, products=$products, client=$client, delivery=$delivery, address=$address, lat=$lat, lng=$lng)"
    }


}