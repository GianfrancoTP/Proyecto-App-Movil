package com.example.entrega1proyecto


import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.items_template.view.*
import java.io.Serializable


// This is the adapter of the items on a list
class AdaptadorItemsCustom(private val items: ArrayList<Item>, private val specificItemClickListener: OnSpecificItemClickListener): RecyclerView.Adapter<AdaptadorItemsCustom.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.items_template, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bindHistoric(item, specificItemClickListener)
    }

    class ViewHolder(private var view: View) : RecyclerView.ViewHolder(view) {
        private var item: Item? = null

        init {
        }

        fun bindHistoric(item: Item, clickListener: OnSpecificItemClickListener) {
            this.item = item
            // We set the text of the items
            view.checkBox.text = item.nameItem
            // We set the item on clicked if it was clicked before
            if(item.estado) {
                view.checkBox.performClick()
            }
            // We set the priority items in different color to highlight from the others
            if(item.prioridad){
                val colors = intArrayOf(
                    Color.parseColor("#1F8EFD"),
                    Color.parseColor("#9AC4EF")
                )

                //create a new gradient color
                val gd = GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM, colors
                )

                gd.cornerRadius = 0f
                //apply the button background to newly created drawable gradient
                view.checkBox.background = gd
            }

            // To update what happens when is checked the item
            view.checkBox.setOnClickListener {
                clickListener.onSpecificItemCLicked(item, it.checkBox)
            }
        }
    }
}

interface OnSpecificItemClickListener{
    fun onSpecificItemCLicked(result: Item, it:CheckBox)
}
class Item(var nameItem: String, var estado: Boolean, var prioridad:Boolean, var plazo: String?, var notasItem: String?,
           var fechaCreacion: String): Serializable {}