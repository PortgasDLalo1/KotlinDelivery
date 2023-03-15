package com.eduardo.kotlinudemydelivery.api

import com.eduardo.kotlinudemydelivery.routes.*

class ApiRoutes {

    val API_URL = "http://10.72.6.224:3000/api/"

    val retrofit = RetrofitClient()

    fun getUserRoutes(): UsersRoutes {
        return retrofit.getClient(API_URL).create(UsersRoutes::class.java)
    }

    fun getUserRoutesWithToken(token: String): UsersRoutes {
        return retrofit.getClientWithToken(API_URL, token).create(UsersRoutes::class.java)
    }

    fun getCategoriesRoutes(token: String): CategoriesRoutes {
        return retrofit.getClientWithToken(API_URL, token).create(CategoriesRoutes::class.java)
    }

    fun getAddressRoutes(token: String): AddressRoutes {
        return retrofit.getClientWithToken(API_URL, token).create(AddressRoutes::class.java)
    }

    fun getProductsRoutes(token: String): ProductsRoutes {
        return retrofit.getClientWithToken(API_URL, token).create(ProductsRoutes::class.java)
    }

    fun getOrdersRoutes(token: String): OrdersRoutes {
        return retrofit.getClientWithToken(API_URL, token).create(OrdersRoutes::class.java)
    }

    fun getPaymentsRoutes(token: String): PaymentsRoutes {
        return retrofit.getClientWithToken(API_URL, token).create(PaymentsRoutes::class.java)
    }

}