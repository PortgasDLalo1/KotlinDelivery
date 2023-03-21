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

class RestaurantConfigFragment : Fragment() {

    var myView: View? = null

    var cardDelivery: CardView? = null
    var cardCaja: CardView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        myView = inflater.inflate(R.layout.fragment_restaurant_config, container, false)

        cardDelivery = myView?.findViewById(R.id.cardDelivery)
        cardCaja = myView?.findViewById(R.id.cardCaja)

        cardDelivery?.setOnClickListener { goToDeliveryConfig() }
        cardCaja?.setOnClickListener { goToBox() }

        return myView
    }

    private fun goToDeliveryConfig(){
        val i = Intent(requireContext(), RestaurantDeliveryConfigActivity::class.java)
        startActivity(i)
    }

    private fun goToBox(){
        val i = Intent(requireContext(), RestaurantConfigBoxActivity::class.java)
        startActivity(i)
    }

}