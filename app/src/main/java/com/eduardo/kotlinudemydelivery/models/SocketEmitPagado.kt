package com.eduardo.kotlinudemydelivery.models

import com.google.gson.Gson

class SocketEmitPagado(
    val id_order: String
) {

    override fun toString(): String {
        return "SocketEmit(id_order='$id_order')"
    }

    fun toJson(): String {
        return Gson().toJson(this)
    }
}