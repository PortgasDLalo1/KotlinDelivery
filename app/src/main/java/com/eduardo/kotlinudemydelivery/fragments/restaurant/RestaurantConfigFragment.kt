package com.eduardo.kotlinudemydelivery.fragments.restaurant

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.activities.restaurant.config.delivery.RestaurantDeliveryConfigActivity
import com.eduardo.kotlinudemydelivery.activities.restaurant.config.box.RestaurantConfigBoxActivity
import com.eduardo.kotlinudemydelivery.activities.restaurant.config.waiter.RestaurantConfigWaiterActivity
import com.eduardo.kotlinudemydelivery.activities.sucursal.SucursalHomeActivity

class RestaurantConfigFragment : Fragment() {

    var myView: View? = null

    var cardDelivery: CardView? = null
    var cardCaja: CardView? = null
    var cardMeseros: CardView? = null
    var cardSucursal: CardView? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        myView = inflater.inflate(R.layout.fragment_restaurant_config, container, false)

        cardDelivery = myView?.findViewById(R.id.cardDelivery)
        cardCaja = myView?.findViewById(R.id.cardCaja)
        cardMeseros = myView?.findViewById(R.id.cardMeseros)
        cardSucursal = myView?.findViewById(R.id.cardSucursales)

        cardDelivery?.setOnClickListener { goToDeliveryConfig() }
        cardCaja?.setOnClickListener { goToBox() }
        cardMeseros?.setOnClickListener { goToWaiters() }
        cardSucursal?.setOnClickListener { goToSucursalConfig() }

        return myView
    }

    private fun goToSucursalConfig(){
        val i = Intent(requireContext(), SucursalHomeActivity::class.java)
        startActivity(i)
    }
    private fun goToDeliveryConfig(){
        val i = Intent(requireContext(), RestaurantDeliveryConfigActivity::class.java)
        startActivity(i)
    }

    private fun goToBox(){
        val i = Intent(requireContext(), RestaurantConfigBoxActivity::class.java)
        startActivity(i)
    }

    private fun goToWaiters(){
        val i = Intent(requireContext(), RestaurantConfigWaiterActivity::class.java)
        startActivity(i)
    }

}