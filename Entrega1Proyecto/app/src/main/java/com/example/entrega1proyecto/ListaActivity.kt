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

    var listaList: ArrayList<ListWithItems> = ArrayList()
    var startingListaList: ArrayList<ListWithItems> = ArrayList()
    //lateinit var itemsRecibidos: ListWithItems
    var modified: ListWithItems? = null
    var validador: Boolean = false
    var user: User? = null
    var isShowingDialog = false
    var dialog: Dialog? = null
    // Database
    lateinit var database: ListDao
    var testListaList: ArrayList<ListWithItems> = ArrayList()
    var listsCounter: Long = 0
    lateinit var adapter: AdaptadorCustom

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista)

        //Create the database
        database = Room.databaseBuilder(this, Database::class.java,"ListsBDD").fallbackToDestructiveMigration().build().ListDao()
        GetAllLists(this).execute()
/*      testListaList = ArrayList(database.getListWithItems())

        /*database.getAllItems().forEach {
            database.deleteItem(it)
        }
        database.getListWithItems().forEach{
            database.deleteList(it.list)
        }


        println("BORRANDOOO TODOOOO    ${database.getListWithItems()}")

         */

        if (testListaList.size > 0 && listsCounter == 0.toLong()) {
            listsCounter = testListaList[testListaList.lastIndex].list.id
        }


*/
        // The recycler view for the Activity that contains the lists
        adapter = AdaptadorCustom(this, this)
        recycler_view.adapter = adapter
        recycler_view.layoutManager = LinearLayoutManager(this)

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


        //This is for the Drag and Drop ----------------------------------------------------------------------------

        val touchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {

                val sourcePosition = viewHolder.adapterPosition
                val targetPosition = target.adapterPosition
                Collections.swap(testListaList, sourcePosition, targetPosition)
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
    override fun onItemCLicked(result: ListWithItems){
        // We keep the List who was clicked
        modified = result
        // Go to the items in a list activity
        val intent = Intent(this, listDetails::class.java)
        intent.putExtra("List Id", result.list!!.id)
        startActivityForResult(intent, 1)
    }

    // Ask for the result from the items in a list activity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            if (resultCode == Activity.RESULT_OK){
                GetSpecificList(
                    this
                )
                /*var x = testListaList.indexOf(modified)
                testListaList[x] = database.getSpecificList(modified!!.list.id)
                recycler_view.adapter!!.notifyItemChanged(x)*/
            }
            else if (resultCode == 2){
                user = data.getSerializableExtra("user details updated") as User
                val endIntent = Intent()

                // We give the result to the Log in activity to maintain the information
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
    override fun onTrashCLicked(result: ListWithItems) {
        Trash(this).execute(result)
/*
        var pos = testListaList.indexOf(result)
        database.deleteList(result.list)
        database.deleteListItems(result.list.id)
        testListaList.removeAt(pos)
        recycler_view.adapter?.notifyItemRemoved(pos)
        
 */
    }

    // Function when was clicked the username to log out
    fun logOutPopUp(view: View){
        val intent = Intent(this, UserDetails::class.java)
        // We give the result to the Log in activity to maintain the information
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
                // Add it to the database

                insertList(
                    this@ListaActivity
                ).execute(ListBDD(listsCounter,view.listNameTextView.text.toString()))
/*
                listsCounter = database.insertList(ListBDD(listsCounter,view.listNameTextView.text.toString())) + 1

                // Add it to the List
                testListaList.add(ListWithItems(ListBDD(listsCounter-1,view.listNameTextView.text.toString()),null))
                recycler_view.adapter?.notifyItemInserted(testListaList.size - 1)

 */
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
        if (modified != null) {
            savedInstanceState.putSerializable("ItemModificado", modified as Serializable)
        }
        savedInstanceState.putBoolean("IS_SHOWING_DIALOG", isShowingDialog)
        println("ANTES DE ROTAAAR     $testListaList")


        savedInstanceState.putBoolean("validador", validador)
    }

    // Function to obtain what was given before changing the state of the activity
    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        // Obtain the username
        user = savedInstanceState?.getSerializable("person") as User
        nombreUsuarioTextView.text = user!!.name
        // Obtain the modified items on the list
        try {
            modified = savedInstanceState?.getSerializable("ItemModificado") as ListWithItems
        }catch (error: TypeCastException){
            println("NO se pudo")
        }

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
        endIntent.putExtra("user details finish",user as Serializable)
        setResult(Activity.RESULT_OK, endIntent)
        finish()
        super.onBackPressed()
    }

    companion object {
        var LISTS = "LISTS"

        class GetAllLists(private val listaActivity: ListaActivity) : AsyncTask<Void, Void, ArrayList<ListWithItems>>(){
            override fun doInBackground(vararg params: Void?): ArrayList<ListWithItems> {
                listaActivity.testListaList =
                    ArrayList(listaActivity.database.getListWithItems())
                println("esta es la lista guardada dps de rotar     ${listaActivity.testListaList}")
                return listaActivity.testListaList
            }

            override fun onPostExecute(result: ArrayList<ListWithItems>?) {
                if (listaActivity.testListaList.size > 0 && listaActivity.listsCounter == 0.toLong()) {
                    listaActivity.listsCounter = listaActivity.testListaList[listaActivity.testListaList.lastIndex].list.id
                }

                // The recycler view for the Activity that contains the lists
                listaActivity.adapter.setData(listaActivity.testListaList)
            }
        }

        class GetSpecificList(private val listaActivity: ListaActivity) : AsyncTask<Void, Void, ListWithItems>(){
            override fun doInBackground(vararg params: Void?): ListWithItems {
                var x = listaActivity.testListaList.indexOf(listaActivity.modified)
                listaActivity.testListaList[x] = listaActivity.database.getSpecificList(
                    listaActivity.modified!!.list.id)
                return listaActivity.testListaList[x]
            }

            override fun onPostExecute(result: ListWithItems?) {
                var x = listaActivity.testListaList.indexOf(listaActivity.modified)
                listaActivity.recycler_view.adapter!!.notifyItemChanged(x)
            }
        }

        class insertList(private val listaActivity: ListaActivity) : AsyncTask<ListBDD, Void, ListBDD>(){
            override fun doInBackground(vararg params: ListBDD?): ListBDD? {
                listaActivity.listsCounter = listaActivity.database.insertList(params[0]!!) + 1
                return params[0]!!
            }

            override fun onPostExecute(result: ListBDD?) {
                listaActivity.testListaList.add(
                    ListWithItems(
                        result!!,
                        null
                    )
                )
                listaActivity.adapter.notifyItemInserted(listaActivity.testListaList.size - 1)
                println("SE INSERTO EL ITEM      $result")
            }

        }

        class Trash(private val listaActivity: ListaActivity) : AsyncTask<ListWithItems, Void, ListWithItems>(){
            override fun doInBackground(vararg params: ListWithItems?): ListWithItems? {
                listaActivity.database.deleteList(params[0]!!.list)
                listaActivity.database.deleteListItems(params[0]!!.list.id)
                return params[0]!!
            }
    
            override fun onPostExecute(result: ListWithItems?) {
                var pos: Int = listaActivity.testListaList.indexOf(result)
                listaActivity.testListaList.removeAt(pos)
                listaActivity.recycler_view.adapter?.notifyItemRemoved(pos)
            }
    
        }
    }
}



