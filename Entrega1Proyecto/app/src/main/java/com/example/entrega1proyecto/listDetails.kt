package com.example.entrega1proyecto

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.entrega1proyecto.ListaActivity.Companion.LISTS
import kotlinx.android.synthetic.main.activity_list_details.*
import kotlinx.android.synthetic.main.items_template.*
import kotlinx.android.synthetic.main.items_template.view.*
import kotlinx.android.synthetic.main.items_template.view.checkBox
import kotlinx.android.synthetic.main.popup.*
import kotlinx.android.synthetic.main.popup.view.*
import kotlinx.android.synthetic.main.popup_to_create_item.*
import kotlinx.android.synthetic.main.popup_to_create_item.view.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class listDetails : AppCompatActivity(), OnSpecificItemClickListener {

    //CAMBIAR EL TIPO DE ELEMENTOS DENTRO DE LA LISTA
    val itemsOnList: ArrayList<Item> = ArrayList()
    var list: ListaItem? = null
    var prioritario = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_details)
        itemsRecyclerView.adapter = AdaptadorItemsCustom(itemsOnList, this)
        itemsRecyclerView.layoutManager = LinearLayoutManager(this)
        list = intent.getSerializableExtra(LISTS)!! as ListaItem
        createItems(list!!)
        nombreListaTextView.text = list?.name
    }

    fun createItems(list: ListaItem){
        if (list.items != null) {
            list.items?.forEach {
                itemsOnList.add(it)
                itemsRecyclerView.adapter?.notifyItemInserted(itemsOnList.size - 1)
            }
        }
    }



    fun anadirItem(view: View){
        var builder: AlertDialog.Builder = AlertDialog.Builder(this)
        var inflater: LayoutInflater = layoutInflater
        var view: View = inflater.inflate(R.layout.popup_to_create_item,null)
        builder.setView(view)
        builder.setNegativeButton("Cancelar", object: DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                dialog?.dismiss()
            }
        })
        builder.setPositiveButton("Confirmar",object:  DialogInterface.OnClickListener{
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onClick(dialog: DialogInterface?, which: Int) {
                if (view.priorityCheckBox.isChecked){
                    prioritario = true
                }
                val current = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                val formatted = current.format(formatter)
                var newItem = Item(nameItem = view.itemNameEditText.getText().toString(),estado = false,
                    prioridad= prioritario, plazo= view.plazoEditText.getText().toString(),
                    notasItem= view.descripcionEditText.getText().toString(),fechaCreacion= formatted)
                itemsOnList.add(newItem)
                itemsRecyclerView.adapter?.notifyItemInserted(itemsOnList.size -1)
                dialog?.dismiss()
            }
        })
        var dialog: Dialog = builder.create()
        dialog.show()
    }

    fun editarListaName(view: View){
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
                list?.name = view.listNameTextView.getText().toString()
                nombreListaTextView.text = list?.name
                dialog?.dismiss()
            }
        })
        var dialog: Dialog = builder.create()
        dialog.show()
    }

    fun goBackToListsView(view:View){
        val myIntent: Intent = Intent()
        list = ListaItem(list!!.name, itemsOnList)
        myIntent.putExtra("listaItems",list)
        myIntent.putExtra("username",nombreListaTextView.text)
        setResult(Activity.RESULT_OK, myIntent)
        finish()
    }


    //FALTA IMPLEMENTAR LOS ITEMS DENTRO DE LA LISTA
    override fun onSpecificItemCLicked(result: Item, check:CheckBox) {
        var itemModifiedPosition = itemsOnList.indexOf(result)
        result.estado = check.isChecked
        itemsOnList[itemModifiedPosition] = result
    }

}
