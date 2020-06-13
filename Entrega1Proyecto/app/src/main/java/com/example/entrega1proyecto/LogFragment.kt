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
import com.example.entrega1proyecto.ListaActivity.Companion.LISTS
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

    fun goToList(){
        val intent = Intent(activity, ListaActivity::class.java)

        intent.putExtra("coming from Log In", true)
        intent.putExtra("user details start",user as Serializable)
        startActivityForResult(intent, 2)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data != null) {
            if (resultCode == Activity.RESULT_OK){
                user = data.getSerializableExtra("user details finish") as User
                emailTextView.setText(user!!.email)
                passwordTextView.setText(user!!.first_name)
            }
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_log, container, false)
        val button = rootView.findViewById<Button>(R.id.IngresarButton)
        // Here we create the db
        database = Room.databaseBuilder(activity!!.applicationContext, Database::class.java, "ListsBDD").build().ListDao()

        try{
            user = savedInstanceState?.getSerializable("user details update") as User
            emailTextView.setText(user!!.email)
            passwordTextView.setText(user!!.first_name)
        }catch (e: Exception){
            GetUserFromApi(this).execute()
            GetListsFromApi(this).execute()
        }

        button.setOnClickListener { goToList() }
        return rootView
    }

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
                            LoadListsToBD(listaActivity).execute(response.body())
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

    class LoadListsToBD(private val listaActivity: LogFragment) : AsyncTask<List<ListBDD>, Void, Void>() {
        override fun doInBackground(vararg params: List<ListBDD>?): Void? {
            params[0]!!.forEach {
                listaActivity.database.insertList(it)
                GetItemsFromApi(listaActivity).execute(it)
            }
            return null
        }

    }

    class GetItemsFromApi(private val listaActivity: LogFragment) : AsyncTask<ListBDD, Void, Void>() {
        override fun doInBackground(vararg params: ListBDD?): Void? {
            val request = UserService.buildService(PersonApi::class.java)
            val call = request.getAllItem(params[0]!!.id.toInt(), API_KEY)
            call.enqueue(object : Callback<List<ItemBDD>> {
                override fun onResponse(
                    call: Call<List<ItemBDD>>,
                    response: Response<List<ItemBDD>>
                ) {
                    println(response)
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

    class LoadItemsToBD(private val listaActivity: LogFragment) : AsyncTask<List<ItemBDD>, Void, Void>() {
        override fun doInBackground(vararg params: List<ItemBDD>?): Void? {
            params[0]!!.forEach {
                val h = it
                h.isShown = !it.done
                listaActivity.database.insertItem(h)
            }
            return null
        }
    }
}
