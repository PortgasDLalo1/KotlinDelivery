package com.eduardo.kotlinudemydelivery.models

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

class Cards(
    @SerializedName("id") val id: String? = null,
    @SerializedName("id_client") val id_client: String? = null,
    @SerializedName("number_card") val number_card: String? = null,
    @SerializedName("expiration") val expiration: String? = null,
    @SerializedName("name_client") val name_client: String? = null,
    @SerializedName("cvv") val cvv: String? = null
) {

    override fun toString(): String {
        return "Cards(id=$id, id_client=$id_client, number_card='$number_card', expiration='$expiration', name_client='$name_client', cvv='$cvv')"
    }

    fun toJson(): String{
        return Gson().toJson(this)
    }
}