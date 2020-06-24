package com.example.entrega1proyecto.model.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.entrega1proyecto.R
import kotlinx.android.synthetic.main.template.view.*
import java.io.Serializable
import kotlin.collections.ArrayList

// This is the adapter for the lists
class AdaptadorCustom(private val itemClickListener: OnItemClickListener, private val trashClickListener: OnTrashClickListener): RecyclerView.Adapter<AdaptadorCustom.ViewHolder>() {
    private var items = ArrayList<ListaItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.template, parent, false)
        return ViewHolder(
            view
        )
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bindHistoric(item, itemClickListener, trashClickListener)
    }

    fun setData(newData: ArrayList<ListaItem>) {
        this.items = newData
        notifyDataSetChanged()
    }

    class ViewHolder(private var view: View) : RecyclerView.ViewHolder(view) {
        private var item: ListaItem? = null

        init {
        }

        fun bindHistoric(item: ListaItem, clickListener: OnItemClickListener, trashListener: OnTrashClickListener) {
            this.item = item
            // We set the name of the list here
            view.listaButton.text = item.name
            // We set the trash image of the list listening to be able to delete the list
            view.trashImageButton.setOnClickListener{
                trashListener.onTrashCLicked(item)
            }
            // We set the list listening to be able to click it and go to the items activity
            view.listaButton.setOnClickListener {
                clickListener.onItemCLicked(item)
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