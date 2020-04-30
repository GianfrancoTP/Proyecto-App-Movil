package com.example.entrega1proyecto


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.items_template.view.*
import kotlinx.android.synthetic.main.template.view.*

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
            view.checkBox.text = item.nameItem


            view.listaButton.setOnClickListener {
                clickListener.onSpecificItemCLicked(item)
            }
        }
    }
}

interface OnSpecificItemClickListener{
    fun onSpecificItemCLicked(result: Item)
}