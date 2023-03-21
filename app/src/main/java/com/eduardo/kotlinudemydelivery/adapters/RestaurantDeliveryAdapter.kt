package com.eduardo.kotlinudemydelivery.adapters

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.eduardo.kotlinudemydelivery.Providers.UsersProvider
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.activities.restaurant.config.delivery.RestaurantDeliveryConfigActivity
import com.eduardo.kotlinudemydelivery.activities.restaurant.config.delivery.RestaurantDeliveryFormActivity
import com.eduardo.kotlinudemydelivery.models.ResponseHttp
import com.eduardo.kotlinudemydelivery.models.User
import de.hdodenhof.circleimageview.CircleImageView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RestaurantDeliveryAdapter(val context: Activity, val deliveries: ArrayList<User>): RecyclerView.Adapter<RestaurantDeliveryAdapter.RestaurantDeliveryViewholder>() {

    var usersProvider = UsersProvider()


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RestaurantDeliveryViewholder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_delivery, parent, false)
        return RestaurantDeliveryViewholder(view)
    }

    override fun onBindViewHolder(
        holder: RestaurantDeliveryViewholder,
        position: Int
    ) {
        val delivery = deliveries[position]
        holder.textNameDelivery.text = "${delivery.name} ${delivery.lastname}"
        holder.textPhoneDelivery.text = delivery.phone
        Glide.with(context).load(delivery.image).into(holder.circleViewDelivery)

        holder.itemView.setOnClickListener {
            //goToDeliveryForm(delivery)
        }

        holder.itemView.setOnLongClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Eliminar")
            builder.setMessage("Desea eliminar el repartidor? \n${delivery.name}")
            builder.setPositiveButton("Si", DialogInterface.OnClickListener { dialog, which ->
                usersProvider.deleteDelivery(delivery.id?.toInt()!!)?.enqueue(object : Callback<ResponseHttp>{
                    override fun onResponse(
                        call: Call<ResponseHttp>,
                        response: Response<ResponseHttp>
                    ) {
                        (context as RestaurantDeliveryConfigActivity).getDelivery()
                        Toast.makeText(context, "Repartidor Eliminado", Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailure(call: Call<ResponseHttp>, t: Throwable) {
                        Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }

                })
            })
            builder.setNegativeButton("no", DialogInterface.OnClickListener { dialog, which ->  })
            builder.show()
            return@setOnLongClickListener true
        }
    }

    override fun getItemCount(): Int {
        return deliveries.size
    }

    private fun goToDeliveryForm(delivery: User){
        val i = Intent(context, RestaurantDeliveryFormActivity::class.java)
//        i.putExtra("delivery", delivery.toJson())
//        i.putExtra("bandera","2")
        context.startActivity(i)
    }

    class RestaurantDeliveryViewholder(view: View): RecyclerView.ViewHolder(view){
        val textNameDelivery: TextView
        val textPhoneDelivery: TextView
        val circleViewDelivery: CircleImageView

        init {
            textNameDelivery = view.findViewById(R.id.textview_nameDelivery)
            textPhoneDelivery = view.findViewById(R.id.textview_phoneDelivery)
            circleViewDelivery = view.findViewById(R.id.circleimage_delivery)
        }
    }

}