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
import java.lang.Exception

class ItemDetails : AppCompatActivity() {

    var item: Item? = null
    var pos = -1
    var isShowingDialog = false
    var dialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_details)

        try {
            item = savedInstanceState?.getSerializable("Item") as Item
            pos = savedInstanceState?.getInt("Item Mod Position")
        }
        catch(e:Exception){
            item = intent.getSerializableExtra("item to watch")!! as Item
            pos = intent.getIntExtra("Item position", -1)
        }

        if(savedInstanceState!=null){
            isShowingDialog = savedInstanceState.getBoolean("IS_SHOWING_DIALOG", false)
            if(isShowingDialog){
                editItemName(View(this))
            }
        }

        nombreItemTextView.text = item!!.nameItem
        createdAtTextView.text = item!!.fechaCreacion
        if(item!!.plazo == ""){
            fechaPlazoTextView.hint = "Escriba aquí la fecha de plazo"
            fechaPlazoTextView.setText("")
        }
        else{
            fechaPlazoTextView.hint = ""
            fechaPlazoTextView.setText(item!!.plazo)
        }

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

    // When the back button is pressed, to go back to the other view
    fun goBackToItemsView(view: View){
        val myIntent: Intent = Intent()
        updateItem()
        myIntent.putExtra("item updated",item as Serializable)
        myIntent.putExtra("item position modified", pos)
        setResult(5, myIntent)
        finish()
    }

    // To delete the specific item
    fun deleteItem(view: View){
        val myIntent: Intent = Intent()
        updateItem()
        myIntent.putExtra("item updated","NONE")
        setResult(5, myIntent)
        finish()
    }

    // When we end modifying the item
    fun updateItem(){
        item!!.nameItem =  nombreItemTextView.text.toString()
        item!!.plazo = fechaPlazoTextView.text.toString()
        item!!.prioridad = NotPriorityImageView.visibility != View.VISIBLE
        item!!.notasItem = notasItemEditText.text.toString()
        if (button3.text == "Volver a no completado"){
            if(!item!!.estado) {
                item!!.isShown = false
                item!!.estado = true
            }

        }
        else{
            if(item!!.estado) {
                item!!.isShown = false
                item!!.estado = false
            }
        }
    }

    // To change the state from being completed or not completed
    fun modifyCompletion(view: View){
        if (button3.text == "Volver a no completado"){
            button3.text = "Completar"
        }
        else{
            button3.text = "Volver a no completado"
        }
    }

    // To edit the name of the item
    fun editItemName(view: View){
        // We show a Dialog to ask the new name
        var builder: AlertDialog.Builder = AlertDialog.Builder(this)
        var inflater: LayoutInflater = layoutInflater
        var view: View = inflater.inflate(R.layout.popup,null)
        view.listNameTextView.hint = "Nombre del Item"
        builder.setCancelable(false)
        builder.setView(view)

        builder.setNegativeButton("Cancelar", object: DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                dialog?.dismiss()
                isShowingDialog = false
            }
        })
        builder.setPositiveButton("Confirmar",object:  DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                item?.nameItem = view.listNameTextView.getText().toString()
                nombreItemTextView.text = item?.nameItem
                dialog?.dismiss()
                isShowingDialog = false
            }
        })
        dialog = builder.create()
        dialog!!.show()
        isShowingDialog = true
    }

    // Function to change the priority of a item, and show it or not show it
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

    // Function to maintain the data when the activity is changed of state
    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        updateItem()
        // We give the username
        savedInstanceState.putSerializable("Item", item as Serializable)
        savedInstanceState.putBoolean("IS_SHOWING_DIALOG", isShowingDialog)
        savedInstanceState.putInt("Item Mod Position", pos)
    }

    // Function to obtain what was given before changing the state of the activity
    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        // Obtain the username
        item = savedInstanceState?.getSerializable("Item") as Item
    }

    // To maintain the state of the dialog if we change the orientation of the phone and
    // the dialog was being shown
    override fun onPause() {
        if(dialog!=null && dialog!!.isShowing) {
            dialog!!.dismiss()
        }
        super.onPause()
    }

    // When the back button was pressed
    override fun onBackPressed() {
        val myIntent: Intent = Intent()
        updateItem()
        myIntent.putExtra("item updated",item as Serializable)
        myIntent.putExtra("item position modified", pos)
        setResult(5, myIntent)
        finish()
        super.onBackPressed()
    }
}
