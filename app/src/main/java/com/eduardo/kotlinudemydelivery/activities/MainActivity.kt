package com.eduardo.kotlinudemydelivery.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.ImageView
import android.widget.Toast
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imageviewGoToRegister.setOnClickListener { goToRegister() }

        binding.btnLogin.setOnClickListener { login() }
    }

    private fun login(){
        val email = binding.edittextEmail.text.toString()
        val pass = binding.edittextPassword.text.toString()

        if (isValidForm(email,pass)){
            Toast.makeText(this,"Formulario valido", Toast.LENGTH_LONG).show()
        }else{
            Toast.makeText(this,"Formulario no valido", Toast.LENGTH_LONG).show()
        }
    }

    fun String.isEmailValid(): Boolean {
        return !TextUtils.isEmpty(this) && android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }

    private fun isValidForm(email:String, pass:String): Boolean{
        if (email.isNullOrBlank()){
            return false
        }

        if (pass.isNullOrBlank()){
            return false
        }

        if (!email.isEmailValid()){
            Toast.makeText(this,"Email no valido", Toast.LENGTH_LONG).show()
            return false
        }

        return true
    }

    private fun goToRegister(){
        val i = Intent(this,RegisterActivity::class.java)
        startActivity(i)
    }
}