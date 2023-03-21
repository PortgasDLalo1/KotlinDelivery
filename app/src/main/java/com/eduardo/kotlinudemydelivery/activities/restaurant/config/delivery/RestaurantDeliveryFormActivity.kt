package com.eduardo.kotlinudemydelivery.activities.restaurant.config.delivery

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.eduardo.kotlinudemydelivery.Providers.UsersProvider
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.databinding.ActivityRestaurantDeliveryFormBinding
import com.eduardo.kotlinudemydelivery.models.Order
import com.eduardo.kotlinudemydelivery.models.ResponseHttp
import com.eduardo.kotlinudemydelivery.models.User
import com.eduardo.kotlinudemydelivery.utils.SharedPref
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class RestaurantDeliveryFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRestaurantDeliveryFormBinding
    var toolbar: Toolbar? = null
    var usersProvider : UsersProvider? = null
    val TAG = "DeliveryForm"
    private var imageFile: File? = null
    var bandera = 0
    var email = ""
    var user: User? = null
    var userSession: User? = null
    val gson = Gson()
    var sharedPref: SharedPref? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRestaurantDeliveryFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPref = SharedPref(this)

        getUserFromSession()
        usersProvider = UsersProvider(userSession?.sessionToken)

        toolbar = findViewById(R.id.toolbar)
        toolbar?.setTitleTextColor(ContextCompat.getColor(this,R.color.black))
        toolbar?.title = "Agregar Repartidor"
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnRegisterD.setOnClickListener { registerDelivery() }
    }

    private fun registerDelivery(){
        val name = binding.edittextNombreD.text.toString()
        val lastname = binding.edittextApellidoD.text.toString()
        val email = binding.edittextEmailD.text.toString()
        val phone = binding.edittextPhoneD.text.toString()
        val pass = binding.edittextPasswordrd.text.toString()
        val cpass = binding.edittextPasswordrcD.text.toString()

        if (isValidForm(name, lastname, email, phone, pass, cpass)){
            val user = User(
                name = name,
                lastname = lastname,
                email = email,
                phone = phone,
                password = pass
            )
            //Toast.makeText(this, "$imageFile", Toast.LENGTH_SHORT).show()
            //Log.d(TAG,"User: ${user.toJson()}")
//            Log.d(TAG,"$imageFile")
            usersProvider?.registerDelivery(user)?.enqueue(object : Callback<ResponseHttp> {
                override fun onResponse(
                    call: Call<ResponseHttp>, response: Response<ResponseHttp>
                ) {
                    if (response.body()?.isSuccess == true) {
                        //goToClientHome()
                        Toast.makeText(this@RestaurantDeliveryFormActivity, response.body()?.message, Toast.LENGTH_LONG).show()
                        finish()
                    }
                    Log.d(TAG, "Response: ${response}")
                    Log.d(TAG, "Body: ${response.body()}")
                }

                override fun onFailure(call: Call<ResponseHttp>, t: Throwable) {
                    Log.d(TAG, "Se produjo un error ${t.message}")
                    Toast.makeText(this@RestaurantDeliveryFormActivity, "Se produjo un error ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }
    

    fun String.isEmailValid(): Boolean {
        return !TextUtils.isEmpty(this) && android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }

    private fun isValidForm(
        name:String,
        lastname:String,
        email:String,
        phone:String,
        pass:String,
        cpass:String
    ): Boolean{

        if (name.isNullOrBlank()){
            Toast.makeText(this,"Ingresa el nombre/s", Toast.LENGTH_LONG).show()
            return false
        }

        if (lastname.isNullOrBlank()){
            Toast.makeText(this,"Ingresa el apellido/s", Toast.LENGTH_LONG).show()
            return false
        }

        if (phone.isNullOrBlank()){
            Toast.makeText(this,"Ingresa un numero celular", Toast.LENGTH_LONG).show()
            return false
        }

        if (pass.isNullOrBlank()){
            Toast.makeText(this,"Ingresa una contraseña", Toast.LENGTH_LONG).show()
            return false
        }

        if (cpass.isNullOrBlank()){
            Toast.makeText(this,"Ingresa la confirmacion contraseña", Toast.LENGTH_LONG).show()
            return false
        }

        if (!email.isEmailValid()){
            Toast.makeText(this,"Email no valido", Toast.LENGTH_LONG).show()
            return false
        }

        if (pass != cpass){
            Toast.makeText(this,"Las contraseñas no coinciden", Toast.LENGTH_LONG).show()
            return false
        }

        return true
    }

    private fun getUserFromSession(){
        val gson = Gson()
        if (!sharedPref?.getData("user").isNullOrBlank()){
            //si el usuario exite en sesion
            userSession = gson.fromJson(sharedPref?.getData("user"), User::class.java)
        }
    }

}