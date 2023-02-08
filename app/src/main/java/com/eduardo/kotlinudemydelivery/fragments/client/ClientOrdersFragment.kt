package com.eduardo.kotlinudemydelivery.fragments.client

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.adapters.TabsPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class ClientOrdersFragment : Fragment() {

    var myView: View? = null

    var viewpager: ViewPager2? = null
    var tablayout: TabLayout? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        myView = inflater.inflate(R.layout.fragment_client_orders, container, false)

        viewpager = myView?.findViewById(R.id.viewpager)
        tablayout = myView?.findViewById(R.id.tab_layout)

        tablayout?.setSelectedTabIndicatorColor(Color.GREEN)
        tablayout?.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))
        tablayout?.tabTextColors = ContextCompat.getColorStateList(requireContext(),R.color.black)
        tablayout?.tabMode = TabLayout.MODE_SCROLLABLE
        tablayout?.isInlineLabel = true

        val numberOfTabs = 4
        val adapter= TabsPagerAdapter(requireActivity().supportFragmentManager, lifecycle, numberOfTabs)
        viewpager?.adapter = adapter
        viewpager?.isUserInputEnabled = true

        TabLayoutMediator(tablayout!!,viewpager!!){ tab, position ->
            when(position){
                0 -> {
                    tab.text = "PAGADO"
                }
                1 -> {
                    tab.text = "DESPACHADO"
                }
                2 -> {
                    tab.text = "EN CAMINO"
                }
                3 -> {
                    tab.text = "ENTREGADO"
                }
            }
        }.attach()

        return myView
    }

}