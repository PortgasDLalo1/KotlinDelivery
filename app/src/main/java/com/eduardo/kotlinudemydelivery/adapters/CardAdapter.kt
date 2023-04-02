package com.eduardo.kotlinudemydelivery.adapters

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.activities.client.orders.checkout.ClientOrderCheckOutActivity
import com.eduardo.kotlinudemydelivery.models.Cards
import com.eduardo.kotlinudemydelivery.utils.SharedPref
import com.google.gson.Gson

class CardAdapter(val context: Activity, val cards: ArrayList<Cards>): RecyclerView.Adapter<CardAdapter.CardViewHolder>() {
    val TAG = "ClienteCard"
    val sharedPref = SharedPref(context)
    var gson = Gson()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cardview_card, parent, false)
        return CardViewHolder(view)
    }

    override fun getItemCount(): Int {
        return cards.size
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {

        val card = cards[position]
        if (card.number_card == "Efectivo") {
            holder.textNumberCard.text = "Efectivo"
            holder.imageCard.setImageResource(R.drawable.efectivo)
        } else{
            val ultimo4 = card.number_card?.substring(15)
            val primerDigito = card.number_card?.substring(0, 1)
            holder.textNumberCard.text = "**** $ultimo4"
            Log.d(TAG, "$primerDigito")
            if (primerDigito == "5") {
                holder.imageCard.setImageResource(R.drawable.mastercard)
            } else if (primerDigito == "4") {
                holder.imageCard.setImageResource(R.drawable.visa)
            } else if (primerDigito == "4") {
                holder.imageCard.setImageResource(R.drawable.american_express)
            } else {
                holder.imageCard.setImageResource(R.drawable.tarjeta)
            }
        }

        holder.itemView.setOnClickListener {
            saveCard(card.toJson())
            goToCheckOut()
        }


    }

    private fun goToCheckOut(){
        val i = Intent(context,ClientOrderCheckOutActivity::class.java)
        context.startActivity(i)
    }

    private fun saveCard(data: String){
        val metodo = gson.fromJson(data,Cards::class.java)
        sharedPref.save("card", metodo)
    }

    class CardViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val textNumberCard: TextView
        val imageCard: ImageView
        val editTextCvv: EditText
        val imageArrow: ImageView

        init {
            textNumberCard = view.findViewById(R.id.textview_number_card2)
            imageCard = view.findViewById(R.id.imageView_icon_card2)
            editTextCvv = view.findViewById(R.id.edittext_cvv_list)
            imageArrow = view.findViewById(R.id.imageView_arrow)

            editTextCvv.visibility = View.INVISIBLE
        }
    }
}