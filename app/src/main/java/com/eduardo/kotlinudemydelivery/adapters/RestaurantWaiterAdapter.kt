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

class RestaurantWaiterAdapter(val context: Activity, val waiters: ArrayList<User>): RecyclerView.Adapter<RestaurantWaiterAdapter.RestaurantWaiterViewholder>() {

    var usersProvider = UsersProvider()


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RestaurantWaiterViewholder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_waiter, parent, false)
        return RestaurantWaiterViewholder(view)
    }

    override fun onBindViewHolder(
        holder: RestaurantWaiterViewholder,
        position: Int
    ) {
        val waiter = waiters[position]
        holder.textNameWaiter.text = "${waiter.name} ${waiter.lastname}"
        holder.textPhoneWaiter.text = waiter.phone
        Glide.with(context).load(waiter.image).into(holder.circleViewWaiter)

        holder.itemView.setOnClickListener {
            //goToDeliveryForm(delivery)
        }

        holder.itemView.setOnLongClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Eliminar")
            builder.setMessage("Desea eliminar el mesero? \n${waiter.name}")
            builder.setPositiveButton("Si", DialogInterface.OnClickListener { dialog, which ->
                usersProvider.deleteDelivery(waiter.id?.toInt()!!)?.enqueue(object : Callback<ResponseHttp>{
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
        return waiters.size
    }

    private fun goToWaiterForm(delivery: User){
        val i = Intent(context, RestaurantDeliveryFormActivity::class.java)
//        i.putExtra("delivery", delivery.toJson())
//        i.putExtra("bandera","2")
        context.startActivity(i)
    }

    class RestaurantWaiterViewholder(view: View): RecyclerView.ViewHolder(view){
        val textNameWaiter: TextView
        val textPhoneWaiter: TextView
        val circleViewWaiter: CircleImageView

        init {
            textNameWaiter = view.findViewById(R.id.textview_nameWaiter)
            textPhoneWaiter = view.findViewById(R.id.textview_phoneWaiter)
            circleViewWaiter = view.findViewById(R.id.circleimage_waiter)
        }
    }

}