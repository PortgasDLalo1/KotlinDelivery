package com.eduardo.kotlinudemydelivery.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.eduardo.kotlinudemydelivery.R
import com.eduardo.kotlinudemydelivery.models.Ingrediente

class IngredientsRestaurantListAdapter(val context: Activity, val ingrediente: ArrayList<Ingrediente>): RecyclerView.Adapter<IngredientsRestaurantListAdapter.IngredientRestaurantViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): IngredientRestaurantViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cardview_ingredient, parent, false)
        return  IngredientRestaurantViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: IngredientRestaurantViewHolder,
        position: Int
    ) {
        val ingre = ingrediente[position]
        holder.tvIngrediente.text = ingre.name_ingrediente
    }

    override fun getItemCount(): Int {
        return ingrediente.size
    }

    class IngredientRestaurantViewHolder(view: View): RecyclerView.ViewHolder(view){
        val tvIngrediente: TextView

        init {
            tvIngrediente = view.findViewById(R.id.tv_ingrediente)
        }
    }
}