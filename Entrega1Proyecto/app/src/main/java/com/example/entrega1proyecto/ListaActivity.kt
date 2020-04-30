package com.example.entrega1proyecto

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_lista.*
import kotlinx.android.synthetic.main.popup.*
import kotlinx.android.synthetic.main.popup.view.*
import kotlinx.android.synthetic.main.popup.view.listNameTextView
import kotlinx.android.synthetic.main.template.*

class ListaActivity : AppCompatActivity(), OnItemClickListener, OnTrashClickListener{

    var listaList: ArrayList<ListaItem> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista)
        recycler_view.adapter = AdaptadorCustom(listaList, this, this)
        recycler_view.layoutManager = LinearLayoutManager(this)
    }

    override fun onItemCLicked(result: ListaItem){
        val intent = Intent(this, listDetails::class.java)
        startActivity(intent)
    }

    override fun onTrashCLicked(result: ListaItem) {
        var pos = listaList.indexOf(result)
        listaList.remove(result)
        recycler_view.adapter?.notifyItemRemoved(pos)
    }


    fun plusButton(view: View){
        var builder: AlertDialog.Builder = AlertDialog.Builder(this)
        var inflater: LayoutInflater = layoutInflater
        var view: View = inflater.inflate(R.layout.popup,null)
        builder.setView(view)
        builder.setNegativeButton("Cancelar", object: DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                dialog?.dismiss()
            }
        })

        builder.setPositiveButton("Confirmar",object:  DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                listaList.add(ListaItem(view.listNameTextView.text.toString(),""))
                recycler_view.adapter?.notifyItemInserted(listaList.size - 1)
                dialog?.dismiss()
            }
        })
        var dialog: Dialog = builder.create()
        dialog.show()
    }
}
