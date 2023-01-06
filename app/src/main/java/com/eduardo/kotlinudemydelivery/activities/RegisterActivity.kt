package com.eduardo.kotlinudemydelivery.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

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
            Toast.makeText(this,"Formulario valido", Toast.LENGTH_LONG).show()
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
}