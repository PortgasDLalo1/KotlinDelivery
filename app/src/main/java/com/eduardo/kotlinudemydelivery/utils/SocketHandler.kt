package com.eduardo.kotlinudemydelivery.utils

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
//import com.github.nkzawa.socketio.client.Socket
//import com.github.nkzawa.socketio.client.IO
import java.net.URISyntaxException

object SocketHandler {

    lateinit var mSocket: Socket

    @Synchronized
    fun setSocket(){
        try {
//             mSocket = IO.socket("http://10.72.6.224:3000/orders/delivery")
            mSocket = IO.socket("https://kotlin-delivery-udemy.onrender.com/")
//            mSocket = IO.socket("http://10.72.6.224:3000")
//            mSocket = IO.socket("http://192.168.3.17:3000/")
        }catch (e: URISyntaxException){
            Log.d("Error","No se pudo conectar el socket ${e.message}")
        }
    }

    @Synchronized
    fun getSocket(): Socket{
        return mSocket
    }

    @Synchronized
    fun establishConnection(){
        mSocket.connect()
    }

    @Synchronized
    fun closeConnection(){
        mSocket.disconnect()
    }

}