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
import android.widget.CompoundButton
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.entrega1proyecto.ListaActivity.Companion.LISTS
import kotlinx.android.synthetic.main.activity_list_details.*
import kotlinx.android.synthetic.main.items_template.*
import kotlinx.android.synthetic.main.items_template.view.*
import kotlinx.android.synthetic.main.items_template.view.checkBox
import kotlinx.android.synthetic.main.popup.*
import kotlinx.android.synthetic.main.popup.view.*
import kotlinx.android.synthetic.main.popup_to_create_item.*
import kotlinx.android.synthetic.main.popup_to_create_item.view.*
import java.lang.Error
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import kotlin.collections.ArrayList

class listDetails : AppCompatActivity(), OnSpecificItemClickListener {

    var itemsOnList: ArrayList<Item> = ArrayList()
    var copyItemsOnList: ArrayList<Item> = ArrayList()
    var list: ListaItem = ListaItem("")
    var prioritario = false
    var shown = false
    var itemModificadoPos = -1
    var itemModified: Item? =  null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_details)
        // We set the adapter for his activity
        itemsRecyclerView.adapter = AdaptadorItemsCustom(itemsOnList, this)
        itemsRecyclerView.layoutManager = LinearLayoutManager(this)
        // We obtain the array of lists
        list = intent.getSerializableExtra(LISTS)!! as ListaItem
        var switch = intent.getBooleanExtra("switch", false)
        SwitchItemsChecked.isChecked = switch
        // If the activity haven't changed the orientation
        if(savedInstanceState == null) {
            createItems(list!!)
        }

        SwitchItemsChecked.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
            if (b){
                itemsOnList.forEach {
                    var itemModifiedPosition = itemsOnList.indexOf(it)
                    it.isShown = !it.isShown
                    itemsRecyclerView.adapter?.notifyItemChanged(itemModifiedPosition)
                }
            }
            else{
                itemsOnList.forEach {
                    var itemModifiedPosition = itemsOnList.indexOf(it)
                    it.isShown = !it.isShown
                    itemsRecyclerView.adapter?.notifyItemChanged(itemModifiedPosition)
                }
            }
        }
        // We set the name of the list
        nombreListaTextView.text = list?.name

        //This is for the Drag and Drop ----------------------------------------------------------------------------

        val touchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val sourcePosition = viewHolder.adapterPosition
                val targetPosition = target.adapterPosition
                Collections.swap(itemsOnList, sourcePosition, targetPosition)
                itemsRecyclerView.adapter?.notifyItemMoved(sourcePosition, targetPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                TODO("Not yet implemented")
            }
        })
        touchHelper.attachToRecyclerView(itemsRecyclerView)
        // End of Drag and Drop --------------------------------------------------------------------------------
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            if (resultCode == 5) {
                try{
                    itemModified = data.getSerializableExtra("item updated") as Item
                    itemsOnList[itemModificadoPos] = itemModified!!
                    itemsRecyclerView.adapter?.notifyItemChanged(itemModificadoPos)
                }catch (e: Exception){
                    itemsOnList.removeAt(itemModificadoPos)
                    itemsRecyclerView.adapter?.notifyItemRemoved(itemModificadoPos)
                }

            }
        }
    }

    // We set the items on the list in this activity
    fun createItems(list: ListaItem){
        if (list.items != null) {
            list.items?.forEach {
                itemsOnList.add(it)
                itemsRecyclerView.adapter?.notifyItemInserted(itemsOnList.size - 1)
            }
        }
    }

    // Function to add items in the list
    fun anadirItem(view: View){
        // We show a popup asking the specific things that need to contain an item
        var builder: AlertDialog.Builder = AlertDialog.Builder(this)
        var inflater: LayoutInflater = layoutInflater
        var view: View = inflater.inflate(R.layout.popup_to_create_item,null)
        builder.setView(view)
        // If they dont want to create a new item we resume what the activity was showing
        builder.setNegativeButton("Cancelar", object: DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                dialog?.dismiss()
            }
        })
        // Here we create the item
        builder.setPositiveButton("Confirmar",object:  DialogInterface.OnClickListener{
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onClick(dialog: DialogInterface?, which: Int) {
                prioritario = false
                // We set if its an important item or not
                if (view.priorityCheckBox.isChecked){
                    prioritario = true
                }
                // We set the date creation date
                val current = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                val formatted = current.format(formatter)
                shown = !SwitchItemsChecked.isChecked
                // Here we create the item
                var newItem = Item(nameItem = view.itemNameEditText.getText().toString(),estado = false,
                    prioridad= prioritario, plazo= view.plazoEditText.getText().toString(),
                    notasItem= view.descripcionEditText.getText().toString(),fechaCreacion= formatted, isShown = shown)
                // We add it to the array of items
                itemsOnList.add(newItem)
                itemsRecyclerView.adapter?.notifyItemInserted(itemsOnList.size -1)
                dialog?.dismiss()
            }
        })
        var dialog: Dialog = builder.create()
        dialog.show()
    }

    // Function to edit the name of the list
    fun editarListaName(view: View){
        // We show a Dialog to ask the new name
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

    // Function to go back to the activity with the lists
    fun goBackToListsView(view:View){
        val myIntent: Intent = Intent()
        list = ListaItem(list!!.name, itemsOnList)
        myIntent.putExtra("listaItems",list)
        myIntent.putExtra("SwitchState",SwitchItemsChecked.isChecked)
        setResult(Activity.RESULT_OK, myIntent)
        finish()
    }

    // We update the item if it was clicked
    override fun onSpecificItemCLicked(result: Item, check:CheckBox) {
        var itemModifiedPosition = itemsOnList.indexOf(result)
        result.estado = check.isChecked
        result.isShown = !check.isChecked

        // We save it on the list ot items
        itemsOnList[itemModifiedPosition] = result
        itemsRecyclerView.adapter?.notifyItemChanged(itemModifiedPosition)
    }

    override fun onEyeItemCLicked(result: Item) {
        val intent = Intent(this, ItemDetails::class.java)
        itemModificadoPos = itemsOnList.indexOf(result)
        intent.putExtra("item to watch", result)
        startActivityForResult(intent, 4)
    }

    // If the state is changed we need to pass the important data to don't lose it
    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putSerializable("lista listas",itemsOnList)
    }

    // Here we recover the data when the state is changed
    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        copyItemsOnList = savedInstanceState?.getSerializable("lista listas") as ArrayList<Item>
        copyItemsOnList.forEach {
            itemsOnList.add(it)
            itemsRecyclerView.adapter?.notifyItemInserted(itemsOnList.size - 1)
        }
    }

    override fun onBackPressed() {
        val myIntent: Intent = Intent()
        list = ListaItem(list!!.name, itemsOnList)
        myIntent.putExtra("listaItems",list)
        myIntent.putExtra("SwitchState",SwitchItemsChecked.isChecked)
        setResult(Activity.RESULT_OK, myIntent)
        finish()
        super.onBackPressed()
    }
}
