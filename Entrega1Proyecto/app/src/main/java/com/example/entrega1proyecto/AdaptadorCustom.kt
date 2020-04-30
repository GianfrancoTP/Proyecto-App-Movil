package com.example.entrega1proyecto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.template.view.*
import java.io.Serializable
import java.sql.Date
import java.util.*
import kotlin.collections.ArrayList

class AdaptadorCustom(private val items: ArrayList<ListaItem>, private val itemClickListener: OnItemClickListener, private val trashClickListener: OnTrashClickListener): RecyclerView.Adapter<AdaptadorCustom.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.template, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bindHistoric(item, itemClickListener, trashClickListener)
    }

    class ViewHolder(private var view: View) : RecyclerView.ViewHolder(view) {
        private var item: ListaItem? = null

        init {
        }

        fun bindHistoric(item: ListaItem, clickListener: OnItemClickListener, trashListener: OnTrashClickListener) {
            this.item = item
            view.listaButton.text = item.name
            view.trashImageButton.setOnClickListener{
                trashListener.onTrashCLicked(item)
            }

            view.listaButton.setOnClickListener {
                clickListener.onItemCLicked(item)
                println("HOLA")
            }
        }
    }
}
interface OnItemClickListener{
    fun onItemCLicked(result: ListaItem)
}

interface OnTrashClickListener{
    fun onTrashCLicked(result: ListaItem)
}

class ListaItem(var name: String, var items: ArrayList<Item>? = null): Serializable {}
class Item(var nameItem: String, estado: Boolean, prioridad:Boolean, plazo: String?, notasItem: String?, fechaCreacion: String): Serializable{}