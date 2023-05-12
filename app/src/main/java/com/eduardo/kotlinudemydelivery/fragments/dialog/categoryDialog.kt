package com.eduardo.kotlinudemydelivery.fragments.dialog

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.eduardo.kotlinudemydelivery.R
import com.github.dhaval2404.imagepicker.ImagePicker
import java.io.File

class categoryDialog: DialogFragment() {
//    var view: View ? = null
    var image : ImageView? = null
    var nameEdit : EditText? = null
    var btnAddUpdate : Button? = null
    var btnCerrar : ImageView? = null
    private var imageFile: File? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        return super.onCreateView(inflater, container, savedInstanceState)

        var view: View = inflater.inflate(R.layout.dialog_category, container, false)
        image = view?.findViewById(R.id.imageview_category)
        nameEdit = view?.findViewById(R.id.edittext_category)
        btnCerrar = view?.findViewById(R.id.btnCerrarlayout)
        btnAddUpdate = view?.findViewById(R.id.btn_create_category)

        arguments?.let {
            nameEdit?.setText(it.getString("name"))
            Glide.with(requireContext()).load(it.getString("image")).into(image!!)
            btnAddUpdate?.setText("modificar")
        }

        image?.setOnClickListener { selectImage() }

        btnCerrar?.setOnClickListener { dismiss() }
        return view
    }

    val startImageForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result: ActivityResult ->
        val resultCode = result.resultCode
        val data = result.data

        if (resultCode == Activity.RESULT_OK){
            val fileUri = data?.data
            imageFile = File(fileUri?.path) // el archivo que vamos a guardar como imagen en el servidor
            image?.setImageURI(fileUri)
        }
        else if (resultCode == ImagePicker.RESULT_ERROR){
            Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_LONG).show()
        }
        else {
            Toast.makeText(requireContext(), "La tarea se cancelo", Toast.LENGTH_LONG).show()
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
}