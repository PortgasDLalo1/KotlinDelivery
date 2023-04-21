package com.eduardo.kotlinudemydelivery.Providers

import com.eduardo.kotlinudemydelivery.api.ApiRoutes
import com.eduardo.kotlinudemydelivery.models.Address
import com.eduardo.kotlinudemydelivery.models.Category
import com.eduardo.kotlinudemydelivery.models.Order
import com.eduardo.kotlinudemydelivery.models.ResponseHttp
import com.eduardo.kotlinudemydelivery.routes.AddressRoutes
import com.eduardo.kotlinudemydelivery.routes.CategoriesRoutes
import com.eduardo.kotlinudemydelivery.routes.OrdersRoutes
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import java.io.File

class OrdersProvider(val token: String) {

    private var ordersRoutes: OrdersRoutes? = null

    init {
        val api = ApiRoutes()
        ordersRoutes = api.getOrdersRoutes(token)
    }

    fun getOrdersByStatus(status: String, id_restaurant: String): Call<ArrayList<Order>>?{
        return ordersRoutes?.getOrdersByStatus(status,id_restaurant,token)
    }

    fun getOrdersByClientAndStatus(idClient: String,status: String): Call<ArrayList<Order>>?{
        return ordersRoutes?.getOrdersByClientAndStatus(idClient,status,token)
    }

    fun getOrdersByIdOrder(idOrder: String): Call<ArrayList<Order>>?{
        return ordersRoutes?.getOrdersByIdOrder(idOrder,token)
    }

    fun create(order: Order): Call<ResponseHttp>? {
        return  ordersRoutes?.create(order, token)
    }

    fun updateToDispatched(order: Order): Call<ResponseHttp>? {
        return  ordersRoutes?.updateToDispatched(order, token)
    }

    fun updateToOnTheWay(order: Order): Call<ResponseHttp>? {
        return  ordersRoutes?.updateToOnTheWay(order, token)
    }

    fun updateToDelivery(order: Order): Call<ResponseHttp>? {
        return  ordersRoutes?.updateToDelivery(order, token)
    }

    fun updateLatLng(order: Order): Call<ResponseHttp>? {
        return  ordersRoutes?.updateLatLng(order, token)
    }

    fun getOrdersByDeliveryAndStatus(id_delivery: String,status: String): Call<ArrayList<Order>>?{
        return ordersRoutes?.getOrdersByDeliveryAndStatus(id_delivery,status,token)
    }
}