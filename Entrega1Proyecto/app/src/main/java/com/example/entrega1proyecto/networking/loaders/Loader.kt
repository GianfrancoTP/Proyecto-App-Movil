package com.example.entrega1proyecto.networking.loaders

import android.content.Context
import android.os.AsyncTask
import androidx.room.Room
import com.example.entrega1proyecto.model.adapters.ListItems
import com.example.entrega1proyecto.ListaActivity
import com.example.entrega1proyecto.configuration.API_KEY
import com.example.entrega1proyecto.model.*
import com.example.entrega1proyecto.networking.PersonApi
import com.example.entrega1proyecto.networking.UserService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


// Here we create the db
lateinit var database: ListDao
lateinit var activityComing: ListaActivity

// Obtain All lists from the api
class GetListsFromApi(val context: Context, private val act: ListaActivity) : AsyncTask<Void, Void, Void>() {
    override fun doInBackground(vararg params: Void?): Void? {

        activityComing = act

        database = Room.databaseBuilder(context, Database::class.java, "ListsBDD").build().ListDao()
        val request = UserService.buildService(PersonApi::class.java)
        val call = request.getAllList(API_KEY)
        call.enqueue(object : Callback<List<ListBDD>> {
            override fun onResponse(
                call: Call<List<ListBDD>>,
                response: Response<List<ListBDD>>
            ) {
                if (response.isSuccessful) {
                    if (response.body() != null) {
                        PostListsToDB()
                            .execute(response.body())
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
class PostListsToDB() :
    AsyncTask<List<ListBDD>, Void, ArrayList<Long>>() {
    override fun doInBackground(vararg params: List<ListBDD>?): ArrayList<Long>? {
        var idFaltantes = ArrayList<Long>()
        params[0]!!.forEach {
            idFaltantes.add(it.id)
            val lisBDD = database.getSpecificList(it.id)
            if (lisBDD == null){
                PostToDBB2().execute(it)
            }
            else{
                val listBDD = lisBDD.list
                if (it.updated_at > listBDD.updated_at) {
                    UpdateListsToDBB()
                        .execute(it)
                } else if (it.updated_at <= listBDD.updated_at) {
                    UpdateListToAPI()
                        .execute(listBDD)
                }
            }
            GetItemsFromApi().execute(it)
        }
        return idFaltantes
    }

    override fun onPostExecute(result: ArrayList<Long>?) {
        GetListsFromDbb().execute(result)
    }
}

// Post an non-existent list in the Db
class PostToDBB2() :
    AsyncTask<ListBDD, Void, Void>() {
    override fun doInBackground(vararg params: ListBDD?): Void? {
        database.insertList(params[0]!!)
        return null
    }
}

// Update lists that are not updated in the db
class UpdateListsToDBB() :
    AsyncTask<ListBDD, Void, Void>() {
    override fun doInBackground(vararg params: ListBDD?): Void? {
        database.updateList(params[0]!!)
        return null
    }
}

// Obtain all the lists from the Db
class GetListsFromDbb() :
    AsyncTask<ArrayList<Long>, Void, Void>() {
    override fun doInBackground(vararg params: ArrayList<Long>?): Void? {
        val k = database.getListWithItems()
        if (k.size != params[0]!!.size) {
            k.forEach {
                var id = it.list.id
                if (!params[0]!!.contains(id)) {
                    PostListToAPI()
                        .execute(it.list)
                }
            }
        }
        return null
    }
}

// Post an non-existent list in the API
class PostListToAPI() :
    AsyncTask<ListBDD, Void, Void>() {
    override fun doInBackground(vararg params: ListBDD?): Void? {
        val request = UserService.buildService(PersonApi::class.java)
        if(!params[0]!!.isSharedList) {
            val call = request.postList(params[0]!!, API_KEY)
            call.enqueue(object : Callback<ListBDD> {
                override fun onResponse(
                    call: Call<ListBDD>,
                    response: Response<ListBDD>
                ) {
                    print(response)
                    if (response.isSuccessful) {
                        if (response.body() != null) {
                            EraseListFromBD(
                                response.body()!!
                            ).execute(params[0]!!)

                        }
                    }
                }

                override fun onFailure(call: Call<ListBDD>, t: Throwable) {
                }
            })
        }
        else{
            val call = request.updateList(params[0]!!.id.toInt(),params[0]!!, API_KEY)
            call.enqueue(object : Callback<ListBDD> {
                override fun onResponse(
                    call: Call<ListBDD>,
                    response: Response<ListBDD>
                ) {
                    print(response)
                    if (response.isSuccessful) {
                        if (response.body() != null) {
                            response.body()!!.isSharedList = true
                            EraseListFromBD(
                                response.body()!!
                            ).execute(params[0]!!)

                        }
                    }
                }
                override fun onFailure(call: Call<ListBDD>, t: Throwable) {
                }
            })
        }

        return null
    }
}

class EraseListFromBD(val id: ListBDD) :
    AsyncTask<ListBDD, Void, ListBDD>() {
    lateinit var x:ListWithItems
    override fun doInBackground(vararg params: ListBDD?): ListBDD? {
        x = database.getSpecificList(params[0]!!.id)
        database.deleteList(params[0]!!)
        return id
    }
    override fun onPostExecute(result: ListBDD?) {
        PostToDBB(x).execute(result!!)
    }
}

// Post an non-existent list in the Db
class PostToDBB(val x : ListWithItems) :
    AsyncTask<ListBDD, Void, Void>() {
    lateinit var para: ListBDD
    override fun doInBackground(vararg params: ListBDD?): Void? {
        para = params[0]!!
        database.insertList(params[0]!!)
        return null
    }

    override fun onPostExecute(result: Void?) {
        x.items?.forEach {
            EraseItemInDb().execute(it)
            it.list_id = para.id
            PostItemToAPI2().execute(it)
        }
    }
}

class EraseItemInDb() :
    AsyncTask<ItemBDD, Void, Void>() {
    override fun doInBackground(vararg params: ItemBDD?): Void? {
        database.deleteItem(params[0]!!)
        return null
    }
}

class PostItemToAPI2() :
    AsyncTask<ItemBDD, Void, Void>() {
    override fun doInBackground(vararg params: ItemBDD?): Void? {
        val listWithItems =
            ListItems(listOf(params[0]!!))
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
                        InsertItemInDB()
                            .execute(response.body()!![0])
                    }
                }
            }
            override fun onFailure(call: Call<List<ItemBDD>>, t: Throwable) {
            }
        })

        return null
    }
}

class InsertItemInDB() :
    AsyncTask<ItemBDD, Void, Void>() {
    override fun doInBackground(vararg params: ItemBDD?): Void? {
        database.insertItem(params[0]!!)
        return null
    }

    override fun onPostExecute(result: Void?) {
        ListaActivity.Companion.GetAllLists(
            activityComing
        ).execute()
    }
}
//                  Para cuando no hay items en la lista

//Update a List into the API
class UpdateListToAPI() :
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
                        UpdateListsToDBB()
                            .execute(params[0]!!)
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

class GetItemsFromApi() :
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
                            LoadItemsToBD()
                                .execute(response.body())
                        }
                        else{
                            ListaActivity.Companion.GetAllLists(
                                activityComing
                            ).execute()
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

class LoadItemsToBD() :
    AsyncTask<List<ItemBDD>, Void, ArrayList<Long>>() {
    var idLista: ArrayList<Long> = ArrayList(0)
    override fun doInBackground(vararg params: List<ItemBDD>?): ArrayList<Long>? {
        idLista = arrayListOf(params[0]!![0].list_id)
        var idItemsFaltantes = ArrayList<Long>()
        params[0]!!.forEach {
            idItemsFaltantes.add(it.id)
            it.isShown = !it.done
            val iteBDD = database.getSpecificItem(it.id)
            if (iteBDD == null){
                PostItemToDBB().execute(it)
            }
            else{
                if (it.updated_at > iteBDD.updated_at) {
                    UpdateItemToDBB()
                        .execute(it)
                } else if (it.updated_at <= iteBDD.updated_at) {
                    UpdateItemToAPI()
                        .execute(iteBDD)
                }
            }
        }
        return idItemsFaltantes
    }
    override fun onPostExecute(result: ArrayList<Long>?) {
        GetItemsFromDbb()
            .execute(result, idLista)
    }
}

class UpdateItemToDBB() :
    AsyncTask<ItemBDD, Void, Void>() {
    override fun doInBackground(vararg params: ItemBDD?): Void? {
        database.updateItem(params[0]!!)
        return null
    }
}

class GetItemsFromDbb() :
    AsyncTask<ArrayList<Long>, Void, Void>() {
    override fun doInBackground(vararg params: ArrayList<Long>?): Void? {
        val k = database.getSpecificList(params[1]!![0]).items
        if (k?.size != params[0]!!.size) {
            k?.forEach {
                var id = it.id
                if (!params[0]!!.contains(id)) {
                    PostItemToAPI()
                        .execute(it)
                }
            }
        }
        return null
    }
}

// Post an non-existent list in the API
class PostItemToAPI() :
    AsyncTask<ItemBDD, Void, Void>() {
    override fun doInBackground(vararg params: ItemBDD?): Void? {
        val listWithItems =
            ListItems(listOf(params[0]!!))
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
                        UpdateItemToDBB()
                            .execute(params[0]!!)
                    }
                }
            }
            override fun onFailure(call: Call<List<ItemBDD>>, t: Throwable) {
            }
        })

        return null
    }
}

class PostItemToDBB() :
    AsyncTask<ItemBDD, Void, Void>() {
    override fun doInBackground(vararg params: ItemBDD?): Void? {
        database.insertItem(params[0]!!)
        return null
    }

    override fun onPostExecute(result: Void?) {
        ListaActivity.Companion.GetAllLists(
            activityComing
        ).execute()
    }

}

class UpdateItemToAPI() :
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
                        UpdateItemToDBB()
                            .execute(params[0]!!)
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