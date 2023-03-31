package com.eduardo.kotlinudemydelivery.activities

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import com.eduardo.kotlinudemydelivery.Providers.SucursalesProvider
import com.eduardo.kotlinudemydelivery.Providers.UsersProvider
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.activities.client.home.ClientHomeActivity
import com.eduardo.kotlinudemydelivery.activities.delivery.home.DeliveryHomeActivity
import com.eduardo.kotlinudemydelivery.activities.restaurant.home.RestaurantHomeActivity
import com.eduardo.kotlinudemydelivery.activities.waiter.home.WaiterHomeActivity
import com.eduardo.kotlinudemydelivery.databinding.ActivityMainBinding
import com.eduardo.kotlinudemydelivery.models.ResponseHttp
import com.eduardo.kotlinudemydelivery.models.User
import com.eduardo.kotlinudemydelivery.utils.SharedPref
import com.eduardo.kotlinudemydelivery.utils.printTicket
import com.google.gson.Gson
import com.mazenrashed.printooth.ui.ScanningActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    var edittextEmail: EditText? = null
    var usersProvider = UsersProvider()
    val TAG = "MainActivity"
    val count = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        edittextEmail = findViewById(R.id.edittext_email)

        binding.imageviewGoToRegister.setOnClickListener { goToRegister() }

        binding.btnLogin.setOnClickListener { login() }

       /* edittextEmail?.addTextChangedListener(object : TextWatcher{
            private val space = ' '
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

//                Toast.makeText(this@MainActivity, "CHAR: $s, start: $start, before: $before, count: $count", Toast.LENGTH_SHORT).show()

            }

            override fun afterTextChanged(s: Editable?) {
                var pos = 0;
                while (true){
                    if (pos >= s?.length!!) break
                    if (space == s[pos] && (((pos +1)%5)!= 0 || pos +1 == s.length)){
                        s.delete(pos,pos+1)
                    }else{
                        pos++
                    }
                }

                pos = 4;
                while (true){
                    if (pos >= s.length)break
                    val c = s[pos]

                    if ("0123456789".indexOf(c) >= 0){
                        s.insert(pos,"" + space)
                    }
                    pos +=5
                }
            }

        })*/

        getUserFromSession()

    }

    private fun login(){
        val email = binding.edittextEmail.text.toString()
        val pass = binding.edittextPassword.text.toString()

        if (isValidForm(email,pass)){
            usersProvider.login(email,pass)?.enqueue(object: Callback<ResponseHttp>{
                override fun onResponse(
                    call: Call<ResponseHttp>,
                    response: Response<ResponseHttp>
                ) {

                    Log.d(TAG, "Response: ${response.body()}")

                    if (response.body()?.isSuccess == true){
                        Toast.makeText(this@MainActivity,response.body()?.message, Toast.LENGTH_LONG).show()
                        saveUserInSession(response.body()?.data.toString())

                    }else{
                        Toast.makeText(this@MainActivity,"Los datos ingresados no son correctos", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<ResponseHttp>, t: Throwable) {
                    Log.d("MainActivity1", "Hubo un error ${t.message}")
                    Toast.makeText(this@MainActivity,"Hubo un error ${t.message}", Toast.LENGTH_LONG).show()
                }

            })
        }else{
            Toast.makeText(this,"Formulario no valido", Toast.LENGTH_LONG).show()
        }
    }

    private fun goToClientHome(){
        val i = Intent(this, ClientHomeActivity::class.java)
        i.flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK //eliminar historial de pantallas
        startActivity(i)
    }

    private fun goToRestaurantHome(){
        val i = Intent(this, RestaurantHomeActivity::class.java)
        i.flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK //eliminar historial de pantallas
        startActivity(i)
    }

    private fun goToDeliveryHome(){
        val i = Intent(this, DeliveryHomeActivity::class.java)
        i.flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK //eliminar historial de pantallas
        startActivity(i)
    }

    private fun goToWaiterHome(){
        val i = Intent(this, WaiterHomeActivity::class.java)
        i.flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK //eliminar historial de pantallas
        startActivity(i)
    }

    private fun goToSelectRol(){
        val i = Intent(this, SelectRolesActivity::class.java)
        i.flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK //eliminar historial de pantallas
        startActivity(i)
    }

    private fun saveUserInSession(data: String){
        val sharedPref = SharedPref(this)
        val gson = Gson()
        val user = gson.fromJson(data,User::class.java)
        sharedPref.save("user",user)

        if(user.roles?.size!! > 1){ // tiene mas de 1 rol
            goToSelectRol()
        }else { // solo tiene 1 rol

            val idRol = user.roles[0].id.toInt()
            if (idRol == 1){
                goToClientHome()
            }else if (idRol == 2){
                goToRestaurantHome()
            }else if (idRol == 3){
                goToDeliveryHome()
            }else if (idRol == 4){
                goToWaiterHome()
            }
        }
    }

    fun String.isEmailValid(): Boolean {
        return !TextUtils.isEmpty(this) && android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }

    private fun getUserFromSession(){
        val sharedPref = SharedPref(this)
        val gson = Gson()
        if (!sharedPref.getData("user").isNullOrBlank()){
            //si el usuario exite en sesion
            val user = gson.fromJson(sharedPref.getData("user"),User::class.java)
            val nameRol = user.roles?.get(0)?.name
            Log.d(TAG, "user $nameRol")

            if (!nameRol.isNullOrBlank()) {
                //si el usuario selecciono el rol
//                val rol = sharedPref.getData("rol")?.replace("\"","")

                if (nameRol == "RESTAURANTE"){
                    goToRestaurantHome()
                }
                else if (nameRol == "CLIENTE"){
                    goToClientHome()
                }
                else if (nameRol == "REPARTIDOR"){
                    goToDeliveryHome()
                }
                else if (nameRol == "MESERO"){
                    goToWaiterHome()
                }
            }
            else{
                goToClientHome()
            }
        }
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