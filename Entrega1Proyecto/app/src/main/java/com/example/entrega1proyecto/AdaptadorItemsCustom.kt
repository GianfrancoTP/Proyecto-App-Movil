package com.example.entrega1proyecto


import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.DrawableContainer
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.entrega1proyecto.model.ItemBDD
import kotlinx.android.synthetic.main.items_template.view.*
import java.io.Serializable


// This is the adapter of the items on a list
class AdaptadorItemsCustom(private val specificItemClickListener: OnSpecificItemClickListener): RecyclerView.Adapter<AdaptadorItemsCustom.ViewHolder>() {
    private var items = ArrayList<Item>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.items_template, parent, false)
        return ViewHolder(view)
    }

    fun setData(newData: ArrayList<Item>) {
        this.items = newData
        notifyDataSetChanged()
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
                if (!view.checkBox.isChecked) {
                    view.checkBox.isChecked = true
                }
            }
            else{
                if (view.checkBox.isChecked) {
                    view.checkBox.isChecked = false
                }
            }
            if(item.isShown){
                view.checkBox.visibility = View.VISIBLE
                view.seeItemDetails.visibility = View.VISIBLE
            }
            else{
                view.checkBox.visibility = View.GONE
                view.seeItemDetails.visibility = View.GONE
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
            else{
                view.checkBox.setBackgroundResource(R.drawable.button_shape)
            }

            // To update what happens when is checked the item
            view.checkBox.setOnClickListener {
                clickListener.onSpecificItemCLicked(item, it.checkBox)
            }

            view.seeItemDetails.setOnClickListener{
                clickListener.onEyeItemCLicked(item)
            }
        }
    }
}

interface OnSpecificItemClickListener{
    fun onSpecificItemCLicked(result: Item, it:CheckBox)
    fun onEyeItemCLicked(result: Item)
}
class Item(var nameItem: String, var estado: Boolean, var prioridad:Boolean, var plazo: String?, var notasItem: String?,
           var fechaCreacion: String, var isShown: Boolean): Serializable {}

class ItemAPI(var items: List<ItemBDD>)