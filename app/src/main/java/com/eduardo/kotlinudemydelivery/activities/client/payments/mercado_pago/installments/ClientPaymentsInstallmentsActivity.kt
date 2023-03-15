package com.eduardo.kotlinudemydelivery.activities.client.payments.mercado_pago.installments

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.eduardo.kotlinudemydelivery.Providers.MercadoPagoProvider
import com.eduardo.kotlinudemydelivery.Providers.PaymentsProvider
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.activities.client.payments.mercado_pago.status.ClientPaymentsStatusActivity
import com.eduardo.kotlinudemydelivery.adapters.ShoppingBagAdapter
import com.eduardo.kotlinudemydelivery.databinding.ActivityClientPaymentsInstallmentsBinding
import com.eduardo.kotlinudemydelivery.models.*
import com.eduardo.kotlinudemydelivery.utils.SharedPref
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.reflect.TypeToken
import com.tommasoberlose.progressdialog.ProgressDialogFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClientPaymentsInstallmentsActivity : AppCompatActivity() {

    val TAG = "ClientPa"

    private lateinit var binding: ActivityClientPaymentsInstallmentsBinding

    var mercadoPagoProvider = MercadoPagoProvider()
    var paymentsProvider: PaymentsProvider? = null

    var cardToken = ""
    var firstSixDigits = ""

    var sharedPref: SharedPref? = null
    var user: User? = null
    var selectedProducts = ArrayList<Product>()
    var gson = Gson()

    var total = 0.0

    var installmentsSelected = "" // couta seleccionada

    var address: Address? = null

    var paymentMethodId = ""
    var paymentTypeId = ""
    var issuerId = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientPaymentsInstallmentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPref = SharedPref(this)
        getUserFromSession()
        getAddressFromSession()
        paymentsProvider = PaymentsProvider(user?.sessionToken!!)

        cardToken = intent.getStringExtra("cardToken").toString()
        firstSixDigits = intent.getStringExtra("firstSixDigits").toString()

        getProductsFromSharedPref()

        getInstallments()

        binding.btnPay.setOnClickListener {
            if(!installmentsSelected.isNullOrBlank()){
                createPayment()
            }else{
                Toast.makeText(this, "Debe seleccionar el numero de cuota", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createPayment(){
        val order = Order(
            products = selectedProducts,
            id_client = user?.id!!,
            id_address = address?.id!!
        )

        val payer = Payer(
            email = user?.email!!
        )

        val mercadoPagoPayment = MercadoPagoPayment(
            order = order,
            token = cardToken,
            description = "Kotlin Delivery",
            paymentMethodId = paymentMethodId,
            paymentTypeId = paymentTypeId,
            issuerId = issuerId,
            payer = payer,
            transactionAmount = total,
            installments = installmentsSelected.toInt()
        )

        ProgressDialogFragment.showProgressBar(this)

        paymentsProvider?.create(mercadoPagoPayment)?.enqueue(object: Callback<ResponseHttp>{
            override fun onResponse(call: Call<ResponseHttp>, response: Response<ResponseHttp>) {

                ProgressDialogFragment.hideProgressBar(this@ClientPaymentsInstallmentsActivity)
                if(response.body()!=null){
                    if (response.body()?.isSuccess == true){
                        sharedPref?.remove("order")
                    }
                    
                    Log.d(TAG,"Response: $response")
                    Toast.makeText(this@ClientPaymentsInstallmentsActivity, response.body()?.message, Toast.LENGTH_LONG)
                        .show()

                    val status = response.body()?.data?.get("status")?.asString
                    val lastFour = response.body()?.data?.get("card")?.asJsonObject?.get("last_four_digits")?.asString
                    goToPaymentsStatus(paymentMethodId,status!!,lastFour!!)
                }else{
                    goToPaymentsStatus(paymentMethodId,"denied","")
                    Toast.makeText(this@ClientPaymentsInstallmentsActivity, "No hubo una respuesta exitosa", Toast.LENGTH_LONG)
                        .show()
                }
            }

            override fun onFailure(call: Call<ResponseHttp>, t: Throwable) {
                ProgressDialogFragment.hideProgressBar(this@ClientPaymentsInstallmentsActivity)
                Toast.makeText(this@ClientPaymentsInstallmentsActivity, "Error ${t.message}", Toast.LENGTH_LONG)
                    .show()
            }

        })
    }

    private fun goToPaymentsStatus(paymentMethodId: String, paymentStatus: String, lastFourDigits: String){
        val i = Intent(this, ClientPaymentsStatusActivity::class.java)
        i.putExtra("paymentMethodId", paymentMethodId)
        i.putExtra("paymentStatus", paymentStatus)
        i.putExtra("lastFourDigits", lastFourDigits)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
    }

    private fun getInstallments(){
        mercadoPagoProvider.getInstallments(firstSixDigits,"$total")?.enqueue(object: Callback<JsonArray> {
            override fun onResponse(
                call: Call<JsonArray>,
                response: Response<JsonArray>
            ) {
                if (response.body() != null){
                    val jsonInstallments = response.body()!!.get(0).asJsonObject.get("payer_costs").asJsonArray

                    val type = object: TypeToken<ArrayList<MercadoPagoInstallments>>(){}.type
                    val installments = gson.fromJson<ArrayList<MercadoPagoInstallments>>(jsonInstallments, type)

                    paymentMethodId = response.body()?.get(0)?.asJsonObject?.get("payment_method_id")?.asString!!
                    paymentTypeId = response.body()?.get(0)?.asJsonObject?.get("payment_type_id")?.asString!!
                    issuerId = response.body()?.get(0)?.asJsonObject?.get("issuer")?.asJsonObject?.get("id")?.asString!!

                    //Log.d(TAG, "response: $response")
                    //Log.d(TAG, "installments: $installments")

                    val arrayAdapter = ArrayAdapter<MercadoPagoInstallments>(this@ClientPaymentsInstallmentsActivity, android.R.layout.simple_dropdown_item_1line, installments)
                    binding.spinnerInstallments.adapter = arrayAdapter
                    binding.spinnerInstallments.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
                        override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, position: Int, l: Long) {
                            installmentsSelected = "${installments[position].installments}"
                            Log.d(TAG,"Coutas seleccionadas: $installmentsSelected")
                            binding.textviewInstallmentsDescription.text = installments[position].recommendedMessage
                        }

                        override fun onNothingSelected(p0: AdapterView<*>?) {

                        }
                    }
                }
            }

            override fun onFailure(call: Call<JsonArray>, t: Throwable) {
                //Log.d(TAG, "Error: ${t.message}")
                Toast.makeText(this@ClientPaymentsInstallmentsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getProductsFromSharedPref(){
        if (!sharedPref?.getData("order").isNullOrBlank()){ // si existe una orden en sharedpref
            val type = object: TypeToken<ArrayList<Product>>() {}.type
            selectedProducts = gson.fromJson(sharedPref?.getData("order"), type)

            for(p in selectedProducts){
                total += (p.price * p.quantity!!)
            }

            binding.textviewTotalI.text = "$ ${total}"
        }
    }

    private fun getUserFromSession(){
        val gson = Gson()
        if (!sharedPref?.getData("user").isNullOrBlank()){
            //si el usuario exite en sesion
            user = gson.fromJson(sharedPref?.getData("user"), User::class.java)
        }
    }

    private fun getAddressFromSession(){
        if (!sharedPref?.getData("address").isNullOrBlank()){
            address = gson.fromJson(sharedPref?.getData("address"),Address::class.java) // si existe una direccion
        }
    }
}