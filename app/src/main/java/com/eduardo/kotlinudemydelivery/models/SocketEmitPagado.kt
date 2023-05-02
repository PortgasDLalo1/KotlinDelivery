package com.eduardo.kotlinudemydelivery.models

import com.google.gson.Gson

class SocketEmitPagado(
    val id_order: String,
    val id_restaurant:String
) {


    fun toJson(): String {
        return Gson().toJson(this)
    }

    override fun toString(): String {
        return "SocketEmitPagado(id_order='$id_order', id_restaurant='$id_restaurant')"
    }
}