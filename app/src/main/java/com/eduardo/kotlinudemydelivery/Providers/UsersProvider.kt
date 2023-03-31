package com.eduardo.kotlinudemydelivery.Providers

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.eduardo.kotlinudemydelivery.api.ApiRoutes
import com.eduardo.kotlinudemydelivery.models.Category
import com.eduardo.kotlinudemydelivery.models.ResponseHttp
import com.eduardo.kotlinudemydelivery.models.User
import com.eduardo.kotlinudemydelivery.routes.UsersRoutes
import com.eduardo.kotlinudemydelivery.utils.SharedPref
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class UsersProvider(val token: String? = null) {

    private var usersRoutes: UsersRoutes? = null
    private var usersRoutesToken: UsersRoutes? = null

    val TAG = "UsersProvider"

    init {
        val api = ApiRoutes()
        usersRoutes = api.getUserRoutes()
        if (token != null) {
            usersRoutesToken = api.getUserRoutesWithToken(token)
        }
    }

    fun createToken(user: User, context: Activity){
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful){
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            val token = task.result

            val sharedPref = SharedPref(context)
            user.notificationToken = token
            sharedPref.save("user",user)

            updateNotificationToken(user)?.enqueue(object : Callback<ResponseHttp>{
                override fun onResponse(
                    call: Call<ResponseHttp>,
                    response: Response<ResponseHttp>
                ) {
                    if (response.body() == null){
                        Log.d(TAG,"Hubo un error al crear el token")
                    }
                }

                override fun onFailure(call: Call<ResponseHttp>, t: Throwable) {
                    Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                }

            })

            Log.d(TAG,"TOKEN NOTIFICACIONES $token")
        })
    }

    fun getDelivery(): Call<ArrayList<User>>?{
        return usersRoutesToken?.getDelivery(token!!)
    }

    fun getWaiter(): Call<ArrayList<User>>?{
        return usersRoutesToken?.getWaiter(token!!)
    }

    fun register(user: User): Call<ResponseHttp>? {
        return usersRoutes?.register(user)
    }

    fun registerDelivery(user: User): Call<ResponseHttp>? {
        return usersRoutes?.registerDelivery(user,token!!)
    }

    fun registerWaiter(user: User): Call<ResponseHttp>? {
        return usersRoutes?.registerWaiter(user,token!!)
    }
    fun login(email: String, password: String): Call<ResponseHttp>? {
        return usersRoutes?.login(email, password)
    }

    fun updateWithoutImage(user: User): Call<ResponseHttp>? {
        return usersRoutesToken?.updateWithoutImage(user, token!!)
    }

    fun update(file: File, user: User): Call<ResponseHttp>? {
        val reqFile = RequestBody.create(MediaType.parse("image/*"), file)
        val image = MultipartBody.Part.createFormData("image",file.name, reqFile)
        val requestBody = RequestBody.create(MediaType.parse("text/plain"), user.toJson())

        return  usersRoutesToken?.update(image,requestBody, token!!)
    }

    fun updateNotificationToken(user: User): Call<ResponseHttp>? {
        return usersRoutesToken?.updateNotificationToken(user, token!!)
    }

    fun deleteDelivery(id_user: Int):Call<ResponseHttp>?{
        return usersRoutes?.deleteDelivery(id_user)
    }
}