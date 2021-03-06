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
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.entrega1proyecto.configuration.API_KEY
import com.example.entrega1proyecto.model.*
import com.example.entrega1proyecto.model.adapters.*
import com.example.entrega1proyecto.networking.PersonApi
import com.example.entrega1proyecto.networking.UserService
import com.example.entrega1proyecto.networking.isOnline
import kotlinx.android.synthetic.main.activity_lista.*
import kotlinx.android.synthetic.main.popup.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.Serializable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import kotlin.collections.ArrayList


class ListaActivity : AppCompatActivity(),
    OnItemClickListener,
    OnTrashClickListener {

    var listaList: ArrayList<ListaItem> = ArrayList()
    var modified: ListaItem =
        ListaItem("")
    var validador: Boolean = false
    var user: User? = null
    var isShowingDialog = false
    var dialog: Dialog? = null
    // For the DB
    lateinit var database: ListDao
    var testListaList: ArrayList<ListWithItems> = ArrayList()
    lateinit var adapter: AdaptadorCustom
    var listsCounter: Long = 1
    val map = hashMapOf<ListaItem, ListBDD>()
    var online = false
    var onlinep = false
    var onlinef = false
    // Shared Lists
    var ListWithIds = ArrayList<Long>()
    var podemosActualizar = true
    var funcionando = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista)
        // Here we create the db
        database = Room.databaseBuilder(this, Database::class.java, "ListsBDD").build().ListDao()

        // The recycler view for the Activity that contains the lists
        adapter = AdaptadorCustom(
            this,
            this
        )
        recycler_view.adapter = adapter
        recycler_view.layoutManager = LinearLayoutManager(this)

        // We get all the lists from the db
        GetAllLists(this).execute()

        if (savedInstanceState?.getBoolean("validador") != null){
            validador = savedInstanceState?.getBoolean("validador")!!
        }

        if(savedInstanceState!=null){
            isShowingDialog = savedInstanceState.getBoolean("IS_SHOWING_DIALOG", false)
            if(isShowingDialog){
                plusButton(View(this))
            }
            onlinep = savedInstanceState?.getBoolean("online1")!!
        }

        onlinef = intent.getBooleanExtra("online", false)
        online = onlinep || isOnline(this) || onlinef

        user = intent.getSerializableExtra("user details start") as User
        nombreUsuarioTextView.text = user!!.first_name

        // This is to keep the Lists if we got back to the Log In activity
        if (!validador) {
            if (intent?.getSerializableExtra("lista") != null) {
                validador = true
                // This is to mantain the list if we change the orientation of the phone
            }
        }

        //This is for the Drag and Drop ----------------------------------------------------------------------------

        val touchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0){
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
                Collections.swap(listaList, sourcePosition, targetPosition)
                ModifyPos(this@ListaActivity).execute(listaList[sourcePosition], listaList[targetPosition])

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

    // Function to go to the activity which contains the items inside a list
    override fun onItemCLicked(result: ListaItem){
        if(funcionando){
            Thread.sleep(900)
        }
        podemosActualizar = false
        // We keep the List who was clicked
        modified = result
        val c = ArrayList<Int>()
        listaList.forEach {
            if (it.isShared!!){
                c.add(listaList.indexOf(it))
            }
        }
        c.forEach {
            listaList.removeAt(it)
            adapter.notifyItemRemoved(it)
        }
        // Go to the items in a list activity
        val intent = Intent(this, listDetails::class.java)
        intent.putExtra(LISTS, map[result])
        intent.putExtra("online", online)
        startActivityForResult(intent, 1)
    }

    // Ask for the result from the items in a list activity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            if (resultCode == Activity.RESULT_OK){
                ListWithIds = ArrayList()
                listaList = ArrayList()
                adapter.notifyDataSetChanged()
                onlinef = data.getBooleanExtra("online", false)
                GetAllLists(this).execute()
            }
            else if (resultCode == 2){
                user = data.getSerializableExtra("user details updated") as User
                val endIntent = Intent()
                // We give the result to the Log in activity to maintain the information
                onlinef = data.getBooleanExtra("online", false)

                endIntent.putExtra("online", onlinef)
                endIntent.putExtra("lista de listas",listaList as Serializable)
                endIntent.putExtra("user details finish",user as Serializable)
                setResult(Activity.RESULT_OK, endIntent)
                finish()
            }
            else if (resultCode == 3){
                onlinef = data.getBooleanExtra("online", false)

                user = data.getSerializableExtra("user details update") as User
                nombreUsuarioTextView.text = user!!.first_name
                podemosActualizar = true
            }
            else if (resultCode == 159){
                onlinef = data.getBooleanExtra("online", false)
                val name = data.getSerializableExtra("changed name") as String
                ListWithIds = ArrayList()
                if (name != "no"){
                    val idChangedName = data.getSerializableExtra("id changed name") as Long
                    val listRemovedByName = map.filterValues { it.id == idChangedName }.keys.toMutableList()[0]
                    map.remove(listRemovedByName)
                }
                podemosActualizar = true
                loop(true)
            }
        }
    }

    // Function when we want to delete a list
    override fun onTrashCLicked(result: ListaItem) {
        if(funcionando){
            Thread.sleep(900)
        }
        podemosActualizar = false

        var pos = listaList.indexOf(result)
        if(result.isShared!!) {
            ListWithIds.remove(map[result]!!.id)
        }
        Trash(this).execute(result)
        listaList.remove(result)
        adapter.notifyItemRemoved(pos)
        println(adapter)
    }

    // Function when was clicked the username to log out
    fun logOutPopUp(view: View){
        if(funcionando){
            Thread.sleep(900)
        }
        podemosActualizar = false
        val intent = Intent(this, UserDetails::class.java)
        // We give the result to the Log in activity to maintain the information
        intent.putExtra("online", online)
        intent.putExtra("lista de listas",listaList)
        intent.putExtra("user details", user as Serializable)
        startActivityForResult(intent, 3)
    }

    // Function to create new lists
    fun plusButton(view: View){
        // We show a Dialog asking what's the name of the list we want to create
        var builder: AlertDialog.Builder = AlertDialog.Builder(this)
        var inflater: LayoutInflater = layoutInflater
        var view: View = inflater.inflate(R.layout.popup,null)
        builder.setCancelable(false)
        builder.setView(view)

        // If they don't want to create a new List we resume what we were showing
        builder.setNegativeButton("Cancelar", object: DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                dialog?.dismiss()
                isShowingDialog = false
            }
        })

        // Here we create the new list with null items inside it
        builder.setPositiveButton("Confirmar",object:  DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                if(funcionando){
                    Thread.sleep(900)
                }
                podemosActualizar = false
                val listToBeAdded =
                    ListaItem(
                        view.listNameTextView.text.toString(),
                        ArrayList(),
                        false
                    )
                listaList.add(listToBeAdded)
                adapter.notifyItemInserted(listaList.size - 1)
                InsertList(this@ListaActivity).execute(listToBeAdded)
                dialog?.dismiss()
                isShowingDialog = false
            }
        })
        dialog = builder.create()
        dialog!!.show()
        isShowingDialog = true
    }

    // Function to maintain the data when the activity is changed of state
    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        // We give the username
        savedInstanceState.putSerializable("person", user as Serializable)
        // We give the changed item if the screen is rotated before being saved in the array of lists
        savedInstanceState.putSerializable("ItemModificado",modified)
        // We give the array of lists
        //savedInstanceState.putSerializable("lista listas",listaList)

        savedInstanceState.putBoolean("online1", onlinef)

        //savedInstanceState.putSerializable("ids list", ListWithIds)

        savedInstanceState.putBoolean("IS_SHOWING_DIALOG", isShowingDialog)

        savedInstanceState.putBoolean("validador", validador)
    }

    // Function to obtain what was given before changing the state of the activity
    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        // Obtain the username
        user = savedInstanceState?.getSerializable("person") as User
        nombreUsuarioTextView.text = user!!.first_name

        //ListWithIds = savedInstanceState.getSerializable("ids list") as ArrayList<Long>

        // Obtain the modified items on the list
        modified = savedInstanceState.getSerializable("ItemModificado") as ListaItem

        adapter.notifyDataSetChanged()
    }

    // To mantain the dialogs states when the app state is changed
    override fun onPause() {
        if(dialog!=null && dialog!!.isShowing) {
            dialog!!.dismiss();
        }
        super.onPause()
    }

    // To go back to the log in activity
    override fun onBackPressed() {
        val endIntent = Intent()
        endIntent.putExtra("online", onlinef)
        endIntent.putExtra("lista de listas",listaList as Serializable)
        endIntent.putExtra("user details finish",user as Serializable)
        setResult(Activity.RESULT_OK, endIntent)
        finish()
        super.onBackPressed()
    }

    private fun loop(saltar: Boolean) {
        CoroutineScope(IO).launch {
            if(!saltar) {
                delay(5000)
            }
            CoroutineScope(Main).launch {
                if(podemosActualizar) {
                    funcionando = true
                    getAllSharedLists(this@ListaActivity)
                    loop(false)
                }
                else {
                    loop(false)
                }
            }
        }
    }

    companion object {
        var LISTS = "LISTS"
        var IT = this

        // Class to get all the lists from the database
        class GetAllLists(private val listaActivity: ListaActivity) :
            AsyncTask<Void, Void, ArrayList<ListWithItems>>() {
            var list: ListaItem =
                ListaItem("", null, false)
            override fun doInBackground(vararg params: Void?): ArrayList<ListWithItems> {
                // Here we get the elements from the database
                listaActivity.testListaList =
                    ArrayList(listaActivity.database.getListWithItems())
                if (listaActivity.listsCounter == 1.toLong() && listaActivity.testListaList.size != 0){
                    listaActivity.listsCounter = listaActivity.testListaList[listaActivity.testListaList.size -1].list.id + 1
                }

                return listaActivity.testListaList
            }

            override fun onPostExecute(result: ArrayList<ListWithItems>?) {
                if (listaActivity.listaList.size != 0){
                    listaActivity.listaList = ArrayList()
                }
                //After getting the elements from the db
                if (listaActivity.testListaList.size > 0 && listaActivity.listsCounter == 1.toLong()) {
                    listaActivity.listsCounter = listaActivity.testListaList[listaActivity.testListaList.lastIndex].list.id + 1
                }

                // Here we add it to the lista list
                listaActivity.testListaList.forEach{
                    if (!it.list.isSharedList){
                        list =
                            ListaItem(it.list.name, null, false)
                        if (list.items == null){
                            list.items = ArrayList()
                        }
                        it.items?.forEach {x->
                            list.items!!.add(
                                Item(
                                    x.name, x.done, x.starred, x.due_date,
                                    x.notes, x.created_at, x.isShown
                                )
                            )
                        }
                        listaActivity.map[list] = it.list
                        listaActivity.listaList.add(list)
                    }
                }
                listaActivity.adapter.setData(listaActivity.listaList)
                listaActivity.podemosActualizar = true
                listaActivity.loop(true)
            }
        }

        // Class to insert items into the db
        class InsertList(private val listaActivity: ListaActivity) : AsyncTask<ListaItem, Void, Void>(){
            lateinit var listaIt: ListaItem
            override fun doInBackground(vararg params: ListaItem?): Void? {
                // We get the new id to add a new list when we add a list
                listaIt = params[0]!!
                lateinit var listToBeAdded: ListBDD
                if (isOnline(listaActivity)) {
                    listToBeAdded = ListBDD(
                        listaActivity.listsCounter,
                        params[0]!!.name,
                        listaActivity.listaList.indexOf(params[0]!!),
                        "0",
                        true,
                        isSharedList = false
                    )
                }
                else{
                    listToBeAdded = ListBDD(
                        listaActivity.listsCounter+100,
                        params[0]!!.name,
                        listaActivity.listaList.indexOf(params[0]!!),
                        "0",
                        false,
                        isSharedList = false
                    )
                }

                val request = UserService.buildService(PersonApi::class.java)
                val call = request.postList(listToBeAdded, API_KEY)
                call.enqueue(object : Callback<ListBDD> {
                    override fun onResponse(
                        call: Call<ListBDD>,
                        response: Response<ListBDD>
                    ) {
                        print(response)
                        if (response.isSuccessful) {
                            if (response.body() != null) {
                                listToBeAdded.id = response.body()!!.id
                                listToBeAdded.updated_at = response.body()!!.updated_at
                                listToBeAdded.isOnline = true
                                InsertDB(this@InsertList, listaActivity).execute(listToBeAdded)
                            }
                        }
                    }
                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onFailure(call: Call<ListBDD>, t: Throwable) {
                        val current = LocalDateTime.now()
                        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                        val formatted = current.format(formatter)

                        listToBeAdded.isOnline = false
                        listToBeAdded.updated_at = formatted
                        InsertDB(this@InsertList, listaActivity).execute(listToBeAdded)
                    }
                })
                listaActivity.podemosActualizar = true
                return null
            }
        }

        class InsertDB(private val listaActivity: InsertList, private val listaAct: ListaActivity):AsyncTask<ListBDD, Void, Void>(){
            override fun doInBackground(vararg params: ListBDD?): Void? {
                listaAct.map[listaActivity.listaIt] = params[0]!!
                listaAct.listsCounter = listaAct.database.insertList(params[0]!!) + 1
                return null
            }

        }

        // Class when a list is removed
        class Trash(private val listaActivity: ListaActivity) : AsyncTask<ListaItem, Void, Void>(){
            override fun doInBackground(vararg params: ListaItem?): Void? {
                val listaABorrar = listaActivity.map[params[0]!!]
                listaActivity.database.deleteList(listaABorrar!!)
                listaActivity.database.deleteListItems(listaABorrar.id)

                val request = UserService.buildService(PersonApi::class.java)
                val call = request.deleteList(listaABorrar.id.toInt(), API_KEY)
                call.enqueue(object : Callback<ListBDD> {
                    override fun onResponse(
                        call: Call<ListBDD>,
                        response: Response<ListBDD>
                    ) {
                        if (response.isSuccessful) {

                        }
                        else{
                            InsertDeletedToDB(listaActivity).execute(ListBddErased(listaABorrar.id))
                        }
                    }

                    override fun onFailure(call: Call<ListBDD>, t: Throwable) {
                        InsertDeletedToDB(listaActivity).execute(ListBddErased(listaABorrar.id))
                        println("NO FUNCIONA ${t.message}")
                    }
                })
                listaActivity.podemosActualizar = true
                return null
            }
        }

        class InsertDeletedToDB(private val listaActivity: ListaActivity): AsyncTask<ListBddErased,Void,Void?>(){
            override fun doInBackground(vararg params: ListBddErased?): Void? {
                listaActivity.database.eraseList(params[0]!!)
                return null
            }
        }

        class UpdateListDb(private val listaActivity: ListaActivity):
            AsyncTask<ListBDD, Void, Void?>(){
            override fun doInBackground(vararg params: ListBDD?): Void? {
                listaActivity.database.updateList(params[0]!!)
                return null
            }
        }

        class ModifyPos(private val listaActivity: ListaActivity) : AsyncTask<ListaItem, Void, Void>(){
            override fun doInBackground(vararg params: ListaItem?): Void? {
                var item1 = listaActivity.map[params[0]!!]
                var item2 = listaActivity.map[params[1]!!]
                var pos = 0
                // Si el item1 es lista compartida
                if(item1!!.isSharedList && !item2!!.isSharedList) {
                    pos = item1.position - 1
                    item1.position = item2.position + 1
                    item2.position = pos
                }
                // Si el item2 es lista compartida
                else if(item2!!.isSharedList && !item1.isSharedList){
                    pos = item1.position + 1
                    item1.position = item2.position - 1
                    item2.position = pos
                }
                // Si ambos son o ninguno es
                else{
                    pos = item1.position
                    item1.position = item2.position
                    item2.position = pos
                }


                val request = UserService.buildService(PersonApi::class.java)
                val call = request.updateList(item1.id.toInt(),item1, API_KEY)
                call.enqueue(object : Callback<ListBDD> {
                    override fun onResponse(
                        call: Call<ListBDD>,
                        response: Response<ListBDD>
                    ) {
                        println(response)
                        if (response.isSuccessful) {
                            if (response.body() != null) {
                                item1.updated_at = response.body()!!.updated_at
                                item1.isOnline = true
                                listaActivity.map[params[0]!!] = item1
                                UpdateListDb(listaActivity).execute(item1)
                            }
                        }
                    }
                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onFailure(call: Call<ListBDD>, t: Throwable) {
                        val current = LocalDateTime.now()
                        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                        val formatted = current.format(formatter)

                        item1.isOnline = false
                        item1.updated_at = formatted
                        listaActivity.map[params[0]!!] = item1
                        UpdateListDb(listaActivity).execute(item1)
                        println("NO FUNCIONA ${t.message}")
                    }
                })

                val call2 = request.updateList(item2.id.toInt(),item2, API_KEY)
                call2.enqueue(object : Callback<ListBDD> {
                    override fun onResponse(
                        call: Call<ListBDD>,
                        response: Response<ListBDD>
                    ) {
                        println(response)
                        if (response.isSuccessful) {
                            if (response.body() != null) {
                                item2.updated_at = response.body()!!.updated_at
                                item2.isOnline = true
                                listaActivity.map[params[1]!!] = item2
                                UpdateListDb(listaActivity).execute(item2)
                            }
                        }
                    }
                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onFailure(call: Call<ListBDD>, t: Throwable) {
                        val current = LocalDateTime.now()
                        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                        val formatted = current.format(formatter)

                        item2.isOnline = false
                        item2.updated_at = formatted
                        listaActivity.map[params[1]!!] = item2
                        UpdateListDb(listaActivity).execute(item2)
                        println("NO FUNCIONA ${t.message}")
                    }
                })
                listaActivity.podemosActualizar = true
                return null
            }

        }

        fun getAllSharedLists(listaActivity: ListaActivity){
            val request = UserService.buildService(PersonApi::class.java)
            val call = request.getSharedLists(API_KEY)
            call.enqueue(object : Callback<List<SharedListBDD>> {
                override fun onResponse(
                    call: Call<List<SharedListBDD>>,
                    response: Response<List<SharedListBDD>>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.forEach {
                            if (!listaActivity.ListWithIds.contains(it.list_id)){
                                listaActivity.ListWithIds.add(it.list_id)
                                GetRealSharedList(listaActivity, it)
                            }
                            else{
                                listaActivity.funcionando = false
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<List<SharedListBDD>>, t: Throwable) {
                    GetSharedListsOffline(listaActivity).execute()
                }
            })
        }

        class GetSharedListsOffline(private val listaActivity: ListaActivity): AsyncTask<Void,Void,Void>(){
            override fun doInBackground(vararg params: Void?): Void? {
                val allLists = listaActivity.database.getAllLists()
                allLists.forEach {
                    if (!listaActivity.ListWithIds.contains(it.id) && it.isSharedList){
                        listaActivity.ListWithIds.add(it.id)
                        it.isOnline = false
                        InsertInBDD(listaActivity, false).execute(it)
                    }
                }
                listaActivity.funcionando = false
                return null
            }
        }

        fun GetRealSharedList(listaActivity: ListaActivity, params: SharedListBDD){
            val request = UserService.buildService(PersonApi::class.java)
            val call = request.getList(params.list_id.toInt(),API_KEY)
            call.enqueue(object : Callback<ListBDD> {
                override fun onResponse(
                    call: Call<ListBDD>,
                    response: Response<ListBDD>
                ) {
                    if (response.isSuccessful) {
                        if(response.body() != null){
                            val x = response.body()
                            x!!.isOnline = true
                            if(x.position > listaActivity.listaList.size + 1) {
                                x.position = listaActivity.listaList.size + 1
                            }
                            if (x.position == 0){
                                x.position = 1
                            }
                            x.isSharedList = true
                            InsertInBDD(listaActivity, true).execute(x)
                        }
                    }
                }

                override fun onFailure(call: Call<ListBDD>, t: Throwable) {
                }
            })

        }

        class InsertInBDD(private val listaActivity: ListaActivity, private val isToF: Boolean) : AsyncTask<ListBDD, Void, ListaItem>(){
            var pos = listaActivity.listaList.size
            override fun doInBackground(vararg params: ListBDD?): ListaItem? {
                val listItems = ListaItem(params[0]!!.name, null, true)
                if(params[0]!!.position - 1 != listaActivity.listaList.size){
                    listaActivity.listaList.add(params[0]!!.position - 1,listItems)
                    pos = params[0]!!.position - 1
                    listaActivity.map.keys.forEach {
                        if(it.isShared!!){
                            listaActivity.map[it]!!.position =  listaActivity.listaList.indexOf(it) + 1
                            listaActivity.database.updateList(listaActivity.map[it]!!)
                            updatePosInApi(listaActivity, listaActivity.map[it]!!)
                        }
                        else{
                            listaActivity.map[it]!!.position =  listaActivity.listaList.indexOf(it)
                            listaActivity.database.updateList(listaActivity.map[it]!!)
                            updatePosInApi(listaActivity, listaActivity.map[it]!!)
                        }
                    }
                }
                else{
                    listaActivity.listaList.add(listItems)
                }
                listaActivity.database.insertList(params[0]!!)
                listaActivity.map[listItems] = params[0]!!

                if(isToF){
                    updatePosInApi(listaActivity, params[0]!!)
                }
                return listItems
            }

            override fun onPostExecute(result: ListaItem?) {

                listaActivity.adapter.notifyItemInserted(pos)
                if(!isToF){
                    listaActivity.funcionando = false
                }
            }
        }

        fun updatePosInApi(listaActivity: ListaActivity,itemUpdt: ListBDD){
            val request = UserService.buildService(PersonApi::class.java)
            val call = request.updateList(itemUpdt.id.toInt(),itemUpdt, API_KEY)
            call.enqueue(object : Callback<ListBDD> {
                override fun onResponse(
                    call: Call<ListBDD>,
                    response: Response<ListBDD>
                ) {
                    println(response)
                    if (response.isSuccessful) {
                        if (response.body() != null) {
                        }
                    }
                }
                override fun onFailure(call: Call<ListBDD>, t: Throwable) {
                }
            })
            listaActivity.funcionando = false
        }
    }
}