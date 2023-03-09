package com.eduardo.kotlinudemydelivery.activities.client.payments.installments

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.databinding.ActivityClientPaymentsInstallmentsBinding

class ClientPaymentsInstallmentsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClientPaymentsInstallmentsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientPaymentsInstallmentsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}