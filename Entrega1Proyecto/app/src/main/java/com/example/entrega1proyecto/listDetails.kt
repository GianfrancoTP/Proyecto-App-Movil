package com.example.entrega1proyecto

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.entrega1proyecto.ListaActivity.Companion.LISTS
import com.example.entrega1proyecto.configuration.API_KEY
import com.example.entrega1proyecto.model.*
import com.example.entrega1proyecto.model.adapters.*
import com.example.entrega1proyecto.networking.PersonApi
import com.example.entrega1proyecto.networking.UserService
import com.example.entrega1proyecto.networking.isOnline
import com.example.entrega1proyecto.utils.LocationUtil
//import com.example.entrega1proyecto.networking.loaders.GetListsFromApilistDetails
import kotlinx.android.synthetic.main.activity_list_details.*
import kotlinx.android.synthetic.main.popup.view.*
import kotlinx.android.synthetic.main.popup_to_create_item.view.*
import kotlinx.android.synthetic.main.popup_to_create_item.view.plazoEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.Serializable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.FormatStyle
import java.util.*
import kotlin.collections.ArrayList
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.share_popup.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class listDetails : AppCompatActivity(),
    OnSpecificItemClickListener {

    var itemsOnList: ArrayList<Item> = ArrayList()
    var copyItemsOnList: ArrayList<Item> = ArrayList()
    var list: ListaItem =
        ListaItem("", ArrayList())
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
    var online = false
    var onlinep = false
    var onlinef = false
    // map
    private lateinit var locationData: LocationUtil
    var lat: Double = 0.toDouble()
    var longitud: Double = 0.toDouble()
    // shared lists changed name
    var changedName = false
    var ListWithIdsItems = ArrayList<Long>()
    var podemosActualizar = true
    var funcionando = false

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

        if (listBeingUsed.isSharedList){
            getItemsFromSharedList(this, false)
        }
        else{
            // We obtain the array of list
            GetTheList(this).execute(listId)
        }

        // Contador para ir actualizando la lista
        if(listBeingUsed.isSharedList) {
            loop()
        }


        // If the activity haven't changed the orientation
/*
        if(savedInstanceState == null) {
            createItems(list!!)
        }

*/
        if(savedInstanceState!=null){
            onlinep = savedInstanceState.getBoolean("online1", false)
            isShowingDialogAdd = savedInstanceState.getBoolean("IS_SHOWING_DIALOG_ADD", false)
            if(isShowingDialogAdd){
                anadirItem(View(this))
            }
            isShowingDialogEdit = savedInstanceState.getBoolean("IS_SHOWING_DIALOG_EDIT", false)
            if(isShowingDialogEdit){
                editarListaName(View(this))
            }
        }

        onlinef = intent.getBooleanExtra("online", false)
        online = onlinep || isOnline(this) || onlinef

        if(isOnline(this) && !onlinep && !onlinef){
            online = true
            //LogFragment.GetUserFromApi(LogFragment()).execute()
            //GetListsFromApilistDetails( applicationContext, this, listId ).execute()
        }

        locationData = LocationUtil(this)

        when {
            isPermissionsGranted() -> locationData.observe(this, Observer {
                lat =  it.latitude
                longitud = it.longitude
            })

            shouldShowRequestPermissionRationale() -> println("Ask Permission")

            else -> ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION
            )
        }

        SwitchItemsChecked.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
            if (b){
                val mp = map
                map = hashMapOf()
                itemsOnList.forEach {
                    val valueTest = mp[it]
                    var itemModifiedPosition = itemsOnList.indexOf(it)
                    it.isShown = it.estado
                    valueTest!!.isShown = it.estado
                    map[it] = valueTest
                    adapter.notifyItemChanged(itemModifiedPosition)
                    UpdateSpecificItem(this, map[it])
                }
                /*map.values.forEach {
                    if(it.done) {
                        it.isShown = true
                        UpdateSpecificItem(this, it)
                    }
                    else{
                        it.isShown = false
                        UpdateSpecificItem(this, it)
                    }
                }*/
            }
            else{
                val mp = map
                map = hashMapOf()
                itemsOnList.forEach {
                    val valueTest = mp[it]
                    var itemModifiedPosition = itemsOnList.indexOf(it)
                    it.isShown = !it.estado
                    valueTest!!.isShown = it.estado
                    map[it] = valueTest
                    adapter.notifyItemChanged(itemModifiedPosition)
                    UpdateSpecificItem(this, map[it])
                }
                /*map.values.forEach {
                    if(it.done) {
                        it.isShown = false
                        UpdateSpecificItem(this, it)
                    }
                    else{
                        it.isShown = true
                        UpdateSpecificItem(this, it)
                    }
                }*/
            }
        }

        //This is for the Drag and Drop ------------------------------------------------------------------------
        val touchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0){
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                if(funcionando){
                    Thread.sleep(900)
                }
                podemosActualizar = false
                val sourcePosition = viewHolder.adapterPosition
                val targetPosition = target.adapterPosition
                if(listBeingUsed.isSharedList) {
                    map[itemsOnList[sourcePosition]]?.position = targetPosition + 1
                    map[itemsOnList[targetPosition]]?.position = sourcePosition + 1
                }
                else{
                    map[itemsOnList[sourcePosition]]?.position = targetPosition
                    map[itemsOnList[targetPosition]]?.position = sourcePosition
                }
                Collections.swap(itemsOnList, sourcePosition, targetPosition)
                adapter.notifyItemMoved(sourcePosition, targetPosition)
                ModifyPositionItems(this@listDetails, itemsOnList[sourcePosition], itemsOnList[targetPosition])
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                TODO("Not yet implemented")
            }
        })
        touchHelper.attachToRecyclerView(itemsRecyclerView)
        // End of Drag and Drop --------------------------------------------------------------------------------

        button3.setOnClickListener { shareListPopUp() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            if (resultCode == 5) {
                try{
                    onlinef = data.getBooleanExtra("online", false)
                    itemModificadoPos = data.getIntExtra("item position modified", -1)

                    //listId = data.getLongExtra("id lista", -1)
                    itemModified = data.getSerializableExtra("item updated") as Item
                    itemsOnList[itemModificadoPos] = itemModified!!
                    adapter.notifyItemChanged(itemModificadoPos)
                    UpdateMap(this).execute()
                    Thread.sleep(900)
                }catch (e: Exception){
                    itemModificadoPos = data.getIntExtra("item position modified", -1)
                    itemsOnList.removeAt(itemModificadoPos)
                    adapter.notifyItemRemoved(itemModificadoPos)
                    UpdateMap(this).execute()
                }
            }
        }
    }

    private fun isPermissionsGranted() =
        ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

    private fun shouldShowRequestPermissionRationale() =
        ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) && ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

    // Function to add items in the list
    fun anadirItem(view: View){

        if (isPermissionsGranted()){
            locationData.observe(this, Observer {
                lat =  it.latitude
                longitud = it.longitude
            })
        }
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
                if(funcionando){
                    Thread.sleep(900)
                }
                podemosActualizar = false
                prioritario = false
                // We set if its an important item or not
                if (view.priorityCheckBox.isChecked){
                    prioritario = true
                }
                // We set the date creation date
                val current = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                val formatted = current.format(formatter)
                var output: String? = null
                if (view.plazoEditText.text.toString() != ""){
                    try{
                        val formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        val dateTime = LocalDateTime.parse(view.plazoEditText.text.toString(), formatter2)
                        output = formatter2.format(dateTime)
                        }
                    catch(e: DateTimeParseException){
                        Toast.makeText(this@listDetails, "Formato de plazo erroneo", Toast.LENGTH_SHORT)
                            .show()
                        view.plazoEditText.setText("")
                    }
                }

                shown = !SwitchItemsChecked.isChecked
                // Here we create the item
                var newItem = Item(
                    nameItem = view.itemNameEditText.text.toString(), estado = false,
                    prioridad = prioritario, plazo = output,
                    notasItem = view.descripcionEditText.text.toString(),
                    fechaCreacion = formatted, isShown = shown
                )

                // We add it to the array of items
                itemsOnList.add(newItem)
                adapter.notifyItemInserted(itemsOnList.size -1)

                //Here we add the item to the bdd
                InsertItem(this@listDetails,newItem)

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
                changedName = true
                list.name = view.listNameTextView.text.toString()
                listBeingUsed.name = view.listNameTextView.text.toString()
                nombreListaTextView.text = list.name
                UpdateNameList(this@listDetails)
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
        val myIntent = Intent()
        if(SwitchItemsChecked.isChecked) {
            itemsOnList.forEach {
                var itemModifiedPosition = itemsOnList.indexOf(it)
                it.isShown = !it.isShown
                adapter.notifyItemChanged(itemModifiedPosition)
                map[it]!!.isShown = it.isShown
                UpdateSpecificItemToEnd(this, map[it])
            }
        }
        else {
            list = ListaItem(
                list!!.name,
                itemsOnList
            )
            if(listBeingUsed.isSharedList){
                if (changedName){
                    myIntent.putExtra("changed name", listBeingUsed.name)
                    myIntent.putExtra("id changed name", listId)
                }
                else{
                    myIntent.putExtra("changed name", "no")
                }
                myIntent.putExtra("online", online)
                setResult(159, myIntent)
                finish()
            }
            else {
                myIntent.putExtra("online", online)
                myIntent.putExtra("listaItems", list)
                setResult(Activity.RESULT_OK, myIntent)
                finish()
            }
        }
    }

    // We update the item if it was clicked
    override fun onSpecificItemCLicked(result: Item, check:CheckBox, position: Int) {
        if(funcionando){
            Thread.sleep(900)
        }
        podemosActualizar = false
        lateinit var itemBDDModified: ItemBDD
        if(listBeingUsed.isSharedList) {
            itemBDDModified = map.filterValues { it.position == position + 1 }.values.toMutableList()[0]
        }
        else{
            itemBDDModified = map.filterValues { it.position == position }.values.toMutableList()[0]
        }

        if(result.estado){
            result.estado = check.isChecked
            result.isShown = check.isChecked
        }
        else {
            result.estado = check.isChecked
            result.isShown = !check.isChecked
        }

        itemBDDModified.done = result.estado
        itemBDDModified.isShown = result.isShown
        map[result] = itemBDDModified
        // We save it on the list ot items
        itemsOnList[position] = result
        adapter.notifyItemChanged(position)
        UpdateSpecificItem(this, itemBDDModified)
    }

    // To go to the item details activity
    override fun onEyeItemCLicked(result: Item, position: Int) {
        if(funcionando){
            Thread.sleep(900)
        }
        podemosActualizar = false
        val intent = Intent(this, ItemDetails::class.java)

        itemModificadoPos = position
        var count = 0
        if(listBeingUsed.isSharedList){
            val itemBuscadoBD = map.filterValues { it.position == position + 1 }.values.toMutableList()
            intent.putExtra("item from db", itemBuscadoBD[0] as Serializable)
        }
        else{
            val itemBuscadoBD = map.filterValues { it.position == position }.values.toMutableList()
            intent.putExtra("item from db", itemBuscadoBD[0] as Serializable)
        }
        /*map.keys.forEach{
            if (count == itemModificadoPos){
                intent.putExtra("item from db", map[it])
            }
            count += 1
        }*/
        intent.putExtra("list", listBeingUsed)
        intent.putExtra("online", online)
        intent.putExtra("item to watch", result)
        intent.putExtra("Item position", itemModificadoPos)

        intent.putExtra("all items", itemsOnList as Serializable)
        startActivityForResult(intent, 4)
    }

    // If the state is changed we need to pass the important data to don't lose it
    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putBoolean("online1", online)
        savedInstanceState.putBoolean("IS_SHOWING_DIALOG_EDIT", isShowingDialogEdit)
        savedInstanceState.putBoolean("IS_SHOWING_DIALOG_ADD", isShowingDialogAdd)
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
        val myIntent = Intent()
        if(SwitchItemsChecked.isChecked) {
            itemsOnList.forEach {
                var itemModifiedPosition = itemsOnList.indexOf(it)
                it.isShown = !it.isShown
                adapter.notifyItemChanged(itemModifiedPosition)
                map[it]!!.isShown = it.isShown
                UpdateSpecificItemToEnd(this, map[it])
            }
        }
        else {
            list = ListaItem(
                list!!.name,
                itemsOnList
            )
            if(listBeingUsed.isSharedList){
                if (changedName){
                    myIntent.putExtra("changed name", listBeingUsed.name)
                    myIntent.putExtra("id changed name", listId)
                }
                else{
                    myIntent.putExtra("changed name", "no")
                }
                myIntent.putExtra("online", online)
                setResult(159, myIntent)
                finish()
            }
            else{
                myIntent.putExtra("online", online)
                myIntent.putExtra("listaItems", list)
                setResult(Activity.RESULT_OK, myIntent)
                finish()
            }

        }
        super.onBackPressed()
    }

    fun shareListPopUp(){
        if(listBeingUsed.isSharedList){
            Toast.makeText(this, "Ya es una lista compartida!", Toast.LENGTH_SHORT).show()
        }
        else{
            var builder: AlertDialog.Builder = AlertDialog.Builder(this)
            var inflater: LayoutInflater = layoutInflater
            var view: View = inflater.inflate(R.layout.share_popup,null)
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
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    postSharedList(this@listDetails, view.emailSharedEditText.text.toString())
                    dialog?.dismiss()
                    isShowingDialogAdd = false
                }
            })
            dialogAdd = builder.create()
            dialogAdd!!.show()
            isShowingDialogAdd = true
        }
    }

    private fun loop() {
        CoroutineScope(Dispatchers.IO).launch {
            delay(5000)
            CoroutineScope(Dispatchers.Main).launch {
                if(podemosActualizar) {
                    funcionando = true
                    getItemsWorkingFromSharedList(this@listDetails)
                    loop()
                }
                else{
                    loop()
                }
            }
        }
    }

    companion object {
        var LOCATION_PERMISSION = 100

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
                    if(listaActivity.listBeingUsed.isSharedList) {
                        listaActivity.list =
                            ListaItem(
                                result.list.name,
                                ArrayList(),
                                true
                            )
                    }
                    else{
                        listaActivity.list =
                            ListaItem(
                                result.list.name,
                                ArrayList(),
                                false
                            )
                    }
                    val x = result.items?.sortedBy { it.position }
                    x?.forEach {
                        val itemAdded =
                            Item(
                                it.name, it.done, it.starred, it.due_date,
                                it.notes, it.created_at, it.isShown
                            )
                        if (listaActivity.SwitchItemsChecked.isChecked){
                            itemAdded.isShown = it.done
                        }
                        else{
                            itemAdded.isShown = !it.done
                        }
                        listaActivity.list.items!!.add(itemAdded)
                        listaActivity.itemsOnList.add(itemAdded)
                        listaActivity.map[itemAdded] = it
                    }
                }
                listaActivity.adapter.setData(listaActivity.itemsOnList)
                // We set the name of the list
                listaActivity.nombreListaTextView.text = listaActivity.list.name
            }
        }


        fun InsertItem(listaActivity: listDetails, params: Item?){
            var listaIt = params!!
            var itemForBDD = ItemBDD(
                listaActivity.itemsCounter,
                listaActivity.listId,
                params.nameItem,
                params.estado,
                params.prioridad,
                params.plazo,
                params.notasItem,
                params.fechaCreacion,
                listaActivity.itemsOnList.indexOf(params),
                params!!.isShown,
                "",
                true,
                listaActivity.longitud,
                listaActivity.lat
            )
            if(listaActivity.listBeingUsed.isSharedList){
                itemForBDD.position += 1
            }
            if(!isOnline(listaActivity))
            {
                itemForBDD.id += 100
                itemForBDD.isOnline = false
            }

            val request = UserService.buildService(PersonApi::class.java)
            val itemTest =
                ListItems(
                    listOf(itemForBDD)
                )

            val call = request.postItem(itemTest, API_KEY)
            call.enqueue(object : Callback<List<ItemBDD>> {
                override fun onResponse(
                    call: Call<List<ItemBDD>>,
                    response: Response<List<ItemBDD>>
                ) {
                    if (response.isSuccessful) {
                        if (response.body() != null) {
                            if (!listaActivity.listBeingUsed.isSharedList) {
                                itemForBDD.id = response.body()!![0].id
                                itemForBDD.updated_at = response.body()!![0].updated_at
                                InsertItemDB(listaIt, listaActivity).execute(itemForBDD)
                            }
                            else{
                                itemForBDD.id = response.body()!![0].id
                                itemForBDD.updated_at = response.body()!![0].updated_at
                                listaActivity.map[listaIt] = itemForBDD
                            }
                        }
                    }
                }

                @RequiresApi(Build.VERSION_CODES.O)
                override fun onFailure(call: Call<List<ItemBDD>>, t: Throwable) {
                    val current = LocalDateTime.now()
                    val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                    val formatted = current.format(formatter)
                    if (!listaActivity.listBeingUsed.isSharedList) {
                        itemForBDD.updated_at = formatted
                        InsertItemDB(listaIt, listaActivity).execute(itemForBDD)
                        println("NO FUNCIONA ${t.message}")
                    }
                    else{
                        itemForBDD.updated_at = formatted
                        listaActivity.map[listaIt] = itemForBDD
                    }
                }
            })

        }

        class InsertItemDB(private val listaActivity: Item, private val listaAct: listDetails):
            AsyncTask<ItemBDD, Void, Void>(){
            override fun doInBackground(vararg params: ItemBDD?): Void? {
                listaAct.map[listaActivity] = params[0]!!
                listaAct.itemsCounter = listaAct.database.insertItem(params[0]!!) + 1
                listaAct.podemosActualizar = true
                return null
            }
        }

        fun UpdateNameList(listaActivity: listDetails){
            val request = UserService.buildService(PersonApi::class.java)
            val call = request.updateList(listaActivity.listBeingUsed.id.toInt(),listaActivity.listBeingUsed, API_KEY)
            call.enqueue(object : Callback<ListBDD> {
                override fun onResponse(
                    call: Call<ListBDD>,
                    response: Response<ListBDD>
                ) {
                    println(response)
                    if (response.isSuccessful) {
                        if (response.body() != null) {
                            listaActivity.listBeingUsed.isOnline = true
                            listaActivity.listBeingUsed.updated_at = response.body()!!.updated_at
                            UpdateListBD(listaActivity).execute(listaActivity.listBeingUsed)
                        }
                    }
                }
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onFailure(call: Call<ListBDD>, t: Throwable) {
                    val current = LocalDateTime.now()
                    val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                    val formatted = current.format(formatter)
                    listaActivity.listBeingUsed.isOnline = false
                    listaActivity.listBeingUsed.updated_at = formatted
                    UpdateListBD(listaActivity).execute(listaActivity.listBeingUsed)
                    println("NO FUNCIONA ${t.message}")
                }
            })
        }

        class UpdateListBD(private val listaActivity: listDetails):
            AsyncTask<ListBDD, Void, Void>() {
            override fun doInBackground(vararg params: ListBDD?): Void? {
                listaActivity.database.updateList(params[0]!!)
                return null
            }
        }

        fun UpdateSpecificItem(listaActivity: listDetails, params: ItemBDD?){
            val request = UserService.buildService(PersonApi::class.java)
            val call = request.updateItem(params!!.id.toInt(), params, API_KEY)
            call.enqueue(object : Callback<ItemBDD> {
                override fun onResponse(
                    call: Call<ItemBDD>,
                    response: Response<ItemBDD>
                ) {
                    if (response.isSuccessful) {
                        if (response.body() != null) {
                            if(!listaActivity.listBeingUsed.isSharedList) {
                                params.updated_at = response.body()!!.updated_at
                                params.isOnline = true
                                if (!listaActivity.listBeingUsed.isSharedList) {
                                    UpdateItemDb(listaActivity).execute(params)
                                }
                            }
                            else{
                                getItemsFromSharedList(listaActivity,true)
                            }
                        }
                    }
                }
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onFailure(call: Call<ItemBDD>, t: Throwable) {
                    val current = LocalDateTime.now()
                    val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                    val formatted = current.format(formatter)
                    listaActivity.listBeingUsed.updated_at = formatted
                    params.isOnline = false
                    params.updated_at = formatted
                    UpdateItemDb(listaActivity).execute(params)
                    println("NO FUNCIONA ${t.message}")

                }
            })
            UpdateMap(listaActivity).execute()
        }

        fun UpdateSpecificItemToEnd(listaActivity: listDetails, params: ItemBDD?){
            val request = UserService.buildService(PersonApi::class.java)
            val call = request.updateItem(params!!.id.toInt(), params, API_KEY)
            call.enqueue(object : Callback<ItemBDD> {
                override fun onResponse(
                    call: Call<ItemBDD>,
                    response: Response<ItemBDD>
                ) {
                    if (response.isSuccessful) {
                        if (response.body() != null) {
                            params.updated_at = response.body()!!.updated_at
                            params.isOnline = true
                            UpdateItemDbToEnd(listaActivity).execute(params)
                        }
                    }
                }
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onFailure(call: Call<ItemBDD>, t: Throwable) {
                    val current = LocalDateTime.now()
                    val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                    val formatted = current.format(formatter)
                    listaActivity.listBeingUsed.updated_at = formatted
                    params.isOnline = false
                    params.updated_at = formatted
                    UpdateItemDbToEnd(listaActivity).execute(params)
                }
            })
        }

        class UpdateItemDbToEnd(private val listaActivity: listDetails):
            AsyncTask<ItemBDD, Void, Void?>(){
            override fun doInBackground(vararg params: ItemBDD?): Void? {
                listaActivity.database.updateItem(params[0]!!)
                return null
            }

            override fun onPostExecute(result: Void?) {
                listaActivity.list = ListaItem(
                    listaActivity.list.name,
                    listaActivity.itemsOnList
                )
                val myIntent = Intent()
                if(listaActivity.listBeingUsed.isSharedList){
                    myIntent.putExtra("online", listaActivity.online)
                    if (listaActivity.changedName){
                        myIntent.putExtra("changed name", listaActivity.listBeingUsed.name)
                        myIntent.putExtra("id changed name", listaActivity.listId)
                    }
                    else{
                        myIntent.putExtra("changed name", "no")
                    }
                    listaActivity.setResult(159, myIntent)
                    listaActivity.finish()
                }
                else{
                    myIntent.putExtra("online", listaActivity.online)
                    myIntent.putExtra("listaItems", listaActivity.list)
                    listaActivity.setResult(Activity.RESULT_OK, myIntent)
                    listaActivity.finish()
                }
            }
        }

        class UpdateItemDb(private val listaActivity: listDetails):
            AsyncTask<ItemBDD, Void, Void?>(){
            override fun doInBackground(vararg params: ItemBDD?): Void? {
                listaActivity.database.updateItem(params[0]!!)
                return null
            }
            override fun onPostExecute(result: Void?) {
                UpdateMap(listaActivity).execute()
            }
        }

        class UpdateMap(private val listaActivity: listDetails):
            AsyncTask<Void, Void, List<ItemBDD>?>(){
            var x = 0
            override fun doInBackground(vararg params: Void?): List<ItemBDD>? {
                x = listaActivity.itemsOnList.size -1
                val listTest = listaActivity.database.getSpecificList(listaActivity.listId)
                val orderedItems = listTest.items?.sortedBy { it.position }
                return orderedItems
            }

            override fun onPostExecute(result: List<ItemBDD>?) {
                //listaActivity.adapter.notifyItemRangeRemoved(0, x)
                //listaActivity.adapter.notifyDataSetChanged()
                var count = 0
                listaActivity.map = hashMapOf()
                result!!.forEach{
                    val item = Item(it.name,it.done, it.starred, it.due_date, it.notes, it.created_at, false)
                    if (listaActivity.SwitchItemsChecked.isChecked){
                        item.isShown = it.done
                    }
                    it.position = count
                    listaActivity.itemsOnList[count] = item
                    listaActivity.map[item] = it
                    count += 1
                }
                if(listaActivity.listBeingUsed.isSharedList) {
                    getItemsFromSharedList(listaActivity, true)
                }
                else{
                    listaActivity.podemosActualizar = true
                }
                //listaActivity.adapter.notifyDataSetChanged()
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun ModifyPositionItems(listaActivity: listDetails, param0: Item?, param1: Item?){
            var item1 = listaActivity.map[param0!!]
            var item2 = listaActivity.map[param1!!]

            //val pos = item1!!.position
            //item1.position = item2!!.position
            //item2.position = pos

            val request = UserService.buildService(PersonApi::class.java)
            val call = request.updateItem(item1?.id!!.toInt(),item1, API_KEY)
            call.enqueue(object : Callback<ItemBDD> {
                override fun onResponse(
                    call: Call<ItemBDD>,
                    response: Response<ItemBDD>
                ) {
                    println(response)
                    if (response.isSuccessful) {
                        if (response.body() != null) {
                            item1.updated_at = response.body()!!.updated_at
                            item1.isOnline = true
                            listaActivity.map[param0] = item1
                            if(!listaActivity.listBeingUsed.isSharedList) {
                                UpdateItemDb(listaActivity).execute(item1)
                            }
                        }
                    }
                }
                override fun onFailure(call: Call<ItemBDD>, t: Throwable) {
                    val current = LocalDateTime.now()
                    val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                    val formatted = current.format(formatter)

                    item1.isOnline = false
                    item1.updated_at = formatted
                    listaActivity.map[param0] = item1
                    if(!listaActivity.listBeingUsed.isSharedList) {
                        UpdateItemDb(listaActivity).execute(item1)
                    }
                    println("NO FUNCIONA ${t.message}")
                }
            })

            val call2 = request.updateItem(item2?.id!!.toInt(),item2, API_KEY)
            call2.enqueue(object : Callback<ItemBDD> {
                override fun onResponse(
                    call: Call<ItemBDD>,
                    response: Response<ItemBDD>
                ) {
                    println(response)
                    if (response.isSuccessful) {
                        if (response.body() != null) {
                            item2.updated_at = response.body()!!.updated_at
                            item2.isOnline = true
                            listaActivity.map[param1] = item2
                            if(!listaActivity.listBeingUsed.isSharedList) {
                                UpdateItemDb(listaActivity).execute(item2)
                            }
                        }
                    }
                }
                override fun onFailure(call: Call<ItemBDD>, t: Throwable) {
                    val current = LocalDateTime.now()
                    val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                    val formatted = current.format(formatter)

                    item2.isOnline = false
                    item2.updated_at = formatted
                    listaActivity.map[param1] = item2
                    if(!listaActivity.listBeingUsed.isSharedList) {
                        UpdateItemDb(listaActivity).execute(item2)
                    }
                    println("NO FUNCIONA ${t.message}")
                }
            })
        }

        fun getItemsFromSharedList(listaActivity: listDetails, isUpdate: Boolean){
            val request = UserService.buildService(PersonApi::class.java)
            val call = request.getAllItem(listaActivity.listBeingUsed.id.toInt(),API_KEY)
            call.enqueue(object : Callback<List<ItemBDD>> {
                override fun onResponse(
                    call: Call<List<ItemBDD>>,
                    response: Response<List<ItemBDD>>
                ) {
                    println(response)
                    if (response.isSuccessful) {
                        if (response.body() != null) {
                            if(!isUpdate) {
                                setSharedItems(response.body()!!, listaActivity)
                            }
                            else{
                                updateMapShared(listaActivity, response.body()!!)
                            }
                        }
                        else{
                            if(!isUpdate) {
                                listaActivity.nombreListaTextView.text = listaActivity.list.name
                                listaActivity.podemosActualizar = true
                            }
                        }
                    }
                }
                override fun onFailure(call: Call<List<ItemBDD>>, t: Throwable) {
                    if(!isUpdate) {
                        GetTheList(listaActivity).execute(listaActivity.listId)
                    }
                }
            })
        }

        fun setSharedItems(items: List<ItemBDD>, listaActivity: listDetails){
            listaActivity.list =
                ListaItem(
                    listaActivity.listBeingUsed.name,
                    ArrayList(),
                    true
                )
            val x = items.sortedBy { it.position }
            x.forEach {
                if (!listaActivity.ListWithIdsItems.contains(it.id)) {
                    listaActivity.ListWithIdsItems.add(it.id)
                }
                val itemAdded =
                    Item(
                        it.name, it.done, it.starred, it.due_date,
                        it.notes, it.created_at, it.isShown
                    )
                if (listaActivity.SwitchItemsChecked.isChecked){
                    itemAdded.isShown = it.done
                } else{
                    itemAdded.isShown = !it.done
                }
                listaActivity.list.items!!.add(itemAdded)
                listaActivity.itemsOnList.add(itemAdded)
                it.isOnline = true
                it.position = listaActivity.itemsOnList.size
                PostToBd(listaActivity).execute(it)
                listaActivity.map[itemAdded] = it
            }
            listaActivity.adapter.setData(listaActivity.itemsOnList)
            // We set the name of the list
            listaActivity.nombreListaTextView.text = listaActivity.listBeingUsed.name
            listaActivity.podemosActualizar = true
        }

        class PostToBd(private val listaActivity: listDetails): AsyncTask<ItemBDD,Void,Void>(){
            override fun doInBackground(vararg params: ItemBDD?): Void? {
                listaActivity.database.insertItem(params[0]!!)
                return null
            }
        }

        fun postSharedList(listaActivity: listDetails, email: String?){
            if(email != null) {
                val listToBeShared = SharedList(1,listaActivity.listBeingUsed.id.toInt(),email)
                val request = UserService.buildService(PersonApi::class.java)
                val call = request.postSharedList(listToBeShared, API_KEY)
                call.enqueue(object : Callback<SharedList> {
                    override fun onResponse(
                        call: Call<SharedList>,
                        response: Response<SharedList>
                    ) {
                        if (response.isSuccessful) {
                            Toast.makeText(listaActivity,"Se ha compartido la lista!", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<SharedList>, t: Throwable) {

                    }
                })
            }
        }

        fun updateMapShared(listaActivity: listDetails, items: List<ItemBDD>){
            listaActivity.map = hashMapOf()
            val x = items.sortedBy { it.position }
//            var posBusc = x[0].position
            var posBusc = 0
            x.forEach { itItem ->
                itItem.isOnline = true
                val itemAdded =
                    Item(
                        itItem.name, itItem.done, itItem.starred, itItem.due_date,
                        itItem.notes, itItem.created_at, itItem.isShown
                    )
                if (listaActivity.SwitchItemsChecked.isChecked){
                    itemAdded.isShown = itItem.done
                } else{
                    itemAdded.isShown = !itItem.done
                }
                if (itItem.position > posBusc + 1){
                    itItem.position = posBusc + 1
                    UpdateSpecificModItem(listaActivity, itItem)
                }
                posBusc += 1
                listaActivity.itemsOnList[itItem.position-1] = itemAdded
                listaActivity.adapter.notifyItemChanged(itItem.position-1)
                PostToBd(listaActivity).execute(itItem)
                listaActivity.map[itemAdded] = itItem
            }
            listaActivity.podemosActualizar = true
        }

        fun UpdateSpecificModItem(listaActivity: listDetails, params: ItemBDD?){
            val request = UserService.buildService(PersonApi::class.java)
            val call = request.updateItem(params!!.id.toInt(), params, API_KEY)
            call.enqueue(object : Callback<ItemBDD> {
                override fun onResponse(
                    call: Call<ItemBDD>,
                    response: Response<ItemBDD>
                ) {
                    if (response.isSuccessful) {
                        if (response.body() != null) {

                        }
                    }
                }

                override fun onFailure(call: Call<ItemBDD>, t: Throwable) {
                }
            })
        }

        fun getItemsWorkingFromSharedList(listaActivity: listDetails){
            val request = UserService.buildService(PersonApi::class.java)
            val call = request.getAllItem(listaActivity.listBeingUsed.id.toInt(),API_KEY)
            call.enqueue(object : Callback<List<ItemBDD>> {
                override fun onResponse(
                    call: Call<List<ItemBDD>>,
                    response: Response<List<ItemBDD>>
                ) {
                    println(response)
                    if (response.isSuccessful) {
                        if (response.body() != null) {
                            addSharedItems(listaActivity, response.body()!!)
                        }
                        else{

                        }
                    }
                }
                override fun onFailure(call: Call<List<ItemBDD>>, t: Throwable) {
                    listaActivity.funcionando = false
                }
            })
        }

        fun addSharedItems(listaActivity: listDetails, items: List<ItemBDD>){
            val x = items.sortedBy { it.position }
            x.forEach {
                it.isOnline = true
                if (!listaActivity.ListWithIdsItems.contains(it.id)) {
                    listaActivity.ListWithIdsItems.add(it.id)
                    val itemAdded =
                        Item(
                            it.name, it.done, it.starred, it.due_date,
                            it.notes, it.created_at, it.isShown
                        )
                    if (listaActivity.SwitchItemsChecked.isChecked){
                        itemAdded.isShown = it.done
                    } else{
                        itemAdded.isShown = !it.done
                    }
                    listaActivity.list.items!!.add(itemAdded)
                    listaActivity.itemsOnList.add(itemAdded)
                    listaActivity.adapter.notifyItemInserted(listaActivity.itemsOnList.size-1)
                    it.position = listaActivity.itemsOnList.size
                    listaActivity.map[itemAdded] = it
                }
                else{
                    val o = listaActivity.map.filterValues { itItem -> itItem.id == it.id }.values.toMutableList()[0]
                    val o2 = listaActivity.map.filterValues { itItem -> itItem.id == it.id }.keys.toMutableList()[0]
                    if(it.updated_at > o.updated_at){
                        val shown: Boolean = if(listaActivity.SwitchItemsChecked.isChecked){
                            it.done
                        } else{
                            !it.done
                        }
                        val itemShared = Item(it.name,it.done, it.starred,it.due_date,it.notes,it.created_at, shown)
                        it.position = o.position
                        listaActivity.list.items!![o.position - 1] = itemShared
                        listaActivity.itemsOnList[o.position - 1] = itemShared
                        listaActivity.adapter.notifyItemChanged(o.position - 1)
                        listaActivity.map.remove(o2)
                        listaActivity.map[itemShared] = it
                    }
                }
            }
            listaActivity.funcionando = false
        }
    }
}