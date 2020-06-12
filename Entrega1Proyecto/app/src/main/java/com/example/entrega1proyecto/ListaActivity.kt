package com.example.entrega1proyecto

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.entrega1proyecto.model.*
import kotlinx.android.synthetic.main.activity_lista.*
import kotlinx.android.synthetic.main.popup.view.*
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList


class ListaActivity : AppCompatActivity(), OnItemClickListener, OnTrashClickListener{

    var listaList: ArrayList<ListaItem> = ArrayList()
    var startingListaList: ArrayList<ListaItem> = ArrayList()
    var itemsRecibidos: ListaItem = ListaItem("")
    var modified: ListaItem = ListaItem("")
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista)

        // The recycler view for the Activity that contains the lists
        adapter = AdaptadorCustom(this, this)
        recycler_view.adapter = adapter
        recycler_view.layoutManager = LinearLayoutManager(this)

        // Here we create the db
        database = Room.databaseBuilder(this, Database::class.java, "ListsBDD").build().ListDao()

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
        }

        user = intent.getSerializableExtra("user details start") as User
        nombreUsuarioTextView.text = user!!.name

        // This is to keep the Lists if we got back to the Log In activity
        if (!validador) {
            if (intent?.getSerializableExtra("lista") != null) {
                validador = true
                // This is to mantain the list if we change the orientation of the phone
/*
                startingListaList = intent.getSerializableExtra("lista")!! as ArrayList<ListaItem>
                createLists(startingListaList)
*/
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
                Collections.swap(listaList, sourcePosition, targetPosition)
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
/*
    // The function to maintain the lists if we rotate the screen or come from the login activity
    private fun createLists(startingListaList: ArrayList<ListaItem>){
        startingListaList.forEach {
            listaList.add(it)
            recycler_view.adapter?.notifyItemInserted(listaList.size - 1)
        }
    }
*/
    // Function to go to the activity which contains the items inside a list
    override fun onItemCLicked(result: ListaItem){
        // We keep the List who was clicked
        modified = result
        // Go to the items in a list activity
        val intent = Intent(this, listDetails::class.java)
        intent.putExtra(LISTS, map[result])
        startActivityForResult(intent, 1)
    }

    // Ask for the result from the items in a list activity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            if (resultCode == Activity.RESULT_OK){
                listaList = ArrayList()
                adapter.notifyDataSetChanged()
                GetAllLists(this).execute()
                // ACA PUEDE SER QUE NOS FALTE OBTENER LA NUEVA LISTA CON SUS ITEMS (UPDATEAR LA LISTA PARA QUE TENGA SUS NUEVOS ITEMS)
/*
                // We get the updated List with all the created items
                itemsRecibidos = data.getSerializableExtra("listaItems") as ListaItem
                var x = listaList.indexOf(modified)
                // We update the changed list inside the array of lists
                listaList[x] = itemsRecibidos
                recycler_view.adapter?.notifyItemChanged(x)
 */
            }
            else if (resultCode == 2){
                user = data.getSerializableExtra("user details updated") as User
                val endIntent = Intent()
                // We give the result to the Log in activity to maintain the information
                endIntent.putExtra("lista de listas",listaList as Serializable)
                endIntent.putExtra("user details finish",user as Serializable)
                setResult(Activity.RESULT_OK, endIntent)
                finish()
            }
            else if (resultCode == 3){
                user = data.getSerializableExtra("user details update") as User
                nombreUsuarioTextView.text = user!!.name
            }
        }
    }

    // Function when we want to delete a list
    override fun onTrashCLicked(result: ListaItem) {
        var pos = listaList.indexOf(result)
        Trash(this).execute(result)
        listaList.remove(result)
        recycler_view.adapter?.notifyItemRemoved(pos)
    }

    // Function when was clicked the username to log out
    fun logOutPopUp(view: View){
        val intent = Intent(this, UserDetails::class.java)
        // We give the result to the Log in activity to maintain the information
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
                val listToBeAdded = ListaItem(view.listNameTextView.text.toString(),ArrayList())
                listaList.add(listToBeAdded)
                InsertList(this@ListaActivity).execute(listToBeAdded)
                adapter.notifyItemInserted(listaList.size - 1)
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

        savedInstanceState.putBoolean("IS_SHOWING_DIALOG", isShowingDialog)

        savedInstanceState.putBoolean("validador", validador)
    }

    // Function to obtain what was given before changing the state of the activity
    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        // Obtain the username
        user = savedInstanceState?.getSerializable("person") as User
        nombreUsuarioTextView.text = user!!.name
        // Obtain the array list
        //startingListaList = savedInstanceState?.getSerializable("lista listas") as ArrayList<ListaItem>
        // Obtain the modified items on the list
        modified = savedInstanceState?.getSerializable("ItemModificado") as ListaItem

        adapter.notifyDataSetChanged()
        // if(!validador) {
/*
        createLists(startingListaList)
*/
        // }
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
        endIntent.putExtra("lista de listas",listaList as Serializable)
        endIntent.putExtra("user details finish",user as Serializable)
        setResult(Activity.RESULT_OK, endIntent)
        finish()
        super.onBackPressed()
    }

    companion object {
        var LISTS = "LISTS"

        // Class to get all the lists from the database
        class GetAllLists(private val listaActivity: ListaActivity) :
            AsyncTask<Void, Void, ArrayList<ListWithItems>>() {
            var list: ListaItem = ListaItem("")
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
                //After getting the elements from the db
                if (listaActivity.testListaList.size > 0 && listaActivity.listsCounter == 1.toLong()) {
                    listaActivity.listsCounter = listaActivity.testListaList[listaActivity.testListaList.lastIndex].list.id + 1
                }

                // Here we add it to the lista list
                listaActivity.testListaList.forEach{
                    list = ListaItem(it.list.name)
                    if (list.items == null){
                        list.items = ArrayList()
                    }
                    it.items?.forEach {x->
                        list.items!!.add(Item(x.nameItem, x.estado, x.prioridad, x.plazo,
                            x.notasItem, x.fechaCreacion, x.isShown))
                    }
                    listaActivity.map[list] = it.list
                    listaActivity.listaList.add(list)
                }
                listaActivity.adapter.setData(listaActivity.listaList)
            }
        }

        // Class to insert items into the db
        class InsertList(private val listaActivity: ListaActivity) : AsyncTask<ListaItem, Void, Void>(){
            override fun doInBackground(vararg params: ListaItem?): Void? {
                // We get the new id to add a new list when we add a list
                val listToBeAdded = ListBDD(listaActivity.listsCounter,params[0]!!.name)
                listaActivity.map[params[0]!!] = listToBeAdded
                listaActivity.listsCounter = listaActivity.database.insertList(listToBeAdded) + 1
                return null
            }
        }

        // Class when a list is removed
        class Trash(private val listaActivity: ListaActivity) : AsyncTask<ListaItem, Void, Void>(){
            override fun doInBackground(vararg params: ListaItem?): Void? {
                val listaABorrar = listaActivity.map[params[0]!!]
                listaActivity.database.deleteList(listaABorrar!!)
                listaActivity.database.deleteListItems(listaABorrar!!.id)
                return null
            }
        }
    }
}