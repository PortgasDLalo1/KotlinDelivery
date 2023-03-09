package com.eduardo.kotlinudemydelivery.models

import com.google.gson.annotations.SerializedName

class Payer(
    @SerializedName("email") val email: String,
   // @SerializedName("identification") val identification: Identification? = null,
    ) {

    override fun toString(): String {
        return "Payer(email='$email')"
    }
}