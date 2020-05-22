package com.example.entrega1proyecto

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.activity_item_details.*
import kotlinx.android.synthetic.main.popup.view.*
import java.io.Serializable

class ItemDetails : AppCompatActivity() {

    var item: Item? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_details)

        item = intent.getSerializableExtra("item to watch")!! as Item

        nombreItemTextView.text = item!!.nameItem
        createdAtTextView.text = item!!.fechaCreacion
        fechaPlazoTextView.setText(item!!.plazo)

        if (item!!.estado){
            button3.text = "Volver a no completado"
        }
        else{
            button3.text = "Completar"
        }

        if (item!!.prioridad){
            NotPriorityImageView.visibility = View.GONE
            PriorityImageView.visibility = View.VISIBLE
        }
        else{
            NotPriorityImageView.visibility = View.VISIBLE
            PriorityImageView.visibility = View.GONE
        }
        notasItemEditText.setText(item!!.notasItem)
    }

    fun goBackToItemsView(view: View){
        val myIntent: Intent = Intent()
        updateItem()
        myIntent.putExtra("item updated",item as Serializable)
        setResult(5, myIntent)
        finish()
    }

    fun deleteItem(view: View){
        val myIntent: Intent = Intent()
        updateItem()
        myIntent.putExtra("item updated","NONE")
        setResult(5, myIntent)
        finish()
    }

    fun updateItem(){
        item!!.nameItem =  nombreItemTextView.text.toString()
        item!!.plazo = fechaPlazoTextView.text.toString()
        item!!.prioridad = NotPriorityImageView.visibility != View.VISIBLE
        item!!.notasItem = notasItemEditText.text.toString()
        if (button3.text == "Volver a no completado"){
            item!!.isShown = false
            item!!.estado = true
        }
        else{
            item!!.isShown = false
            item!!.estado = false
        }
    }

    fun modifyCompletion(view: View){
        if (button3.text == "Volver a no completado"){
            button3.text = "Completar"
        }
        else{
            button3.text = "Volver a no completado"
        }
    }

    fun editItemName(view: View){
        // We show a Dialog to ask the new name
        var builder: AlertDialog.Builder = AlertDialog.Builder(this)
        var inflater: LayoutInflater = layoutInflater
        var view: View = inflater.inflate(R.layout.popup,null)
        view.listNameTextView.hint = "Nombre del Item"
        builder.setView(view)
        builder.setNegativeButton("Cancelar", object: DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                dialog?.dismiss()
            }
        })
        builder.setPositiveButton("Confirmar",object:  DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                item?.nameItem = view.listNameTextView.getText().toString()
                nombreItemTextView.text = item?.nameItem
                dialog?.dismiss()
            }
        })
        var dialog: Dialog = builder.create()
        dialog.show()
    }

    fun changePriority(view: View){
        if (NotPriorityImageView.visibility == View.VISIBLE){
            NotPriorityImageView.visibility = View.GONE
            PriorityImageView.visibility = View.VISIBLE
        }
        else{
            NotPriorityImageView.visibility = View.VISIBLE
            PriorityImageView.visibility = View.GONE
        }
    }
}
