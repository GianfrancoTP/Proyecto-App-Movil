package com.example.entrega1proyecto

import android.content.Context
import android.os.AsyncTask
import androidx.room.Room
import com.example.entrega1proyecto.configuration.API_KEY
import com.example.entrega1proyecto.model.Database
import com.example.entrega1proyecto.model.ItemBDD
import com.example.entrega1proyecto.model.ListBDD
import com.example.entrega1proyecto.model.ListDao
import com.example.entrega1proyecto.networking.PersonApi
import com.example.entrega1proyecto.networking.UserService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


// Here we create the db
lateinit var databaselistDetails: ListDao
lateinit var activityCominglistDetails: listDetails
var listID = 0.toLong()

// Obtain All lists from the api
class GetListsFromApilistDetails(val context: Context, private val act: listDetails, private val id: Long) : AsyncTask<Void, Void, Void>() {
    override fun doInBackground(vararg params: Void?): Void? {

        activityCominglistDetails = act
        listID = id

        databaselistDetails = Room.databaseBuilder(context, Database::class.java, "ListsBDD").build().ListDao()
        val request = UserService.buildService(PersonApi::class.java)
        val call = request.getAllList(API_KEY)
        call.enqueue(object : Callback<List<ListBDD>> {
            override fun onResponse(
                call: Call<List<ListBDD>>,
                response: Response<List<ListBDD>>
            ) {
                if (response.isSuccessful) {
                    if (response.body() != null) {
                        PostListsToDBlistDetails().execute(response.body())
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
class PostListsToDBlistDetails() :
    AsyncTask<List<ListBDD>, Void, ArrayList<Long>>() {
    override fun doInBackground(vararg params: List<ListBDD>?): ArrayList<Long>? {
        var idFaltantes = ArrayList<Long>()
        params[0]!!.forEach {
            idFaltantes.add(it.id)
            val lisBDD = databaselistDetails.getSpecificList(it.id)
            if (lisBDD == null){
                PostToDBBlistDetails().execute(it)
            }
            else{
                val listBDD = lisBDD.list
                if (it.updated_at > listBDD.updated_at) {
                    UpdateListsToDBBlistDetails().execute(it)
                } else if (it.updated_at < listBDD.updated_at) {
                    UpdateListToAPIlistDetails().execute(listBDD)
                }
            }
            GetItemsFromApilistDetails().execute(it)
        }
        return idFaltantes
    }

    override fun onPostExecute(result: ArrayList<Long>?) {
        GetListsFromDbblistDetails().execute(result)
    }
}

// Update lists that are not updated in the db
class UpdateListsToDBBlistDetails() :
    AsyncTask<ListBDD, Void, Void>() {
    override fun doInBackground(vararg params: ListBDD?): Void? {
        databaselistDetails.updateList(params[0]!!)
        return null
    }
}

// Obtain all the lists from the Db
class GetListsFromDbblistDetails() :
    AsyncTask<ArrayList<Long>, Void, Void>() {
    override fun doInBackground(vararg params: ArrayList<Long>?): Void? {
        val k = databaselistDetails.getListWithItems()
        if (k.size != params[0]!!.size) {
            k.forEach {
                var id = it.list.id
                if (!params[0]!!.contains(id)) {
                    PostListToAPIlistDetails().execute(it.list)
                }
            }
        }
        return null
    }
}

// Post an non-existent list in the API
class PostListToAPIlistDetails() :
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
                        params[0]!!.id = response.body()!!.id
                        UpdateListsToDBBlistDetails().execute(params[0]!!)
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
class PostToDBBlistDetails() :
    AsyncTask<ListBDD, Void, Void>() {
    override fun doInBackground(vararg params: ListBDD?): Void? {
        databaselistDetails.insertList(params[0]!!)
        return null
    }
}
//                      ESTAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA ES PARA CUANDO NO HAY ITEMSSSSSSSSSSSSSSSSSS!!!!!!!!!!!!!!!!
//Update a List into the API
class UpdateListToAPIlistDetails() :
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
                        params[0]!!.id = response.body()!!.id
                        UpdateListsToDBBlistDetails().execute(params[0]!!)
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

class GetItemsFromApilistDetails() :
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
                        if (response.body()!!.isNotEmpty()) {
                            LoadItemsToBDlistDetails().execute(response.body())
                        }
                        else{
                            listDetails.Companion.GetTheList(activityCominglistDetails).execute(listID)
                        }
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

class LoadItemsToBDlistDetails() :
    AsyncTask<List<ItemBDD>, Void, ArrayList<Long>>() {
    var idLista: ArrayList<Long> = ArrayList(0)
    override fun doInBackground(vararg params: List<ItemBDD>?): ArrayList<Long>? {
        idLista = arrayListOf(params[0]!![0].list_id)
        var idItemsFaltantes = ArrayList<Long>()
        params[0]!!.forEach {
            idItemsFaltantes.add(it.id)
            it.isShown = !it.done
            val iteBDD = databaselistDetails.getSpecificItem(it.id)
            if (iteBDD == null){
                PostItemToDBBlistDetails().execute(it)
            }
            else{
                if (it.updated_at > iteBDD.updated_at) {
                    UpdateItemToDBBlistDetails().execute(it)
                } else if (it.updated_at < iteBDD.updated_at) {
                    UpdateItemToAPIlistDetails().execute(iteBDD)
                }
            }
        }
        return idItemsFaltantes
    }
    override fun onPostExecute(result: ArrayList<Long>?) {
        GetItemsFromDbblistDetails().execute(result, idLista)
    }
}

class UpdateItemToDBBlistDetails() :
    AsyncTask<ItemBDD, Void, Void>() {
    override fun doInBackground(vararg params: ItemBDD?): Void? {
        databaselistDetails.updateItem(params[0]!!)
        return null
    }
}

class GetItemsFromDbblistDetails() :
    AsyncTask<ArrayList<Long>, Void, Void>() {
    override fun doInBackground(vararg params: ArrayList<Long>?): Void? {
        val k = databaselistDetails.getSpecificList(params[1]!![0]).items
        if (k?.size != params[0]!!.size) {
            k?.forEach {
                var id = it.id
                if (!params[0]!!.contains(id)) {
                    PostItemToAPIlistDetails().execute(it)
                }
            }
        }
        return null
    }
}

// Post an non-existent list in the API
class PostItemToAPIlistDetails() :
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
                        params[0]!!.id = response.body()!![0].id
                        UpdateItemToDBBlistDetails().execute(params[0]!!)
                    }
                }
            }
            override fun onFailure(call: Call<List<ItemBDD>>, t: Throwable) {
            }
        })

        return null
    }
}

class PostItemToDBBlistDetails() :
    AsyncTask<ItemBDD, Void, Void>() {
    override fun doInBackground(vararg params: ItemBDD?): Void? {
        databaselistDetails.insertItem(params[0]!!)
        return null
    }

    override fun onPostExecute(result: Void?) {
        listDetails.Companion.GetTheList(activityCominglistDetails).execute(listID)
    }

}

class UpdateItemToAPIlistDetails() :
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
                        params[0]!!.id = response.body()!!.id
                        UpdateItemToDBBlistDetails().execute(params[0]!!)
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