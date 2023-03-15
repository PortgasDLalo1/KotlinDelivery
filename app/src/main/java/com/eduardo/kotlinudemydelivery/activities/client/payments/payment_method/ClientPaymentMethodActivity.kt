package com.eduardo.kotlinudemydelivery.activities.client.payments.payment_method

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.activities.client.payments.mercado_pago.form.ClientPaymentFormActivity
import com.eduardo.kotlinudemydelivery.activities.client.payments.paypal.form.ClientPaymentsPaypalFormActivity
import com.eduardo.kotlinudemydelivery.databinding.ActivityClientPaymentMethodBinding

class ClientPaymentMethodActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClientPaymentMethodBinding

    var toolbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientPaymentMethodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toolbar = findViewById(R.id.toolbar)
        toolbar?.setTitleTextColor(ContextCompat.getColor(this,R.color.black))
        toolbar?.title = "Metodos de pago"

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.imageviewMercadopago.setOnClickListener { goToMercadoPago() }
        binding.imageviewPaypal.setOnClickListener { goToPaypal() }
    }

    private fun goToMercadoPago(){
        val i = Intent(this,ClientPaymentFormActivity::class.java)
        startActivity(i)
    }

    private fun goToPaypal(){
        val i = Intent(this,ClientPaymentsPaypalFormActivity::class.java)
        startActivity(i)
    }
}