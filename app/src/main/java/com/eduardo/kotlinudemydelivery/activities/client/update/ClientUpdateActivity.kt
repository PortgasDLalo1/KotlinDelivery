package com.eduardo.kotlinudemydelivery.activities.client.update

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.eduardo.kotlinudemydelivery.Providers.UsersProvider
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.databinding.ActivityClientUpdateBinding
import com.eduardo.kotlinudemydelivery.models.ResponseHttp
import com.eduardo.kotlinudemydelivery.models.User
import com.eduardo.kotlinudemydelivery.utils.SharedPref
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class ClientUpdateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClientUpdateBinding
    var sharedPref: SharedPref? = null
    var user: User? = null
    private var imageFile: File? = null
    var usersProvider : UsersProvider? = null
    val TAG = "ClientUpdateActivity"

    var toolbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        sharedPref = SharedPref(this)

        toolbar = findViewById(R.id.toolbar)
        toolbar?.title = "Editar Perfil"
        toolbar?.setTitleTextColor(ContextCompat.getColor(this,R.color.white))
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        getUserFromSession()

        usersProvider = UsersProvider(user?.sessionToken)

        binding.edittextNombres.setText(user?.name)
        binding.edittextApellidos.setText(user?.lastname)
        binding.edittextPhone.setText(user?.phone)

        if (!user?.image.isNullOrBlank()) {
            Glide.with(this).load(user?.image).into(binding.circleimageUser)
        }

        binding.circleimageUser.setOnClickListener { selectImage() }
        binding.btnUpdate.setOnClickListener { updateData() }
    }

    private fun getUserFromSession(){
        val gson = Gson()
        if (!sharedPref?.getData("user").isNullOrBlank()){
            //si el usuario exite en sesion
            user = gson.fromJson(sharedPref?.getData("user"), User::class.java)
        }
    }

    private val startImageForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result: ActivityResult ->

        val resultCode = result.resultCode
        val data = result.data

        if (resultCode == Activity.RESULT_OK){
            val fileUri = data?.data
            imageFile = File(fileUri?.path) // el archivo que vamos a guardar como imagen en el servidor
            binding.circleimageUser.setImageURI(fileUri)
        }
        else if (resultCode == ImagePicker.RESULT_ERROR){
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_LONG).show()
        }
        else {
            Toast.makeText(this, "La tarea se cancelo", Toast.LENGTH_LONG).show()
        }

    }

    private fun selectImage(){
        ImagePicker.with(this)
            .crop()
            .compress(1024)
            .maxResultSize(1080,1080)
            .createIntent { intent ->
                startImageForResult.launch(intent)
            }
    }

    private fun updateData(){

        val name = binding.edittextNombres.text.toString()
        val lastname = binding.edittextApellidos.text.toString()
        val phone = binding.edittextPhone.text.toString()

        user?.name = name
        user?.lastname = lastname
        user?.phone = phone

        if(imageFile != null) {

            usersProvider?.update(imageFile!!, user!!)?.enqueue(object : Callback<ResponseHttp> {
                override fun onResponse(
                    call: Call<ResponseHttp>,
                    response: Response<ResponseHttp>
                ) {
                    Log.d(TAG, "RESPONSE: $response")
                    Log.d(TAG, "BODY: ${response.body()}")

                    Toast.makeText(this@ClientUpdateActivity, response.body()?.message, Toast.LENGTH_LONG).show()

                    if(response.body()?.isSuccess == true){
                        saveUserInSession(response.body()?.data.toString())
                    }
                }

                override fun onFailure(call: Call<ResponseHttp>, t: Throwable) {
                    Log.d(TAG, "Error: ${t.message}")
                    Toast.makeText(
                        this@ClientUpdateActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }

            })
        }
        else{
            usersProvider?.updateWithoutImage(user!!)?.enqueue(object : Callback<ResponseHttp> {
                override fun onResponse(
                    call: Call<ResponseHttp>,
                    response: Response<ResponseHttp>
                ) {
                    Log.d(TAG, "RESPONSE: $response")
                    Log.d(TAG, "BODY: ${response.body()}")

                    Toast.makeText(this@ClientUpdateActivity, response.body()?.message, Toast.LENGTH_LONG).show()

                    if(response.body()?.isSuccess == true){
                        saveUserInSession(response.body()?.data.toString())
                    }
                }

                override fun onFailure(call: Call<ResponseHttp>, t: Throwable) {
                    Log.d(TAG, "Error: ${t.message}")
                    Toast.makeText(
                        this@ClientUpdateActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }

            })
        }
    }

    private fun saveUserInSession(data: String){
        val gson = Gson()
        val user = gson.fromJson(data,User::class.java)
        sharedPref?.save("user",user)
    }
}