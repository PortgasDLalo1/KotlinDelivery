package com.eduardo.kotlinudemydelivery.activities.client.payments.mercado_pago.status

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.activities.client.home.ClientHomeActivity
import com.eduardo.kotlinudemydelivery.databinding.ActivityClientPaymentsStatusBinding
import com.eduardo.kotlinudemydelivery.models.SocketEmitPagado
import com.eduardo.kotlinudemydelivery.utils.SocketHandler
import com.eduardo.kotlinudemydelivery.utils.SocketPaymentHandler
import io.socket.client.Socket

//import com.github.nkzawa.socketio.client.Socket

class ClientPaymentsStatusActivity : AppCompatActivity() {

    private lateinit var binding : ActivityClientPaymentsStatusBinding

    var paymentMethodId = ""
    var paymentStatus = ""
    var lastFourDigits = ""
    var idOrder = ""
    var mSocket: Socket? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientPaymentsStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        paymentMethodId = intent.getStringExtra("paymentMethodId").toString()
        paymentStatus = intent.getStringExtra("paymentStatus").toString()
        lastFourDigits = intent.getStringExtra("lastFourDigits").toString()
        idOrder = intent.getStringExtra("idOrder").toString()
        connectSocket()

        if (paymentStatus == "approved"){
            binding.circleImageStatus.setImageResource(R.drawable.ic_check)
            binding.textviewStatus.text = "Tu orden fue procesada exitosamente usando ( $paymentMethodId **** $lastFourDigits ) \n\nMira el estado de tu compra en la seccion de Mis Pedidos"
            emitPago()
        }else{
            binding.circleImageStatus.setImageResource(R.drawable.ic_cancel)
            binding.textviewStatus.text = "Hubo un error procesando el pago"
        }

        binding.btnFinish.setOnClickListener { goToHome() }
    }

    private fun goToHome(){
        val i = Intent(this, ClientHomeActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(i)
    }

    private fun connectSocket(){
        SocketPaymentHandler.setSocket()
//        socket = SocketPaymentHandler.getSocket()
//        socket?.connect()
        SocketPaymentHandler.establishConnection()
    }

    private fun emitPago(){
        mSocket = SocketPaymentHandler.getSocket()
        val data = SocketEmitPagado(
            id_order = idOrder
        )

        mSocket!!.emit("pagado",data.toJson())
    }

    override fun onDestroy() {
        super.onDestroy()
        mSocket?.disconnect()
    }
}