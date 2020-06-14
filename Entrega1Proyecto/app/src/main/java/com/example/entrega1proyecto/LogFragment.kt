package com.example.entrega1proyecto

import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.room.Room
import com.example.entrega1proyecto.configuration.API_KEY
import com.example.entrega1proyecto.model.*
import com.example.entrega1proyecto.networking.PersonApi
import com.example.entrega1proyecto.networking.UserService
import kotlinx.android.synthetic.main.fragment_log.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.Serializable

class LogFragment : Fragment() {
    var user: User? = null

    // Db
    lateinit var database: ListDao

    fun goToList() {

        if (user != null) {
            val intent = Intent(activity, ListaActivity::class.java)
            intent.putExtra("coming from Log In", true)
            intent.putExtra("user details start", user as Serializable)
            startActivityForResult(intent, 2)
        } else {
            Toast.makeText(context, "Debe tener internet para hacer Log In!", Toast.LENGTH_LONG)
                .show()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data != null) {
            if (resultCode == Activity.RESULT_OK) {
                user = data.getSerializableExtra("user details finish") as User
                emailTextView.setText(user!!.email)
                passwordTextView.setText(user!!.first_name)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_log, container, false)
        val button = rootView.findViewById<Button>(R.id.IngresarButton)
        // Here we create the db
        CheckConnection(this).execute()
        database =
            Room.databaseBuilder(activity!!.applicationContext, Database::class.java, "ListsBDD")
                .build().ListDao()

        try {
            user = savedInstanceState?.getSerializable("user details update") as User
            SetUserToDB(this).execute(user)
            emailTextView.setText(user!!.email)
            passwordTextView.setText(user!!.first_name)
        } catch (e: Exception) {
            GetUserFromApi(this).execute()
            GetListsFromApi(this).execute()
        }

        button.setOnClickListener { goToList() }
        return rootView
    }

    class CheckConnection(private val listaActivity: LogFragment) : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            while (!isOnline(listaActivity.context!!)) {
            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            GetUserFromApi(listaActivity).execute()
        }
    }

/*
    class CompareToBDD(private val listaActivity: LogFragment) : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {

            return null
        }
    }

 */
    // Function to save user into the DB
    class SetUserToDB(private val listaActivity: LogFragment) : AsyncTask<User, Void, Void>() {
        override fun doInBackground(vararg params: User?): Void? {
            val user = params[0]!!
            val u1 = UserBBDD(
                1,
                user.first_name,
                user.last_name,
                user.email,
                user.phone,
                user.profile_photo
            )
            listaActivity.database.insertUser(u1)
            return null
        }
/*
        override fun onPostExecute(result: Void?) {
            CompareToBDD(listaActivity).execute()
        }
*/
    }

    // Function to obtain user from the api
    class GetUserFromApi(private val listaActivity: LogFragment) : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            val request = UserService.buildService(PersonApi::class.java)
            val call = request.getUsers(API_KEY)
            call.enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if (response.isSuccessful) {
                        if (response.body() != null) {
                            listaActivity.user = response.body()!!
                            listaActivity.emailTextView.setText(listaActivity.user!!.email)
                            listaActivity.passwordTextView.setText(listaActivity.user!!.first_name)
                            SetUserToDB(listaActivity).execute(listaActivity.user)
                        }
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    Toast.makeText(
                        listaActivity.activity!!.applicationContext,
                        "${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
            return null
        }
    }

    // Obtain All lists from the api
    class GetListsFromApi(private val listaActivity: LogFragment) : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            val request = UserService.buildService(PersonApi::class.java)
            val call = request.getAllList(API_KEY)
            call.enqueue(object : Callback<List<ListBDD>> {
                override fun onResponse(
                    call: Call<List<ListBDD>>,
                    response: Response<List<ListBDD>>
                ) {
                    if (response.isSuccessful) {
                        if (response.body() != null) {
                            PostListsToDB(listaActivity).execute(response.body())
                        }
                    }
                }

                override fun onFailure(call: Call<List<ListBDD>>, t: Throwable) {
                    println("NO FUNCIONA ${t.message}")
                }
            })
            return null
        }
    }

    // Load API lists into the DB
    class PostListsToDB(private val listaActivity: LogFragment) :
        AsyncTask<List<ListBDD>, Void, ArrayList<Long>>() {
        override fun doInBackground(vararg params: List<ListBDD>?): ArrayList<Long>? {
            var idFaltantes = ArrayList<Long>()
            params[0]!!.forEach {
                idFaltantes.add(it.id)
                val lisBDD = listaActivity.database.getSpecificList(it.id)
                if (lisBDD == null){
                    PostToDBB(listaActivity).execute(it)
                }
                else{
                    val listBDD = lisBDD.list
                    if (it.updated_at > listBDD.updated_at) {
                        UpdateListsToDBB(listaActivity).execute(it)
                    } else if (it.updated_at < listBDD.updated_at) {
                        UpdateListToAPI(listaActivity).execute(listBDD)
                    }
                }
                GetItemsFromApi(listaActivity).execute(it)
            }
            return idFaltantes
        }

        override fun onPostExecute(result: ArrayList<Long>?) {
            GetListsFromDbb(listaActivity).execute(result)
        }
    }

    // Update lists that are not updated in the db
    class UpdateListsToDBB(private val listaActivity: LogFragment) :
        AsyncTask<ListBDD, Void, Void>() {
        override fun doInBackground(vararg params: ListBDD?): Void? {
            listaActivity.database.updateList(params[0]!!)
            return null
        }
    }

    // Obtain all the lists from the Db
    class GetListsFromDbb(private val listaActivity: LogFragment) :
        AsyncTask<ArrayList<Long>, Void, Void>() {
        override fun doInBackground(vararg params: ArrayList<Long>?): Void? {
            val k = listaActivity.database.getListWithItems()
            if (k.size != params[0]!!.size) {
                k.forEach {
                    var id = it.list.id
                    if (!params[0]!!.contains(id)) {
                        PostListToAPI(listaActivity).execute(it.list)
                    }
                }
            }
            return null
        }

    }
    //  PUEDE SER QUE TENGAMOS QUE ACTUALIZAR LOS IDS DE LA BDD---------------------------------------------
    // Post an non-existent list in the API
    class PostListToAPI(private val listaActivity: LogFragment) :
        AsyncTask<ListBDD, Void, Void>() {
        override fun doInBackground(vararg params: ListBDD?): Void? {
            val request = UserService.buildService(PersonApi::class.java)
            val call = request.postList(params[0]!!, API_KEY)
            call.enqueue(object : Callback<ListBDD> {
                override fun onResponse(
                    call: Call<ListBDD>,
                    response: Response<ListBDD>
                ) {
                    print(response)
                    if (response.isSuccessful) {
                        if (response.body() != null) {

                        }
                    }
                }
                override fun onFailure(call: Call<ListBDD>, t: Throwable) {
                }
            })

            return null
        }
    }

    // Post an non-existent list in the Db
    class PostToDBB(private val listaActivity: LogFragment) :
        AsyncTask<ListBDD, Void, Void>() {
        override fun doInBackground(vararg params: ListBDD?): Void? {
            listaActivity.database.insertList(params[0]!!)
            return null
        }
    }
    //  PUEDE SER QUE TENGAMOS QUE ACTUALIZAR LOS IDS DE LA BDD---------------------------------------------
    //Update a List into the API
    class UpdateListToAPI(private val listaActivity: LogFragment) :
        AsyncTask<ListBDD, Void, Void>() {
        override fun doInBackground(vararg params: ListBDD?): Void? {
            val request = UserService.buildService(PersonApi::class.java)
            val call = request.updateList(params[0]!!.id.toInt(), params[0]!!, API_KEY)
            call.enqueue(object : Callback<ListBDD> {
                override fun onResponse(
                    call: Call<ListBDD>,
                    response: Response<ListBDD>
                ) {
                    if (response.isSuccessful) {
                        if (response.body() != null) {
                            println(response)
                        }
                    }
                }

                override fun onFailure(call: Call<ListBDD>, t: Throwable) {
                    println("NO FUNCIONA ${t.message}")
                }
            })
            return null
        }
    }

    class GetItemsFromApi(private val listaActivity: LogFragment) :
        AsyncTask<ListBDD, Void, Void>() {
        override fun doInBackground(vararg params: ListBDD?): Void? {
            val request = UserService.buildService(PersonApi::class.java)
            val call = request.getAllItem(params[0]!!.id.toInt(), API_KEY)
            call.enqueue(object : Callback<List<ItemBDD>> {
                override fun onResponse(
                    call: Call<List<ItemBDD>>,
                    response: Response<List<ItemBDD>>
                ) {
                    if (response.isSuccessful) {
                        if (response.body() != null) {
                            LoadItemsToBD(listaActivity).execute(response.body())
                            println(response.body())
                        }
                    }
                }

                override fun onFailure(call: Call<List<ItemBDD>>, t: Throwable) {
                    println("NO FUNCIONA ${t.message}")
                }
            })
            return null
        }
    }

    class LoadItemsToBD(private val listaActivity: LogFragment) :
        AsyncTask<List<ItemBDD>, Void, ArrayList<Long>>() {
        var idLista: ArrayList<Long> = ArrayList(0)
        override fun doInBackground(vararg params: List<ItemBDD>?): ArrayList<Long>? {
            idLista = arrayListOf(params[0]!![0].list_id)
            var idItemsFaltantes = ArrayList<Long>()
            params[0]!!.forEach {
                idItemsFaltantes.add(it.id)
                it.isShown = !it.done
                val iteBDD = listaActivity.database.getSpecificItem(it.id)
                if (iteBDD == null){
                    PostItemToDBB(listaActivity).execute(it)
                }
                else{
                    if (it.updated_at > iteBDD.updated_at) {
                        UpdateItemToDBB(listaActivity).execute(it)
                    } else if (it.updated_at < iteBDD.updated_at) {
                        UpdateItemToAPI(listaActivity).execute(iteBDD)
                    }
                }
            }
            return idItemsFaltantes
        }
        override fun onPostExecute(result: ArrayList<Long>?) {
            GetItemsFromDbb(listaActivity).execute(result, idLista)
        }
    }

    class UpdateItemToDBB(private val listaActivity: LogFragment) :
        AsyncTask<ItemBDD, Void, Void>() {
        override fun doInBackground(vararg params: ItemBDD?): Void? {
            listaActivity.database.updateItem(params[0]!!)
            return null
        }
    }

    class GetItemsFromDbb(private val listaActivity: LogFragment) :
        AsyncTask<ArrayList<Long>, Void, Void>() {
        override fun doInBackground(vararg params: ArrayList<Long>?): Void? {
            val k = listaActivity.database.getSpecificList(params[1]!![0]).items
            if (k?.size != params[0]!!.size) {
                k?.forEach {
                    var id = it.id
                    if (!params[0]!!.contains(id)) {
                        PostItemToAPI(listaActivity).execute(it)
                    }
                }
            }
            return null
        }

    }

    //  PUEDE SER QUE TENGAMOS QUE ACTUALIZAR LOS IDS DE LA BDD---------------------------------------------
    // Post an non-existent list in the API
    class PostItemToAPI(private val listaActivity: LogFragment) :
        AsyncTask<ItemBDD, Void, Void>() {
        override fun doInBackground(vararg params: ItemBDD?): Void? {
            val listWithItems = ListItems(listOf(params[0]!!))
            val request = UserService.buildService(PersonApi::class.java)
            val call = request.postItem(listWithItems, API_KEY)
            call.enqueue(object : Callback<List<ItemBDD>> {
                override fun onResponse(
                    call: Call<List<ItemBDD>>,
                    response: Response<List<ItemBDD>>
                ) {
                    print(response)
                    if (response.isSuccessful) {
                        if (response.body() != null) {

                        }
                    }
                }
                override fun onFailure(call: Call<List<ItemBDD>>, t: Throwable) {
                }
            })

            return null
        }
    }

    class PostItemToDBB(private val listaActivity: LogFragment) :
        AsyncTask<ItemBDD, Void, Void>() {
        override fun doInBackground(vararg params: ItemBDD?): Void? {
            listaActivity.database.insertItem(params[0]!!)
            return null
        }
    }

    //  PUEDE SER QUE TENGAMOS QUE ACTUALIZAR LOS IDS DE LA BDD---------------------------------------------
    class UpdateItemToAPI(private val listaActivity: LogFragment) :
        AsyncTask<ItemBDD, Void, Void>() {
        override fun doInBackground(vararg params: ItemBDD?): Void? {
            val request = UserService.buildService(PersonApi::class.java)
            val call = request.updateItem(params[0]!!.id.toInt(), params[0]!!, API_KEY)
            call.enqueue(object : Callback<ItemBDD> {
                override fun onResponse(
                    call: Call<ItemBDD>,
                    response: Response<ItemBDD>
                ) {
                    if (response.isSuccessful) {
                        if (response.body() != null) {
                            println(response)
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
