package com.eduardo.kotlinudemydelivery.models

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

class Ingrediente(
    @SerializedName("id_ingrediente") val id: String? = null,
    @SerializedName("name_ingrediente") val name_ingrediente: String,
    @SerializedName("is_available") val is_available: Boolean
) {
    fun toJson(): String{
        return Gson().toJson(this)
    }

    override fun toString(): String {
        return "Ingrediente(id=$id, name_ingrediente='$name_ingrediente', is_available=$is_available)"
    }

}