package com.eduardo.kotlinudemydelivery.routes

import com.eduardo.kotlinudemydelivery.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface OrdersRoutes {

    @GET("orders/findByStatus/{status}")
    fun getOrdersByStatus(
        @Path("status") status: String,
        @Header("Authorization") token: String
    ):Call<ArrayList<Order>>

    @GET("orders/findByClientAndStatus/{id_client}/{status}")
    fun getOrdersByClientAndStatus(
        @Path("id_client") idClient: String,
        @Path("status") status: String,
        @Header("Authorization") token: String
    ):Call<ArrayList<Order>>

    @GET("orders/findByIdOrder/{id_order}")
    fun getOrdersByIdOrder(
        @Path("id_order") idOrder: String,
        @Header("Authorization") token: String
    ):Call<ArrayList<Order>>

    @POST("orders/create")
    fun create(
        @Body order: Order,
        @Header("Authorization") token: String
    ): Call<ResponseHttp>

    @PUT("orders/updateToDispatched")
    fun updateToDispatched(
        @Body order: Order,
        @Header("Authorization") token: String
    ): Call<ResponseHttp>

    @PUT("orders/updateToOnTheWay")
    fun updateToOnTheWay(
        @Body order: Order,
        @Header("Authorization") token: String
    ): Call<ResponseHttp>

    @PUT("orders/updateToDelivery")
    fun updateToDelivery(
        @Body order: Order,
        @Header("Authorization") token: String
    ): Call<ResponseHttp>

    @GET("orders/findByDeliveryAndStatus/{id_delivery}/{status}")
    fun getOrdersByDeliveryAndStatus(
        @Path("id_delivery") id_delivery: String,
        @Path("status") status: String,
        @Header("Authorization") token: String
    ):Call<ArrayList<Order>>

    @PUT("orders/updateLatLng")
    fun updateLatLng(
        @Body order: Order,
        @Header("Authorization") token: String
    ): Call<ResponseHttp>
}