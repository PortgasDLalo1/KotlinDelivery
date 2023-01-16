package com.eduardo.kotlinudemydelivery.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.eduardo.kotlinudemydelivery.Providers.UsersProvider
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.activities.client.home.ClientHomeActivity
import com.eduardo.kotlinudemydelivery.databinding.ActivityRegisterBinding
import com.eduardo.kotlinudemydelivery.models.ResponseHttp
import com.eduardo.kotlinudemydelivery.models.User
import com.eduardo.kotlinudemydelivery.utils.SharedPref
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    var usersProvider = UsersProvider()

    val TAG = "RegisterActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imageviewGoToLogin.setOnClickListener { finish() }

        binding.btnRegister.setOnClickListener { register() }
    }

    private fun register(){
        val name = binding.edittextNombres.text.toString()
        val lastname = binding.edittextApellidos.text.toString()
        val email = binding.edittextEmailr.text.toString()
        val phone = binding.edittextPhone.text.toString()
        val pass = binding.edittextPasswordr.text.toString()
        val cpass = binding.edittextPasswordrc.text.toString()

        if (isValidForm(name, lastname, email, phone, pass, cpass)){
            val user = User(
                name = name,
                lastname = lastname,
                email = email,
                phone = phone,
                password = pass
            )
            usersProvider.register(user)?.enqueue(object: Callback<ResponseHttp>{
                override fun onResponse(
                    call: Call<ResponseHttp>,
                    response: Response<ResponseHttp>
                ) {
                    if(response.body()?.isSuccess == true){
                        saveUserInSession(response.body()?.data.toString())
                        goToClientHome()
                        finish()
                    }

                    Toast.makeText(this@RegisterActivity,response.body()?.message,Toast.LENGTH_LONG).show()

                    Log.d(TAG, "Response: ${response}")
                    Log.d(TAG, "Body: ${response.body()}")
                }

                override fun onFailure(call: Call<ResponseHttp>, t: Throwable) {
                    Log.d(TAG,"Se produjo un error ${t.message}")
                    Toast.makeText(this@RegisterActivity,"Se produjo un error ${t.message}",Toast.LENGTH_LONG).show()
                }

            })
        }
    }

    private fun goToClientHome(){
        val i = Intent(this, SaveImageActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK //eliminar historial de pantallas
        startActivity(i)
    }

    private fun saveUserInSession(data: String){
        val sharedPref = SharedPref(this)
        val gson = Gson()
        val user = gson.fromJson(data,User::class.java)
        sharedPref.save("user",user)
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
}