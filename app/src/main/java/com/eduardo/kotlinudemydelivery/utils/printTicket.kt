package com.eduardo.kotlinudemydelivery.utils

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.eduardo.kotlinudemydelivery.models.Order
import com.mazenrashed.printooth.Printooth
import com.mazenrashed.printooth.data.printable.Printable
import com.mazenrashed.printooth.data.printable.TextPrintable
import com.mazenrashed.printooth.data.printer.DefaultPrinter
import com.mazenrashed.printooth.ui.ScanningActivity
import com.mazenrashed.printooth.utilities.Printing
import com.mazenrashed.printooth.utilities.PrintingCallback

class printTicket: AppCompatActivity(), PrintingCallback {

    private var printing: Printing? = null;

    override fun connectingWithPrinter() {
        TODO("Not yet implemented")
    }

    override fun connectionFailed(error: String) {
        TODO("Not yet implemented")
    }

    override fun disconnected() {
        TODO("Not yet implemented")
    }

    override fun onError(error: String) {
        TODO("Not yet implemented")
    }

    override fun onMessage(message: String) {
        TODO("Not yet implemented")
    }

    override fun printingOrderSentSuccessfully() {
        TODO("Not yet implemented")
    }

    fun initView() {

        if (printing != null){
            printing!!.printingCallback = this
        }



//            if (Printooth.hasPairedPrinter()){
//                Printooth.removeCurrentPrinter()
//                Toast.makeText(this, "Printer is not enable",Toast.LENGTH_SHORT).show()
//                Log.e("Printer", " is enable")
//            }else{
        Printooth.setPrinter("MP210","DC:0D:51:58:BF:CB")
        Toast.makeText(this, "Printer is enable",Toast.LENGTH_SHORT).show()
        Log.e("printer", " is enable")
//            }
//            changePairAndUnpair()
        /*if (Printooth.hasPairedPrinter())
            Printooth.removeCurrentPrinter()
        else{
            startActivityForResult(Intent(this@MainActivity,ScanningActivity::class.java), ScanningActivity.SCANNING_FOR_PRINTER)
            changePairAndUnpair()
        }*/


        /*binding.btnPrintImages.setOnClickListener{
            if (!Printooth.hasPairedPrinter())
                startActivityForResult(Intent(this@MainActivity, ScanningActivity::class.java), ScanningActivity.SCANNING_FOR_PRINTER)
            else
                printImage()
        }*/
    }

    fun printText(order: Order) {
        var printables = ArrayList<Printable>()

        val str = StringBuilder()
        val str2 = StringBuilder()

        var titulo = TextPrintable.Builder()
            .setText(
                "Productos        Cant      Total"
            )
            .setFontSize(DefaultPrinter.FONT_SIZE_NORMAL)
            .setLineSpacing(DefaultPrinter.LINE_SPACING_30)
            .setUnderlined(DefaultPrinter.UNDERLINED_MODE_ON)
            .setCharacterCode(DefaultPrinter.CHARCODE_PC852)
            .setNewLinesAfter(2)
            .build()
        printables.add(titulo)

        for (p in order?.products!!){
            Toast.makeText(this, p.name, Toast.LENGTH_SHORT).show()
//            str.append("${p.name}")
//            str.append(rightSpace(32,"${p.name}"))
//            str.append(p.quantity!!)
            str.append(rightSpace(18,"${p.quantity!!}",))
            str.append("${p.quantity!!}")
            str.append(rightSpace(12,"$ ${p.price}",))
            str.append("$ ${p.price}")

            var productos = TextPrintable.Builder()
                .setText(
                    "- ${p.name}\n$str"
                )
                .setFontSize(DefaultPrinter.FONT_SIZE_NORMAL)
                .setLineSpacing(DefaultPrinter.LINE_SPACING_30)
                .setCharacterCode(DefaultPrinter.CHARCODE_PC852)
                .setNewLinesAfter(1)
                .build()

            printables.add(productos)
            str.setLength(0)
        }



        Printooth.printer().print(printables)
    }

    fun initPrinting() {
        if (Printooth.hasPairedPrinter()){
            printing = Printooth.printer()
        }
        if (printing != null){
            printing!!.printingCallback = this
        }
    }

    private fun rightSpace(max: Int, name: String):  StringBuilder{
        val strS = StringBuilder()
        val totalS = name.length
        for (i in 0 .. max - name.length ){
            strS.append(".")
        }
        return strS
    }

    private fun rightSpace2(max: Int, precio: String, cantidad: Int):  StringBuilder{
        var c: Int = 0
        c = if (cantidad.toString().length == 1)
            precio.length-3
        else
            precio.length-2

        val strS = StringBuilder()
        for (i in 0 .. max -c){
            strS.append(".")
        }
        return strS
    }

    fun checaBluetooth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT))
        }
        else{
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetooth.launch(enableBtIntent)
        }
    }

    var requestBluetooth = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            //granted
            Toast.makeText(this, "Bluetooth is enable", Toast.LENGTH_SHORT).show()
        }else{
            //deny
            Toast.makeText(this, "Bluetooth is enable", Toast.LENGTH_SHORT).show()
        }
    }

    val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d("test006", "${it.key} = ${it.value}")
            }
        }

}