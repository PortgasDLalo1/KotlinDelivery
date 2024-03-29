package com.eduardo.kotlinudemydelivery.routes

import com.eduardo.kotlinudemydelivery.models.Category
import com.eduardo.kotlinudemydelivery.models.ResponseHttp
import com.eduardo.kotlinudemydelivery.models.User
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface UsersRoutes {

    @GET("users/findDelivery")
    fun getDelivery(
        @Header("Authorization") token: String
    ):Call<ArrayList<User>>

    @GET("users/findWaiter")
    fun getWaiter(
        @Header("Authorization") token: String
    ):Call<ArrayList<User>>

    @POST("users/create")
    fun register(@Body user: User): Call<ResponseHttp>


    @POST("users/createDelivery")
    fun registerDelivery(
        @Body user: User,
         @Header("Authorization") token: String
    ): Call<ResponseHttp>

    @POST("users/createWaiter")
    fun registerWaiter(
        @Body user: User,
        @Header("Authorization") token: String
    ): Call<ResponseHttp>

    @FormUrlEncoded
    @POST("users/login")
    fun login(@Field("email") email: String, @Field("password") password: String): Call<ResponseHttp>

    @Multipart
    @PUT("users/update")
    fun update(
        @Part image: MultipartBody.Part,
        @Part("user") user: RequestBody,
        @Header("Authorization") token: String
    ): Call<ResponseHttp>

    @PUT("users/updateWithoutImage")
    fun updateWithoutImage(
        @Body user: User,
        @Header("Authorization") token: String
    ): Call<ResponseHttp>

    @PUT("users/updateNotificationToken")
    fun updateNotificationToken(
        @Body user: User,
        @Header("Authorization") token: String
    ): Call<ResponseHttp>

    @DELETE("users/deleteDelivery/{id_user}")
    fun deleteDelivery(
        @Path("id_user") id_user: Int
    ): Call<ResponseHttp>
}