package com.example.entrega1proyecto

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import androidx.annotation.MainThread
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
import okhttp3.internal.wait
import java.io.Serializable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import kotlin.collections.ArrayList

class listDetails : AppCompatActivity(), OnSpecificItemClickListener {

    var itemsOnList: ArrayList<Item> = ArrayList()
    var copyItemsOnList: ArrayList<Item> = ArrayList()
    var list: ListaItem = ListaItem("", ArrayList())
    var prioritario = false
    var shown = false
    var itemModificadoPos = -1
    var itemModified: Item? =  null
    var isShowingDialogAdd = false
    var isShowingDialogEdit = false
    var dialogEdit: Dialog? = null
    var dialogAdd: Dialog? = null
    // Db
    lateinit var database: ListDao
    lateinit var adapter: AdaptadorItemsCustom
    var listId: Long = (-1).toLong()
    var map = hashMapOf<Item, ItemBDD>()
    var itemsCounter = 1.toLong()
    lateinit var listBeingUsed: ListBDD

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_details)
        // We set the adapter for his activity
        adapter = AdaptadorItemsCustom(this)
        itemsRecyclerView.adapter = adapter
        itemsRecyclerView.layoutManager = LinearLayoutManager(this)

        // Here we create the db
        database = Room.databaseBuilder(this, Database::class.java, "ListsBDD").build().ListDao()

        // Here we obtain the id of the list we are inside
        listBeingUsed = intent.getSerializableExtra(LISTS)!! as ListBDD
        listId = listBeingUsed.id

        // We obtain the array of list
        GetTheList(this).execute(listId)

        // If the activity haven't changed the orientation
/*
        if(savedInstanceState == null) {
            createItems(list!!)
        }

*/
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

        SwitchItemsChecked.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
            if (b){
                itemsOnList.forEach {
                    var itemModifiedPosition = itemsOnList.indexOf(it)
                    it.isShown = !it.isShown
                    adapter.notifyItemChanged(itemModifiedPosition)
                }
            }
            else{
                itemsOnList.forEach {
                    var itemModifiedPosition = itemsOnList.indexOf(it)
                    it.isShown = !it.isShown
                    adapter.notifyItemChanged(itemModifiedPosition)
                }
            }
        }

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
                adapter.notifyItemMoved(sourcePosition, targetPosition)
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
                    if (itemsOnList.size == 0){
                        itemsOnList= data.getSerializableExtra("all items back") as ArrayList<Item>
                    }
                    itemModified = data.getSerializableExtra("item updated") as Item
                    itemModificadoPos = data.getIntExtra("item position modified", -1)
                    itemsOnList[itemModificadoPos] = itemModified!!
                    UpdateMap(this).execute()
                    adapter.notifyItemChanged(itemModificadoPos)
                }catch (e: Exception){
                    val copy = data.getSerializableExtra("copy item") as Item
                    map.remove(copy)
                    itemsOnList.removeAt(itemModificadoPos)
                    adapter.notifyItemRemoved(itemModificadoPos)
                }

            }
        }
    }

    // We set the items on the list in this activity
    fun createItems(list: ListaItem){
        if (list.items != null) {
            list.items?.forEach {
                itemsOnList.add(it)
                adapter.notifyItemInserted(itemsOnList.size - 1)
            }
        }
    }

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
                var newItem = Item(nameItem = view.itemNameEditText.text.toString(),estado = false,
                    prioridad= prioritario, plazo= view.plazoEditText.text.toString(),
                    notasItem= view.descripcionEditText.text.toString(),
                    fechaCreacion= formatted, isShown = shown)

                //Here we add the item to the bdd
                InsertItem(this@listDetails).execute(newItem)

                // We add it to the array of items
                itemsOnList.add(newItem)
                adapter.notifyItemInserted(itemsOnList.size -1)
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
                list?.name = view.listNameTextView.text.toString()
                listBeingUsed.name = view.listNameTextView.text.toString()
                nombreListaTextView.text = list?.name
                UpdateNameList(this@listDetails).execute(list)
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
                adapter.notifyItemChanged(itemModifiedPosition)
                map[it]!!.isShown = it.isShown
                UpdateSpecificItem(this).execute(map[it])
            }
        }
        list = ListaItem(list!!.name, itemsOnList)
        myIntent.putExtra("listaItems",list)
        setResult(Activity.RESULT_OK, myIntent)
        finish()
    }

    // We update the item if it was clicked
    override fun onSpecificItemCLicked(result: Item, check:CheckBox) {
        var itemModifiedPosition = itemsOnList.indexOf(result)
        //------------------------------------------------------------------------------------------------------------
        // Esto no esta funcionando para cuando modificamos un item y luego lo clickeamos para decir que esta terminado
        var itemBDDModified = map[result]
        //------------------------------------------------------------------------------------------------------------
        if(result.estado){
            result.estado = check.isChecked
            result.isShown = check.isChecked
        }
        else {
            result.estado = check.isChecked
            result.isShown = !check.isChecked
        }

        itemBDDModified!!.estado = result.estado
        itemBDDModified!!.isShown = result.isShown
        map[result] = itemBDDModified
        // We save it on the list ot items
        itemsOnList[itemModifiedPosition] = result
        adapter.notifyItemChanged(itemModifiedPosition)
        UpdateSpecificItem(this).execute(itemBDDModified)
    }

    // To go to the item details activity
    override fun onEyeItemCLicked(result: Item) {
        val intent = Intent(this, ItemDetails::class.java)
        itemModificadoPos = itemsOnList.indexOf(result)
        intent.putExtra("item to watch", result)
        intent.putExtra("item recorded", result as Serializable)
        intent.putExtra("item from db", map[result])
        intent.putExtra("Item position", itemModificadoPos)
        intent.putExtra("all items", itemsOnList as Serializable)
        startActivityForResult(intent, 4)
    }

    // If the state is changed we need to pass the important data to don't lose it
    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putSerializable("lista listas",itemsOnList)
        savedInstanceState.putBoolean("IS_SHOWING_DIALOG_EDIT", isShowingDialogEdit)
        savedInstanceState.putBoolean("IS_SHOWING_DIALOG_ADD", isShowingDialogAdd)
    }

    // Here we recover the data when the state is changed
    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        /*copyItemsOnList = savedInstanceState?.getSerializable("lista listas") as ArrayList<Item>
        copyItemsOnList.forEach {
            itemsOnList.add(it)
            adapter.notifyItemInserted(itemsOnList.size - 1)
        }
         */
    }

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
                adapter.notifyItemChanged(itemModifiedPosition)
                map[it]!!.isShown = it.isShown
                UpdateSpecificItem(this).execute(map[it])
            }
        }
        list = ListaItem(list!!.name, itemsOnList)
        myIntent.putExtra("listaItems",list)
        setResult(Activity.RESULT_OK, myIntent)
        finish()
        super.onBackPressed()
    }

    companion object {
        class GetTheList(private val listaActivity: listDetails) :
            AsyncTask<Long, Void, ListWithItems>() {
            override fun doInBackground(vararg params: Long?): ListWithItems {
                val idBUsc = params[0]!!
                println("${listaActivity.database.getListWithItems()}")
                val x = listaActivity.database.getSpecificList(idBUsc)

                val testItemsList = ArrayList(listaActivity.database.getAllItems())

                if (testItemsList.size > 0 && listaActivity.itemsCounter == 1.toLong()) {
                    listaActivity.itemsCounter = testItemsList[testItemsList.lastIndex].id + 1
                }

                return x
            }

            override fun onPostExecute(result: ListWithItems?) {
                if (result != null) {
                    if (listaActivity.itemsOnList.size != 0){
                        listaActivity.itemsOnList = ArrayList()
                    }
                    listaActivity.list = ListaItem(result!!.list.name, ArrayList())
                    result!!.items?.forEach {
                        val itemAdded = Item(
                            it.nameItem, it.estado, it.prioridad, it.plazo,
                            it.notasItem, it.fechaCreacion, it.isShown
                        )
                        listaActivity.list.items!!.add(itemAdded)
                        listaActivity.itemsOnList.add(itemAdded)
                        listaActivity.map[itemAdded] = it
                    }
                }
                listaActivity.adapter.setData(listaActivity.itemsOnList)
                // We set the name of the list
                listaActivity.nombreListaTextView.text = listaActivity.list?.name
            }
        }

        class InsertItem(private val listaActivity: listDetails) :
            AsyncTask<Item, Void, Void>() {
            override fun doInBackground(vararg params: Item?): Void? {
                val itemForBDD = ItemBDD(
                    listaActivity.itemsCounter,
                    listaActivity.listId, params[0]!!.nameItem, params[0]!!.estado,
                    params[0]!!.prioridad, params[0]!!.plazo!!,
                    params[0]!!.notasItem!!,
                    params[0]!!.fechaCreacion, params[0]!!.isShown
                )
                listaActivity.map[params[0]!!] = itemForBDD
                listaActivity.itemsCounter = listaActivity.database.insertItem(itemForBDD) + 1
                return null
            }
        }

        class UpdateNameList(private val listaActivity: listDetails):
                AsyncTask<ListaItem, Void, Void>(){
            override fun doInBackground(vararg params: ListaItem?): Void? {
                listaActivity.database.updateList(listaActivity.listBeingUsed)
                return null
            }
        }

        class UpdateSpecificItem(private val listaActivity: listDetails):
            AsyncTask<ItemBDD, Void, Void>() {
            override fun doInBackground(vararg params: ItemBDD?): Void? {
                listaActivity.database.updateItem(params[0]!!)
                return null
            }
        }

        class UpdateMap(private val listaActivity: listDetails):
            AsyncTask<Void, Void, List<ItemBDD>?>(){
            override fun doInBackground(vararg params: Void?): List<ItemBDD>? {
                listaActivity.map = hashMapOf()
                return listaActivity.database.getAllItems()
            }

            override fun onPostExecute(result: List<ItemBDD>?) {
                var count = 0
                result!!.forEach{
                    listaActivity.map[listaActivity.itemsOnList[count]] = it
                    count += 1
                }
            }

        }
    }
}