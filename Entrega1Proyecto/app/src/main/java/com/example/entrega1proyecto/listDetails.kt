package com.example.entrega1proyecto

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.entrega1proyecto.ListaActivity.Companion.LISTS
import com.example.entrega1proyecto.model.*
import kotlinx.android.synthetic.main.activity_list_details.*
import kotlinx.android.synthetic.main.popup.view.*
import kotlinx.android.synthetic.main.popup_to_create_item.view.*
import java.io.Serializable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import kotlin.collections.ArrayList

class listDetails : AppCompatActivity(), OnSpecificItemClickListener {

    var itemsOnList: ArrayList<ItemBDD> = ArrayList()
    var copyItemsOnList: ArrayList<ItemBDD> = ArrayList()
    lateinit var list: ListWithItems
    var prioritario = false
    var shown = false
    var itemModificadoPos = -1
    var itemModified: ItemBDD? =  null
    var isShowingDialogAdd = false
    var isShowingDialogEdit = false
    var dialogEdit: Dialog? = null
    var dialogAdd: Dialog? = null
    var itemsCounter: Long = 0
    // Database
    lateinit var database: ListDao
    var idOfList: Long = (-1).toLong()
    var testItemsList: ArrayList<ItemBDD> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_details)

        database = Room.databaseBuilder(this, Database::class.java,"ListsBDD").allowMainThreadQueries().fallbackToDestructiveMigration().build().ListDao()
        testItemsList = ArrayList(database.getAllItems())



        if (testItemsList.size > 0 && itemsCounter == 0.toLong()) {
            itemsCounter = testItemsList[testItemsList.lastIndex].id
        }

        // We obtain the array of lists
        idOfList = intent.getSerializableExtra("List Id")!! as Long

        list = database.getSpecificList(idOfList)
        itemsOnList = ArrayList(list.items!!)
        println("EN EL ON CREATE ANTES DEL ADAPTER       $itemsOnList    $list")

        // We set the adapter for his activity
        itemsRecyclerView.adapter = AdaptadorItemsCustom(itemsOnList, this)
        itemsRecyclerView.layoutManager = LinearLayoutManager(this)


        // If the activity haven't changed the orientation
        /*if(savedInstanceState == null) {
            createItems(list!!)
        }*/
        if(savedInstanceState!=null){
            isShowingDialogAdd = savedInstanceState.getBoolean("IS_SHOWING_DIALOG_ADD", false)
            if(isShowingDialogAdd){
                anadirItem(View(this))
            }
            isShowingDialogEdit = savedInstanceState.getBoolean("IS_SHOWING_DIALOG_EDIT", false)
            if(isShowingDialogEdit){
                editarListaName(View(this))
            }
        }

        SwitchItemsChecked.setOnClickListener {
            var b = SwitchItemsChecked.isChecked
            if (b) {
                itemsOnList.forEach {
                    var itemModifiedPosition = itemsOnList.indexOf(it)
                    println("ANTES DEL MODIFICAR EL SWITCH ON     $itemsOnList")
                    it.isShown = !it.isShown
                    itemsRecyclerView.adapter?.notifyItemChanged(itemModifiedPosition)
                    database.insertItem(it)
                    println("Switch On!!!!!      $itemsOnList")
                }
            } else {
                itemsOnList.forEach {
                    var itemModifiedPosition = itemsOnList.indexOf(it)
                    it.isShown = !it.isShown
                    itemsRecyclerView.adapter?.notifyItemChanged(itemModifiedPosition)
                    database.insertItem(it)
                    println("Switch off!!!!!      $itemsOnList")
                }
            }
        }

        // We set the name of the list
        nombreListaTextView.text = list?.list.name

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
                    itemModified = database.getSpecificItem(data.getSerializableExtra("item updated") as Long)
                    itemModificadoPos = data.getIntExtra("item position modified", -1)
                    itemsOnList[itemModificadoPos] = itemModified!!
                    itemsRecyclerView.adapter!!.notifyItemChanged(itemModificadoPos)
                }catch (e: Exception){
                    itemsOnList.removeAt(itemModificadoPos)
                    itemsRecyclerView.adapter?.notifyItemRemoved(itemModificadoPos)
                }

            }
        }
    }

    // We set the items on the list in this activity
    /*fun createItems(list: ListWithItems){
        if (list.items != null) {
            list.items?.forEach {
                itemsOnList.add(it)
                itemsRecyclerView.adapter?.notifyItemInserted(itemsOnList.size - 1)
            }
        }
    }*/

    // Function to add items in the list
    fun anadirItem(view: View){
        // We show a popup asking the specific things that need to contain an item
        var builder: AlertDialog.Builder = AlertDialog.Builder(this)
        var inflater: LayoutInflater = layoutInflater
        var view: View = inflater.inflate(R.layout.popup_to_create_item,null)
        builder.setCancelable(false)
        builder.setView(view)

        // If they dont want to create a new item we resume what the activity was showing
        builder.setNegativeButton("Cancelar", object: DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                dialog?.dismiss()
                isShowingDialogAdd = false
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
                var newItem = ItemBDD(nameItem = view.itemNameEditText.text.toString(),estado = false,
                    prioridad= prioritario, plazo= view.plazoEditText.text.toString(),
                    notasItem= view.descripcionEditText.text.toString(),
                    fechaCreacion= formatted, isShown = shown, id= itemsCounter, listID = list.list.id)

                itemsCounter = database.insertItem(newItem)
                newItem.id = itemsCounter
                itemsCounter += 1
                // We add it to the array of items
                itemsOnList.add(newItem)
                itemsRecyclerView.adapter?.notifyItemInserted(itemsOnList.size -1)
                dialog?.dismiss()
                isShowingDialogAdd = false
            }
        })
        dialogAdd = builder.create()
        dialogAdd!!.show()
        isShowingDialogAdd = true
    }

    // Function to edit the name of the list
    fun editarListaName(view: View){
        // We show a Dialog to ask the new name
        var builder: AlertDialog.Builder = AlertDialog.Builder(this)
        var inflater: LayoutInflater = layoutInflater
        var view: View = inflater.inflate(R.layout.popup,null)
        builder.setCancelable(false)
        builder.setView(view)

        builder.setNegativeButton("Cancelar", object: DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                dialog?.dismiss()
                isShowingDialogEdit = false
            }
        })
        builder.setPositiveButton("Confirmar",object:  DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                list?.list.name = view.listNameTextView.getText().toString()
                nombreListaTextView.text = list?.list.name
                database.insertList(list.list)
                dialog?.dismiss()
                isShowingDialogEdit = false
            }
        })
        dialogEdit = builder.create()
        dialogEdit!!.show()
        isShowingDialogEdit = true
    }

    // Function to go back to the activity with the lists
    fun goBackToListsView(view:View){
        val myIntent: Intent = Intent()
        if(SwitchItemsChecked.isChecked) {
            itemsOnList.forEach {
                var itemModifiedPosition = itemsOnList.indexOf(it)
                it.isShown = !it.isShown
                itemsRecyclerView.adapter?.notifyItemChanged(itemModifiedPosition)
            }
        }
        list = ListWithItems(list!!.list, itemsOnList)
        myIntent.putExtra("Name List updated", list.list.name)
        //myIntent.putExtra("listaItems",list as Serializable)
        setResult(Activity.RESULT_OK, myIntent)
        finish()
    }

    // We update the item if it was clicked
    override fun onSpecificItemCLicked(result: ItemBDD, check:CheckBox) {
        var itemModifiedPosition = itemsOnList.indexOf(result)
        if(result.estado){
            result.estado = check.isChecked
            result.isShown = check.isChecked
        }
        else {
            result.estado = check.isChecked
            result.isShown = !check.isChecked
        }

        // We save it on the list ot items
        itemsOnList[itemModifiedPosition] = result
        itemsRecyclerView.adapter?.notifyItemChanged(itemModifiedPosition)
        database.insertItem(result)
    }

    // To go to the item details activity
    override fun onEyeItemCLicked(result: ItemBDD) {
        val intent = Intent(this, ItemDetails::class.java)
        itemModificadoPos = itemsOnList.indexOf(result)
        intent.putExtra("item to watch", result.id)
        intent.putExtra("Item position", itemModificadoPos)
        startActivityForResult(intent, 4)
    }

    // If the state is changed we need to pass the important data to don't lose it
    /*public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        println("ANTES DE LA ROTACIOOOOOON    $itemsOnList   ${database.getAllItems()}")
        //savedInstanceState.putSerializable("lista listas",itemsOnList)
        /*var count = 0
        itemsOnList.forEach {
            database.insertItem(it)
            itemsOnList[count] = it
            itemsRecyclerView.adapter?.notifyItemChanged(count)
        }*/

        savedInstanceState.putBoolean("IS_SHOWING_DIALOG_EDIT", isShowingDialogEdit)
        savedInstanceState.putBoolean("IS_SHOWING_DIALOG_ADD", isShowingDialogAdd)
    }*/

    // Here we recover the data when the state is changed
    /*override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        var counter = 0
        itemsOnList = ArrayList(database.getAllItems())
        println("Rotando la pantalla!!!!!      $itemsOnList")
        /*itemsOnList.forEach {
            if (it.estado && SwitchItemsChecked.isChecked){
                it.isShown = true
            }
            else if(it.estado && !SwitchItemsChecked.isChecked){
                it.isShown = false
            }
            else it.isShown = !(!it.estado && SwitchItemsChecked.isChecked)

            database.insertItem(it)
            itemsOnList[counter] = it
            itemsRecyclerView.adapter?.notifyItemChanged(counter)
        }*/
        /*copyItemsOnList = savedInstanceState?.getSerializable("lista listas") as ArrayList<ItemBDD>
        copyItemsOnList.forEach {
            itemsOnList.add(it)
            itemsRecyclerView.adapter?.notifyItemInserted(itemsOnList.size - 1)
        }*/
    }*/

    // To maintain the dialog states if the app state is changed
    override fun onPause() {
        if (dialogEdit != null && dialogEdit!!.isShowing) {
            dialogEdit!!.dismiss()
        }
        if (dialogAdd != null && dialogAdd!!.isShowing) {
            dialogAdd!!.dismiss()
        }
        super.onPause()
    }

    // To go back to the lists activity
    override fun onBackPressed() {
        val myIntent: Intent = Intent()
        if(SwitchItemsChecked.isChecked) {
            itemsOnList.forEach {
                var itemModifiedPosition = itemsOnList.indexOf(it)
                it.isShown = !it.isShown
                itemsRecyclerView.adapter?.notifyItemChanged(itemModifiedPosition)
            }
        }
        list = ListWithItems(list!!.list, itemsOnList)
        database.insertList(list.list)
        //myIntent.putExtra("listaItems",list as Serializable)
        setResult(Activity.RESULT_OK, myIntent)
        finish()
        super.onBackPressed()
    }
}
