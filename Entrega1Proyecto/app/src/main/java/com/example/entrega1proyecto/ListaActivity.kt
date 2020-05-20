package com.example.entrega1proyecto

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.entrega1proyecto.model.User
import com.example.entrega1proyecto.networking.PersonApi
import com.example.entrega1proyecto.networking.UserService
import kotlinx.android.synthetic.main.activity_lista.*
import kotlinx.android.synthetic.main.popup.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList


class ListaActivity : AppCompatActivity(), OnItemClickListener, OnTrashClickListener{

    var listaList: ArrayList<ListaItem> = ArrayList()
    var startingListaList: ArrayList<ListaItem> = ArrayList()
    var itemsRecibidos: ListaItem = ListaItem("")
    var modified: ListaItem = ListaItem("")
    var username: String? = null
    var validador: Boolean = false
    var user: User? = null

    companion object {
        var LISTS = "LISTS"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista)
        val request = UserService.buildService(PersonApi::class.java)

        // The recycler view for the Activity that contains the lists
        recycler_view.adapter = AdaptadorCustom(listaList, this, this)
        recycler_view.layoutManager = LinearLayoutManager(this)

        // This is to obtain the username of the logged person
        username = intent.getStringExtra("email")!!
        val call = request.getUsers()
        call.enqueue(object: Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    if (response.body() != null) {
                        user = response.body()!!
                        nombreUsuarioTextView.text = user!!.name
                    }
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Toast.makeText(this@ListaActivity, "${t.message}", Toast.LENGTH_SHORT).show()
            }
        })

        // This is to keep the Lists if we got back to the Log In activity
        if (intent?.getSerializableExtra("lista") != null) {
            validador = true
            // This is to mantain the list if we change the orientation of the phone
            startingListaList = intent.getSerializableExtra("lista")!! as ArrayList<ListaItem>
            createLists(startingListaList)
        }
        nombreUsuarioTextView.text = username

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

    // The function to maintain the lists if we rotate the screen or come from the login activity
    private fun createLists(startingListaList: ArrayList<ListaItem>){
        startingListaList.forEach {
            listaList.add(it)
            recycler_view.adapter?.notifyItemInserted(listaList.size - 1)
        }
    }

    // Function to go to the activity which contains the items inside a list
    override fun onItemCLicked(result: ListaItem){
        // We keep the List who was clicked
        modified = result
        // Go to the items in a list activity
        val intent = Intent(this, listDetails::class.java)
        intent.putExtra(LISTS, result)
        startActivityForResult(intent, 1)
    }

    // Ask for the result from the items in a list activity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            if (resultCode == Activity.RESULT_OK){
                // We get the updated List with all the created items
                itemsRecibidos = data.getSerializableExtra("listaItems") as ListaItem
                var x = listaList.indexOf(modified)
                // We update the changed list inside the array of lists
                listaList[x] = itemsRecibidos
                recycler_view.adapter?.notifyItemChanged(x)
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
        }
    }

    // Function when we want to delete a list
    override fun onTrashCLicked(result: ListaItem) {
        var pos = listaList.indexOf(result)
        listaList.remove(result)
        recycler_view.adapter?.notifyItemRemoved(pos)
    }

    // Function when was clicked the username to log out
    fun logOutPopUp(view: View){
        val intent = Intent(this, UserDetails::class.java)
        // We give the result to the Log in activity to maintain the information
        intent.putExtra("lista de listas",listaList)
        intent.putExtra("user details", user as Serializable)
        startActivityForResult(intent, 1)
    }

    // Function to create new lists
    fun plusButton(view: View){
        // We show a Dialog asking what's the name of the list we want to create
        var builder: AlertDialog.Builder = AlertDialog.Builder(this)
        var inflater: LayoutInflater = layoutInflater
        var view: View = inflater.inflate(R.layout.popup,null)
        builder.setView(view)

        // If they don't want to create a new List we resume what we were showing
        builder.setNegativeButton("Cancelar", object: DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                dialog?.dismiss()
            }
        })

        // Here we create the new list with null items inside it
        builder.setPositiveButton("Confirmar",object:  DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                listaList.add(ListaItem(view.listNameTextView.text.toString(),null))
                recycler_view.adapter?.notifyItemInserted(listaList.size - 1)
                dialog?.dismiss()
            }
        })
        var dialog: Dialog = builder.create()
        dialog.show()
    }

    // Function to maintain the data when the activity is changed of state
    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        // We give the username
        savedInstanceState.putString("nombreDeUsuarioTextView", nombreUsuarioTextView.text.toString())
        // We give the changed item if the screen is rotated before being saved in the array of lists
        savedInstanceState.putSerializable("ItemModificado",modified)
        // We give the array of lists
        savedInstanceState.putSerializable("lista listas",listaList)
    }

    // Function to obtain what was given before changing the state of the activity
    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        // Obtain the username
        nombreUsuarioTextView.text = savedInstanceState?.getString("nombreDeUsuarioTextView")
        // Obtain the array list
        startingListaList = savedInstanceState?.getSerializable("lista listas") as ArrayList<ListaItem>
        // Obtain the modified items on the list
        modified = savedInstanceState?.getSerializable("ItemModificado") as ListaItem
        if(!validador) {
            createLists(startingListaList)
        }
    }
}



