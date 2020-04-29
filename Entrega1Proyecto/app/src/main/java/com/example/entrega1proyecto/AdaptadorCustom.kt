package com.example.entrega1proyecto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.template.view.*
import java.sql.Date

class AdaptadorCustom(private val items: ArrayList<ListaItem>, val itemClickListener: OnItemClickListener): RecyclerView.Adapter<AdaptadorCustom.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.template, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bindHistoric(item, itemClickListener)
    }

    class ViewHolder(private var view: View) : RecyclerView.ViewHolder(view) {
        private var item: ListaItem? = null

        init {
        }

        fun bindHistoric(item: ListaItem, clickListener: OnItemClickListener) {
            this.item = item
            view.listaButton.text = item.name

            itemView.setOnClickListener {
                clickListener.onItemCLicked(item)
            }
        }
    }
}
interface OnItemClickListener{
    fun onItemCLicked(result: ListaItem)
}

class ListaItem(var name: String, val items: String) {}
//class Item(var name: String, fechaPlazo: Date, prioridad: Boolean, notas: String){}