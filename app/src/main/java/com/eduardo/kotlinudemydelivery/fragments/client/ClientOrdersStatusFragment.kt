package com.eduardo.kotlinudemydelivery.fragments.client

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eduardo.kotlinudemydelivery.Providers.OrdersProvider
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.adapters.OrdersClientAdapter
import com.eduardo.kotlinudemydelivery.databinding.FragmentClientOrdersStatusBinding
import com.eduardo.kotlinudemydelivery.models.Order
import com.eduardo.kotlinudemydelivery.models.User
import com.eduardo.kotlinudemydelivery.utils.SharedPref
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ClientOrdersStatusFragment : Fragment() {

    var myView: View? = null
    var ordersProvider: OrdersProvider? = null
    var user: User? = null
    var sharedPref: SharedPref? = null
    var recyclerViewOrders: RecyclerView? = null
    var adapter: OrdersClientAdapter? = null
    var status = ""
    private var _binding: FragmentClientOrdersStatusBinding? = null
    private val binding get() = _binding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
//        myView = inflater.inflate(R.layout.fragment_client_orders_status, container, false)

        _binding = FragmentClientOrdersStatusBinding.inflate(inflater,container,false)

        sharedPref = SharedPref(requireActivity())
        status = arguments?.getString("status")!!
        
        getUserFromSession()
        ordersProvider = OrdersProvider(user?.sessionToken!!)
//        recyclerViewOrders = myView?.findViewById(R.id.recyclerview_orders)
        binding?.recyclerviewOrders?.layoutManager = LinearLayoutManager(requireContext())

        getOrders()

        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getOrders(){
        ordersProvider?.getOrdersByClientAndStatus(user?.id!!,status)?.enqueue(object : Callback<ArrayList<Order>> {
            override fun onResponse(
                call: Call<ArrayList<Order>>,
                response: Response<ArrayList<Order>>
            ) {
                if (response.body() != null){
                    val orders = response.body()
                    adapter = OrdersClientAdapter(requireActivity(),orders!!)
                    binding?.recyclerviewOrders?.adapter = adapter
                    binding?.shimmer?.isVisible = false
                    binding?.recyclerviewOrders?.isVisible = true
                }
            }

            override fun onFailure(call: Call<ArrayList<Order>>, t: Throwable) {
                Toast.makeText(requireActivity(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
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