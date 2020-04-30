package com.example.entrega1proyecto

import android.app.Dialog
import android.content.DialogInterface
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.entrega1proyecto.ListaActivity.Companion.LISTS
import kotlinx.android.synthetic.main.activity_list_details.*
import kotlinx.android.synthetic.main.popup_to_create_item.*
import kotlinx.android.synthetic.main.popup_to_create_item.view.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class listDetails : AppCompatActivity(), OnSpecificItemClickListener {

    //CAMBIAR EL TIPO DE ELEMENTOS DENTRO DE LA LISTA
    var itemsOnList: ArrayList<Item> = ArrayList()
    var list: ListaItem? = null
    var prioritario = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_details)
        itemsRecyclerView.adapter = AdaptadorItemsCustom(itemsOnList, this)
        itemsRecyclerView.layoutManager = LinearLayoutManager(this)
        list = intent.getSerializableExtra(LISTS)!! as ListaItem
        nombreListaTextView.text = list?.name
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
                val itemprueba:Item = Item(view.itemNameEditText.getText().toString(),false,prioritario,
                    view.plazoEditText.getText().toString(), view.descripcionEditText.getText().toString(),formatted)
                itemsOnList.add(itemprueba)
                //itemsOnList.add(Item(view.itemNameEditText.getText().toString(),false,prioritario,
                //    view.plazoEditText.getText().toString(),view.descripcionEditText.getText().toString(), formatted))
                //itemsRecyclerView.adapter?.notifyItemInserted(itemsOnList.size -1)
                dialog?.dismiss()
            }
        })
        var dialog: Dialog = builder.create()
        dialog.show()
    }

    //FALTA IMPLEMENTAR LOS ITEMS DENTRO DE LA LISTA
    override fun onSpecificItemCLicked(result: Item) {
        TODO("Not yet implemented")
    }

}
