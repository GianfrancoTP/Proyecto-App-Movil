package com.example.entrega1proyecto

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.room.Room
import com.example.entrega1proyecto.configuration.API_KEY
import com.example.entrega1proyecto.model.Database
import com.example.entrega1proyecto.model.ItemBDD
import com.example.entrega1proyecto.model.ListBDD
import com.example.entrega1proyecto.model.ListDao
import com.example.entrega1proyecto.networking.PersonApi
import com.example.entrega1proyecto.networking.UserService
import kotlinx.android.synthetic.main.activity_item_details.*
import kotlinx.android.synthetic.main.popup.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.Serializable
import java.lang.Exception
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class ItemDetails : AppCompatActivity() {

    var item: Item? = null
    var pos = -1
    var isShowingDialog = false
    var dialog: Dialog? = null
    // Db
    lateinit var database: ListDao
    lateinit var itemDb: ItemBDD
    lateinit var copyOfItem: Item
    lateinit var allItems: ArrayList<Item>
    var online = false
    var onlinep = false
    var onlinef = false
    lateinit var listBeingUsed: ListBDD
    lateinit var idListaABorrar:ListBDD

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_details)

        database = Room.databaseBuilder(this, Database::class.java, "ListsBDD").build().ListDao()

        try {
            item = savedInstanceState?.getSerializable("Item") as Item
            pos = savedInstanceState?.getInt("Item Mod Position")
            itemDb = savedInstanceState?.getSerializable("item from db") as ItemBDD
        }
        catch(e:Exception){
            item = intent.getSerializableExtra("item to watch")!! as Item
            copyOfItem = intent.getSerializableExtra("item recorded")!! as Item
            pos = intent.getIntExtra("Item position", -1)
            allItems = intent.getSerializableExtra("all items") as ArrayList<Item>
            itemDb = intent.getSerializableExtra("item from db") as ItemBDD
            listBeingUsed = intent.getSerializableExtra("list") as ListBDD

        }

        CheckIsInAPI(this).execute()

        if(savedInstanceState!=null){
            onlinep = savedInstanceState.getBoolean("onlinef", false)!!
            isShowingDialog = savedInstanceState.getBoolean("IS_SHOWING_DIALOG", false)
            if(isShowingDialog){
                editItemName(View(this))
            }
        }

        onlinef = intent.getBooleanExtra("online",false)
        online = onlinep || isOnline(this) || onlinef

        if(isOnline(this) && !onlinep && !onlinef){
            online = true
            //LogFragment.GetUserFromApi(LogFragment()).execute()
            //GetListsFromApi(applicationContext, null, this).execute()
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
        updateItemToEndAct()
    }


    // To delete the specific item
    fun deleteItem(view: View){
        val myIntent: Intent = Intent()
        EraseItem(this).execute(itemDb)
        myIntent.putExtra("item updated","NONE")
        myIntent.putExtra("copy item", copyOfItem)
        setResult(5, myIntent)
        finish()
    }

    // When we end modifying the item
    fun updateItem(){
        item!!.nameItem =  nombreItemTextView.text.toString()
        itemDb.name = nombreItemTextView.text.toString()

        item!!.plazo = fechaPlazoTextView.text.toString()
        itemDb.due_date = fechaPlazoTextView.text.toString()

        item!!.prioridad = NotPriorityImageView.visibility != View.VISIBLE
        itemDb.starred = NotPriorityImageView.visibility != View.VISIBLE

        item!!.notasItem = notasItemEditText.text.toString()
        itemDb.notes = notasItemEditText.text.toString()

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
        itemDb.done = item!!.estado
        itemDb.isShown = item!!.isShown
        UpdateItem(this).execute()
    }

    fun updateItemToEndAct(){
        item!!.nameItem =  nombreItemTextView.text.toString()
        itemDb.name = nombreItemTextView.text.toString()

        item!!.plazo = fechaPlazoTextView.text.toString()
        itemDb.due_date = fechaPlazoTextView.text.toString()

        item!!.prioridad = NotPriorityImageView.visibility != View.VISIBLE
        itemDb.starred = NotPriorityImageView.visibility != View.VISIBLE

        item!!.notasItem = notasItemEditText.text.toString()
        itemDb.notes = notasItemEditText.text.toString()

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
        itemDb.done = item!!.estado
        itemDb.isShown = item!!.isShown
        UpdateItemToEnd(this).execute()
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
                item?.nameItem = view.listNameTextView.text.toString()
                itemDb.name = view.listNameTextView.text.toString()
                UpdateItem(this@ItemDetails).execute()
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
        savedInstanceState.putBoolean("onlinef", online)
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
        updateItemToEndAct()
        //super.onBackPressed()
    }

    companion object{
        class UpdateItem(private val listaActivity: ItemDetails):
            AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg params: Void?): Void? {
                val request = UserService.buildService(PersonApi::class.java)
                val call = request.updateItem(listaActivity.itemDb.id.toInt(), listaActivity.itemDb, API_KEY)
                call.enqueue(object : Callback<ItemBDD> {
                    override fun onResponse(
                        call: Call<ItemBDD>,
                        response: Response<ItemBDD>
                    ) {
                        println(response)
                        if (response.isSuccessful) {
                            if (response.body() != null) {
                                listaActivity.itemDb.updated_at = response.body()!!.updated_at
                                UpdateItemDB(listaActivity).execute()
                                println(response.body())
                            }
                        }
                    }
                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onFailure(call: Call<ItemBDD>, t: Throwable) {
                        val current = LocalDateTime.now()
                        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                        val formatted = current.format(formatter)

                        listaActivity.itemDb.updated_at = formatted
                        UpdateItemDB(listaActivity).execute(listaActivity.itemDb)
                        println("NO FUNCIONA ${t.message}")
                    }
                })

                return null
            }
        }

        class CheckIsInAPI(private val listaActivity: ItemDetails):
            AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg params: Void?): Void? {
                val request = UserService.buildService(PersonApi::class.java)
                val call = request.getItem(listaActivity.itemDb.id.toInt(), API_KEY)
                call.enqueue(object : Callback<ItemBDD> {
                    override fun onResponse(
                        call: Call<ItemBDD>,
                        response: Response<ItemBDD>
                    ) {
                        if (response.isSuccessful) {
                            if (response.body() != null) {
                            }
                        }
                        else{
                            CheckListInAPI(listaActivity).execute()
                        }
                    }
                    override fun onFailure(call: Call<ItemBDD>, t: Throwable) {
                        println("no se encontro")

                    }
                })
                return null
            }
        }

        class CheckListInAPI(private val listaActivity: ItemDetails):
            AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg params: Void?): Void? {
                val request = UserService.buildService(PersonApi::class.java)
                val call = request.getList(listaActivity.listBeingUsed.id.toInt(), API_KEY)
                call.enqueue(object : Callback<ListBDD> {
                    override fun onResponse(
                        call: Call<ListBDD>,
                        response: Response<ListBDD>
                    ) {
                        println("ESTA ES LA RESPUESTA $response")
                        if (response.isSuccessful) {
                            if (response.body() != null) {
                                InsertItemInAPI(listaActivity).execute()
                            }
                        }
                        else{
                            InsertListInAPI(listaActivity).execute(listaActivity.listBeingUsed)
                        }
                    }
                    override fun onFailure(call: Call<ListBDD>, t: Throwable) {
                        println("no se encontro")

                    }
                })
                return null
            }
        }



        // Class to insert items into the db
        class InsertListInAPI(private val listaActivity: ItemDetails) : AsyncTask<ListBDD, Void, Void>(){
            override fun doInBackground(vararg params: ListBDD?): Void? {

                val request = UserService.buildService(PersonApi::class.java)
                val call = request.postList(params[0]!!, API_KEY)
                listaActivity.idListaABorrar = params[0]!!
                var listABorrar = params[0]!!

                call.enqueue(object : Callback<ListBDD> {
                    override fun onResponse(
                        call: Call<ListBDD>,
                        response: Response<ListBDD>
                    ) {
                        print(response)
                        if (response.isSuccessful) {
                            if (response.body() != null) {
                                println("LISTA A BORRAR!!!!!!!     ${listaActivity.idListaABorrar}")
                                EraseListInDB(listaActivity).execute()
                                listABorrar.id = response.body()!!.id
                                listaActivity.itemDb.list_id = response.body()!!.id
                                listABorrar.updated_at = response.body()!!.updated_at
                                InsertListInDB(listaActivity).execute(listABorrar)
                                InsertItemInAPI(listaActivity).execute()
                            }
                        }
                    }
                    override fun onFailure(call: Call<ListBDD>, t: Throwable) {
                    }
                })
                return null
            }
        }

        class EraseListInDB(private val listaActivity: ItemDetails):
            AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg params: Void?): Void? {
                println("borrando lista!!!!!!!! ${listaActivity.idListaABorrar}")
                listaActivity.database.deleteList(listaActivity.idListaABorrar)
                return null
            }
        }

        class InsertListInDB(private val listaActivity: ItemDetails):
            AsyncTask<ListBDD, Void, Void>() {
            override fun doInBackground(vararg params: ListBDD?): Void? {
                listaActivity.database.insertList(params[0]!!)
                return null
            }
        }

        class InsertItemInAPI(private val listaActivity: ItemDetails):
            AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg params: Void?): Void? {
                val request = UserService.buildService(PersonApi::class.java)
                val itemTest = ListItems(listOf(listaActivity.itemDb))
                val itemBorrar = listaActivity.itemDb

                val call = request.postItem(itemTest, API_KEY)
                call.enqueue(object : Callback<List<ItemBDD>> {
                    override fun onResponse(
                        call: Call<List<ItemBDD>>,
                        response: Response<List<ItemBDD>>
                    ) {
                        println("ESTA ES LA RESPONSE DE AÑADIRLO  $response")
                        if (response.isSuccessful) {
                            if (response.body() != null) {
                                EraseItemInDB(listaActivity).execute(itemBorrar)
                                println("ESTAMOS DENTRO DE haber insertado en api       ${listaActivity.itemDb}")
                                listaActivity.itemDb.id = response.body()!![0].id
                                listaActivity.itemDb.updated_at =
                                    response.body()!![0].updated_at
                                InsertItemDB(listaActivity).execute(
                                    listaActivity.itemDb
                                )
                            }
                        }
                    }

                    override fun onFailure(call: Call<List<ItemBDD>>, t: Throwable) {
                    }
                })
                return null
            }
        }

        class EraseItemInDB(private val listaAct: ItemDetails):AsyncTask<ItemBDD, Void, Void>(){
            override fun doInBackground(vararg params: ItemBDD?): Void? {
                println("ELIMINANDO ITEM DE DB     ${params[0]!!}")
                listaAct.database.deleteItem(params[0]!!)
                return null
            }
        }

        class InsertItemDB(private val listaAct: ItemDetails):AsyncTask<ItemBDD, Void, Void>(){
            override fun doInBackground(vararg params: ItemBDD?): Void? {
                println("INSERTANDO ITEM EN DB  ${params[0]!!}")
                listaAct.database.insertItem(params[0]!!)
                return null
            }
        }

        class UpdateItemToEnd(private val listaActivity: ItemDetails):
            AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg params: Void?): Void? {
                val request = UserService.buildService(PersonApi::class.java)
                val call = request.updateItem(listaActivity.itemDb.id.toInt(), listaActivity.itemDb, API_KEY)
                call.enqueue(object : Callback<ItemBDD> {
                    override fun onResponse(
                        call: Call<ItemBDD>,
                        response: Response<ItemBDD>
                    ) {
                        println(response)
                        if (response.isSuccessful) {
                            if (response.body() != null) {
                                listaActivity.itemDb.updated_at = response.body()!!.updated_at
                                UpdateItemDBEnding(listaActivity).execute()
                                println(response.body())
                            }
                        }
                    }
                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onFailure(call: Call<ItemBDD>, t: Throwable) {
                        val current = LocalDateTime.now()
                        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                        val formatted = current.format(formatter)

                        listaActivity.itemDb.updated_at = formatted
                        UpdateItemDB(listaActivity).execute(listaActivity.itemDb)
                        println("NO FUNCIONA ${t.message}")
                    }
                })
                return null
            }
        }

        class UpdateItemDB(private val listaActivity: ItemDetails):
            AsyncTask<ItemBDD, Void, Void>() {
            override fun doInBackground(vararg params: ItemBDD?): Void? {
                listaActivity.database.updateItem(listaActivity.itemDb)
                return null
            }
        }

        class UpdateItemDBEnding(private val listaActivity: ItemDetails):
            AsyncTask<ItemBDD, Void, Void>() {
            override fun doInBackground(vararg params: ItemBDD?): Void? {
                listaActivity.database.updateItem(listaActivity.itemDb)
                return null
            }

            override fun onPostExecute(result: Void?) {
                val myIntent: Intent = Intent()
                myIntent.putExtra("item updated",listaActivity.item as Serializable)
                myIntent.putExtra("item position modified", listaActivity.pos)
                myIntent.putExtra("copy item", listaActivity.copyOfItem as Serializable)
                myIntent.putExtra("all items back", listaActivity.allItems as Serializable)
                myIntent.putExtra("online", listaActivity.online)
                listaActivity.setResult(5, myIntent)
                listaActivity.finish()
            }
        }

        class EraseItem(private val listaActivity: ItemDetails):
            AsyncTask<ItemBDD, Void, Void>() {
            override fun doInBackground(vararg params: ItemBDD?): Void? {
                listaActivity.database.deleteItem(params[0]!!)
                val request = UserService.buildService(PersonApi::class.java)
                val call = request.deleteItem(params[0]!!.id.toInt(), API_KEY)
                call.enqueue(object : Callback<ItemBDD> {
                    override fun onResponse(
                        call: Call<ItemBDD>,
                        response: Response<ItemBDD>
                    ) {
                        println(response)
                        if (response.isSuccessful) {
                            if (response.body() != null) {
                                println("funciona")
                                println(response.body())
                            }
                        }
                    }
                    override fun onFailure(call: Call<ItemBDD>, t: Throwable) {
                        println("NO FUNCIONA ${t.message}")
                    }
                })
                return null
            }
        }
    }
}