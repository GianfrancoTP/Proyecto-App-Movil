package com.example.entrega1proyecto

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_lista.*
import kotlinx.android.synthetic.main.popup.view.*
import java.util.*
import kotlin.collections.ArrayList


class ListaActivity : AppCompatActivity(), OnItemClickListener, OnTrashClickListener{

    var listaList: ArrayList<ListaItem> = ArrayList()
    var startingListaList: ArrayList<ListaItem> = ArrayList()
    var itemsRecibidos: ListaItem = ListaItem("")
    var modified: ListaItem = ListaItem("")
    var username: String? = null
    var validador: Boolean = false

    companion object {
        var LISTS = "LISTS"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista)
        recycler_view.adapter = AdaptadorCustom(listaList, this, this)
        recycler_view.layoutManager = LinearLayoutManager(this)
        username = intent.getStringExtra("email")!!
        if (intent?.getSerializableExtra("lista") != null) {
            validador = true
            startingListaList = intent.getSerializableExtra("lista")!! as ArrayList<ListaItem>
            createLists(startingListaList)
        }
        nombreUsuarioTextView.text = username

        //This is for the Drag and Drop ----------------------------------------------------------------------------

        val touchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val sourcePosition = viewHolder.adapterPosition
                val targetPosition = target.adapterPosition
                Collections.swap(listaList, sourcePosition, targetPosition)
                recycler_view.adapter?.notifyItemMoved(sourcePosition, targetPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                TODO("Not yet implemented")
            }
        })
        touchHelper.attachToRecyclerView(recycler_view)
        // End of Drag and Drop --------------------------------------------------------------------------------
    }

    private fun createLists(startingListaList: ArrayList<ListaItem>){
        startingListaList.forEach {
            listaList.add(it)
            recycler_view.adapter?.notifyItemInserted(listaList.size - 1)
        }
    }
    override fun onItemCLicked(result: ListaItem){
        modified = result
        val intent = Intent(this, listDetails::class.java)
        intent.putExtra(LISTS, result)
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            if (resultCode == Activity.RESULT_OK){
                itemsRecibidos = data.getSerializableExtra("listaItems") as ListaItem
                var x = listaList.indexOf(modified)
                listaList[x] = itemsRecibidos
                recycler_view.adapter?.notifyItemChanged(x)
            }
        }
    }

    override fun onTrashCLicked(result: ListaItem) {
        var pos = listaList.indexOf(result)
        listaList.remove(result)
        recycler_view.adapter?.notifyItemRemoved(pos)
    }


    fun logOutPopUp(view: View){
        var builder: AlertDialog.Builder = AlertDialog.Builder(this)
        var inflater: LayoutInflater = layoutInflater
        var view: View = inflater.inflate(R.layout.log_out_pop_up,null)
        builder.setView(view)
        builder.setNegativeButton("Cancelar", object: DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                dialog?.dismiss()
            }
        })

        builder.setPositiveButton("Confirmar",object:  DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                val myIntent = Intent()
                myIntent.putExtra("lista de listas",listaList)
                setResult(Activity.RESULT_OK, myIntent)
                dialog?.dismiss()
                finish()
            }
        })
        var dialog: Dialog = builder.create()
        dialog.show()
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
                listaList.add(ListaItem(view.listNameTextView.text.toString(),null))
                recycler_view.adapter?.notifyItemInserted(listaList.size - 1)
                dialog?.dismiss()
            }
        })
        var dialog: Dialog = builder.create()
        dialog.show()
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putString("nombreDeUsuarioTextView", nombreUsuarioTextView.text.toString())
        savedInstanceState.putSerializable("ItemModificado",modified)
        savedInstanceState.putSerializable("lista listas",listaList)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        nombreUsuarioTextView.text = savedInstanceState?.getString("nombreDeUsuarioTextView")
        startingListaList = savedInstanceState?.getSerializable("lista listas") as ArrayList<ListaItem>
        modified = savedInstanceState?.getSerializable("ItemModificado") as ListaItem
        if(!validador) {
            createLists(startingListaList)
        }
    }
}
