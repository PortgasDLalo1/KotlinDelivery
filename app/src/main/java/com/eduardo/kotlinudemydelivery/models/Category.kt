package com.eduardo.kotlinudemydelivery.models

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

class Category(
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String,
    @SerializedName("image") val image: String? = null
) {

    override fun toString(): String {
        return name
    }

    fun toJson(): String{
        return Gson().toJson(this)
    }
}