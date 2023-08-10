package com.eduardo.kotlinudemydelivery.fragments.restaurant.ingredientes

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.databinding.RestaurantFragmentIngredientesListBinding
import androidx.appcompat.app.AlertDialog
import com.eduardo.kotlinudemydelivery.Providers.IngredienteProvider
import com.eduardo.kotlinudemydelivery.adapters.IngredientsRestaurantListAdapter
import com.eduardo.kotlinudemydelivery.models.Ingrediente
import com.eduardo.kotlinudemydelivery.models.ResponseHttp
import com.eduardo.kotlinudemydelivery.models.User
import com.eduardo.kotlinudemydelivery.utils.SharedPref
import com.google.gson.Gson
import com.tommasoberlose.progressdialog.ProgressDialogFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class RestaurantIngredientesListFragment : Fragment() {

    private var _binding: RestaurantFragmentIngredientesListBinding? = null
    private val binding get() = _binding!!
    var dialog: androidx.appcompat.app.AlertDialog? = null
    var nameIngrediente: EditText? = null
    var ingredienteProvider: IngredienteProvider? = null
    var sharedPref: SharedPref? = null
    var user: User? = null
    var ingredientes = ArrayList<Ingrediente>()
    var adapterIngre : IngredientsRestaurantListAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = RestaurantFragmentIngredientesListBinding.inflate(inflater,container,false)

        sharedPref = SharedPref(requireActivity())
        getUserFromSession()
        ingredienteProvider = IngredienteProvider(user?.sessionToken!!)

        binding.abrirFormIngrediente.setOnClickListener { showDialogIngrediente() }

        getIngredientes()
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun showDialogIngrediente(){
        var builder = AlertDialog.Builder(requireContext())
        val view2 = layoutInflater.inflate(R.layout.fragment_ingrediente_dialog,null)

        builder.setView(view2)
        dialog = builder.create()
        dialog?.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.show()

        val cerrar = view2.findViewById<ImageView>(R.id.btnCerrarlayout)
        val addIngrediente = view2.findViewById<Button>(R.id.btn_create_ingre)
        nameIngrediente = view2.findViewById<EditText>(R.id.et_ingrediente_name)

        cerrar?.setOnClickListener { dialog?.dismiss() }
        addIngrediente.setOnClickListener {
            val name = nameIngrediente?.text.toString()
            if (!name.isNullOrBlank()){
                val ingredienteM = Ingrediente(
                    name_ingrediente = name,
                    is_available = true
                )

                createIngrediente(ingredienteM)
            }else{
                Toast.makeText(requireContext(), "Ingrese un nombre para el ingrediente", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createIngrediente(ingrediente: Ingrediente){
        ProgressDialogFragment.showProgressBar(requireActivity())

        ingredienteProvider?.createIngre(ingrediente)?.enqueue(object : Callback<ResponseHttp>{
            override fun onResponse(call: Call<ResponseHttp>, response: Response<ResponseHttp>) {
                ProgressDialogFragment.hideProgressBar(requireActivity())
                if (response.body()?.isSuccess == true){
                    dialog?.dismiss()
                    Toast.makeText(requireContext(), response.body()?.message, Toast.LENGTH_SHORT).show()
                }else{
                    ProgressDialogFragment.hideProgressBar(requireActivity())
                    Toast.makeText(requireContext(), response.body()?.message, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseHttp>, t: Throwable) {
                ProgressDialogFragment.hideProgressBar(requireActivity())
            }

        })
    }

    private fun getIngredientes(){
        ingredienteProvider?.getAll()?.enqueue(object: Callback<ArrayList<Ingrediente>>{
            override fun onResponse(
                call: Call<ArrayList<Ingrediente>>,
                response: Response<ArrayList<Ingrediente>>
            ) {
                if (response.body() != null){
                    ingredientes = response.body()!!
                    adapterIngre = IngredientsRestaurantListAdapter(requireActivity(),ingredientes)
                    binding.recyclerviewIngredientesList?.adapter = adapterIngre
                }
            }

            override fun onFailure(call: Call<ArrayList<Ingrediente>>, t: Throwable) {

            }

        })
    }

    private fun getUserFromSession(){
        val gson = Gson()
        if (!sharedPref?.getData("user").isNullOrBlank()){
            //si el usuario exite en sesion
            user = gson.fromJson(sharedPref?.getData("user"), User::class.java)
        }
    }

}