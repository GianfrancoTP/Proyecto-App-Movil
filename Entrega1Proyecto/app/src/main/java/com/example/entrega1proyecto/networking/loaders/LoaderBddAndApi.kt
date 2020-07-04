package com.example.entrega1proyecto.networking.loaders

import android.content.Context
import android.os.AsyncTask
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Room
import com.example.entrega1proyecto.LogFragment
import com.example.entrega1proyecto.MainActivity.Companion.VERIFICADOR
import com.example.entrega1proyecto.configuration.API_KEY
import com.example.entrega1proyecto.model.*
import com.example.entrega1proyecto.model.adapters.Item
import com.example.entrega1proyecto.model.adapters.ListItems
import com.example.entrega1proyecto.networking.PersonApi
import com.example.entrega1proyecto.networking.UserService
import com.example.entrega1proyecto.networking.isOnline
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

// Here we create the db
var allListsWithItemsFromDB: List<ListWithItems>? = null
var copyAllListsWithItemsFromDB: List<ListWithItems>? = null
var allListsWithItemsFromApi = ArrayList<ListWithItems>()

var allListsErased: List<Long>? = null
var allItemsErased: List<Long>? = null

var allListsOffline: List<ListBDD>? = null
var allItemsOffline: List<ItemBDD>? = null
var userOffline: User? = null

var userFromDB: UserBBDD? = null
var userFromApi: User? = null

lateinit var databaseLoader: ListDao
var allListsFromApi: List<ListBDD>? = null

lateinit var contextAll: Context

fun setDB(context: Context){
    contextAll = context
    databaseLoader = Room.databaseBuilder(context, Database::class.java, "ListsBDD").build().ListDao()
    GetAllFromDB().execute()
}
class GetAllFromDB(): AsyncTask<Void, Void, Void>() {
    override fun doInBackground(vararg params: Void?): Void? {
        copyAllListsWithItemsFromDB = databaseLoader.getListWithItems()
        allListsWithItemsFromDB = databaseLoader.getListWithItems()
        allItemsErased = databaseLoader.getAllItemsErased()
        allListsErased = databaseLoader.getAllListsErased()
        allItemsOffline = databaseLoader.getAllItemsOffline()
        allListsOffline = databaseLoader.getAllListsOffline()
        userOffline = databaseLoader.getUserOffline()
        userFromDB = databaseLoader.getUser()
        return null
    }

    override fun onPostExecute(result: Void?) {
        UpdateAndEraseAll()
    }
}

fun UpdateAndEraseAll(){
    // Primero borramos las listas borradas offline
    if (allListsErased != null && allListsErased?.size != 0){
        allListsErased?.forEach {
            val request = UserService.buildService(PersonApi::class.java)
            val call = request.deleteList(it.toInt(), API_KEY)
            call.enqueue(object : Callback<ListBDD> {
                override fun onResponse(
                    call: Call<ListBDD>,
                    response: Response<ListBDD>
                ) {
                    if (response.isSuccessful) {
                        EraseFromErasedLists().execute(it)
                    }
                }

                override fun onFailure(call: Call<ListBDD>, t: Throwable) {
                    VERIFICADOR = true
                }
            })
        }
    }

    // Luego borramos los items borrados offline
    if (allItemsErased != null && allItemsErased?.size != 0){
        allItemsErased?.forEach {
            val request = UserService.buildService(PersonApi::class.java)
            val call = request.deleteItem(it.toInt(), API_KEY)
            call.enqueue(object : Callback<ItemBDD> {
                override fun onResponse(
                    call: Call<ItemBDD>,
                    response: Response<ItemBDD>
                ) {
                    if (response.isSuccessful) {
                        EraseFromErasedItems().execute(it)
                    }
                }
                override fun onFailure(call: Call<ItemBDD>, t: Throwable) {
                    VERIFICADOR = true
                }
            })
        }
    }

    if (userOffline != null) {
        // Updateamos User offline
        val request = UserService.buildService(PersonApi::class.java)
        val call = request.updateUser(userOffline!!, API_KEY)
        call.enqueue(object : Callback<User> {
            override fun onResponse(
                call: Call<User>,
                response: Response<User>
            ) {
                if (response.isSuccessful) {
                    if (response.body() != null) {
                    }
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                println("NO FUNCIONA ${t.message}")
                VERIFICADOR = true
            }
        })
    }

    // Updateamos Listas offline
    if (allListsOffline != null && allListsOffline?.size != 0) {
        var count = 0
        allListsOffline?.forEach {
            val request = UserService.buildService(PersonApi::class.java)
            val call = request.updateList(it.id.toInt(), it, API_KEY)
            call.enqueue(object : Callback<ListBDD> {
                override fun onResponse(
                    call: Call<ListBDD>,
                    response: Response<ListBDD>
                ) {
                    count += 1
                    if (response.isSuccessful) {
                        if (response.body() != null) {
                            if(!it.isSharedList) {
                                UpdatearOnlineList(it, count).execute(it)
                            }
                        }
                    }
                    else{
                        if(!it.isSharedList) {
                            insertListInApi(it, count)
                        }
                    }
                }

                override fun onFailure(call: Call<ListBDD>, t: Throwable) {
                    VERIFICADOR = true
                }
            })
        }
    }
    else{
        if (allItemsOffline != null && allItemsOffline?.size != 0){
            updateAllOfflineItemsToApi()
        }
        else{
            GetAllFromApi()
        }
    }
}

fun insertListInApi(listToInsert: ListBDD, count: Int){
    val request = UserService.buildService(PersonApi::class.java)
    val callUploadLists = request.postList(listToInsert, API_KEY)
    callUploadLists.enqueue(object : Callback<ListBDD> {
        override fun onResponse(
            callUpload: Call<ListBDD>,
            response: Response<ListBDD>
        ) {
            print(response)
            if (response.isSuccessful) {
                if (response.body() != null) {
                    UpdatearOnlineList(listToInsert, count).execute(response.body())
                }
            }
        }

        override fun onFailure(callUpload: Call<ListBDD>, t: Throwable) {
        }
    })
}

class UpdatearOnlineList(val listToDelete: ListBDD, val count: Int): AsyncTask<ListBDD, Void, Void>() {
    override fun doInBackground(vararg params: ListBDD?): Void? {
        params[0]!!.isOnline = true
        allItemsOffline?.forEach {
            if (it.list_id == listToDelete.id){
                databaseLoader.deleteItemDeleted(ItemBddErased(it.id))
                databaseLoader.deleteItem(it)
                it.list_id = params[0]!!.id
            }
        }
        databaseLoader.deleteList(listToDelete)
        databaseLoader.insertList(params[0]!!)
        if (count == allListsOffline!!.size){
            updateAllOfflineItemsToApi()
        }
        return null
    }
}

// Updateamos Items offline
fun updateAllOfflineItemsToApi(){
    if (allItemsOffline != null && allItemsOffline?.size != 0){
        allItemsOffline?.forEach {
            val request = UserService.buildService(PersonApi::class.java)
            val call = request.updateItem(it.id.toInt(), it, API_KEY)
            call.enqueue(object : Callback<ItemBDD> {
                override fun onResponse(call: Call<ItemBDD>, response: Response<ItemBDD>)
                {
                    if (response.isSuccessful) {
                        UpdatearOnlineItems(null).execute(it)
                    }
                    else{
                        insertItemInApi(it)
                    }
                }
                override fun onFailure(call: Call<ItemBDD>, t: Throwable) {
                    println("NO FUNCIONA ${t.message}")
                    VERIFICADOR = true
                }
            })
        }
    }
    else{
        VERIFICADOR = true
    }
}

fun insertItemInApi(itemToInsert: ItemBDD){
    val request = UserService.buildService(PersonApi::class.java)
    val x = ListItems(listOf(itemToInsert))
    val p = itemToInsert
    val callUploadLists = request.postItem(x, API_KEY)
    callUploadLists.enqueue(object : Callback<List<ItemBDD>> {
            override fun onResponse(
                call: Call<List<ItemBDD>>,
                response: Response<List<ItemBDD>>
            ) {
            print(response)
            if (response.isSuccessful) {
                if (response.body() != null) {
                    itemToInsert.id = response.body()!![0].id
                    UpdatearOnlineItems(p).execute(itemToInsert)
                }
            }
        }
        override fun onFailure(callUpload: Call<List<ItemBDD>>, t: Throwable) {
        }
    })
}

class UpdatearOnlineItems(val item: ItemBDD?): AsyncTask<ItemBDD, Void, Void>() {
    override fun doInBackground(vararg params: ItemBDD?): Void? {
        params[0]!!.isOnline = true
        if (item != null) {
            databaseLoader.deleteItem(item)
        }
        databaseLoader.insertItem(params[0]!!)
        GetAllFromApi()
        return null
    }
}

class EraseFromErasedLists(): AsyncTask<Long, Void, Void>() {
    override fun doInBackground(vararg params: Long?): Void? {
        databaseLoader.deleteListDeleted(ListBddErased(params[0]!!))
        return null
    }
}

class EraseFromErasedItems(): AsyncTask<Long, Void, Void>() {
    override fun doInBackground(vararg params: Long?): Void? {
        databaseLoader.deleteItemDeleted(ItemBddErased(params[0]!!))
        return null
    }
}

fun GetAllFromApi(){
    val request = UserService.buildService(PersonApi::class.java)
    val call = request.getAllList(API_KEY)
    call.enqueue(object : Callback<List<ListBDD>> {
        override fun onResponse(
            call: Call<List<ListBDD>>,
            response: Response<List<ListBDD>>
        ) {
            if (response.isSuccessful) {
                if (response.body() != null) {
                    allListsFromApi = response.body()!!
                    loadItems(allListsFromApi!!)
                }
                else{
                    VERIFICADOR = true
                }
            }
        }

        override fun onFailure(call: Call<List<ListBDD>>, t: Throwable) {
            VERIFICADOR = true
        }
    })


    val callUser = request.getUsers(API_KEY)
    callUser.enqueue(object : Callback<User> {
        override fun onResponse(callUser: Call<User>, response: Response<User>) {
            if (response.isSuccessful) {
                if (response.body() != null) {
                    userFromApi = response.body()!!
                    loadUser()
                }
            }
        }

        override fun onFailure(callUser: Call<User>, t: Throwable) {
        }
    })

}

fun loadItems(allListsFromApi: List<ListBDD>){
    val request = UserService.buildService(PersonApi::class.java)
    var counter = 0
    allListsFromApi?.forEach {
        val callLists = request.getAllItem(it.id.toInt(), API_KEY)
        callLists.enqueue(object : Callback<List<ItemBDD>> {
            override fun onResponse(
                callLists: Call<List<ItemBDD>>,
                response: Response<List<ItemBDD>>
            ) {
                if (response.isSuccessful) {
                    if (response.body() != null) {
                        if (response.body()!!.isNotEmpty()) {
                            allListsWithItemsFromApi.add(ListWithItems(it, response.body()))
                        }
                        else{
                            allListsWithItemsFromApi.add(ListWithItems(it, null))

                        }
                        counter += 1
                        if (counter == allListsFromApi.size){
                            updateAll()
                        }
                    }
                }
            }
            override fun onFailure(callLists: Call<List<ItemBDD>>, t: Throwable) {
                println("NO FUNCIONA ${t.message}")
            }
        })
    }
    if (allListsFromApi.isEmpty()){
        updateAll()
    }
}

fun loadUser(){
    if (userFromDB == null){
        if (userFromApi != null){
            LoadUserToDb().execute()
        }
    }
    VERIFICADOR = true
}

class LoadUserToDb(): AsyncTask<Void, Void, Void>() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun doInBackground(vararg params: Void?): Void? {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        val formatted = current.format(formatter)
        databaseLoader.insertUser(UserBBDD(1,userFromApi!!.first_name, userFromApi!!.last_name, userFromApi!!.email, userFromApi!!.phone, userFromApi!!.profile_photo,formatted,true))
        return null
    }
}

fun updateAll(){
    // CASO 2: BDD vacia y API llena
    if (allListsWithItemsFromDB == null || allListsWithItemsFromDB?.size == 0){
        if (allListsWithItemsFromApi.isNotEmpty()){
            UploadListsAndItemsToBdd().execute()
        }
        else{
            VERIFICADOR = true
        }
    }
    // ACA SE TOMAN EL CASO 1 y 3:
    // 1) BDD con cosas y API sin cosas
    // 3) BDD con cosas y API con cosas
    else{
        // Caso 3
        if (allListsWithItemsFromApi.isNotEmpty()){
            compareListsAndItems()
        }
        // Caso 1
        else{
            uploadListsToApi()
        }
    }
}

class UploadListsAndItemsToBdd(): AsyncTask<Void, Void, Void>() {
    override fun doInBackground(vararg params: Void?): Void? {
        allListsWithItemsFromApi.forEach {
            it.list.isOnline = true
            databaseLoader.insertList(it.list)
            if (it.items != null) {
                it.items.forEach {
                    it.isOnline = true
                    databaseLoader.insertItem(it)
                }
            }
        }
        VERIFICADOR = true
        return null
    }
}

fun uploadListsToApi(){
    val request = UserService.buildService(PersonApi::class.java)
    var todasShared = true
    allListsWithItemsFromDB?.forEach {
        if (!it.list.isSharedList) {
            val callUpload = request.postList(it.list, API_KEY)
            callUpload.enqueue(object : Callback<ListBDD> {
                override fun onResponse(
                    callUpload: Call<ListBDD>,
                    response: Response<ListBDD>
                ) {
                    print(response)
                    if (response.isSuccessful) {
                        if (response.body() != null) {
                            if (it.items != null) {
                                it.items.forEach {
                                    it.list_id = response.body()!!.id
                                }
                                uploadItemsToApi()
                            }
                            todasShared = todasShared && false
                            it.list.id = response.body()!!.id
                        }
                    }
                }

                override fun onFailure(callUpload: Call<ListBDD>, t: Throwable) {
                }
            })
        }
    }
    if(todasShared){
        VERIFICADOR = true
    }
}

fun uploadItemsToApi(){
    val request = UserService.buildService(PersonApi::class.java)
    allListsWithItemsFromDB?.forEach {
        if (it.items != null){
            val items = ListItems(it.items)
            val call = request.postItem(items, API_KEY)
            call.enqueue(object : Callback<List<ItemBDD>> {
                override fun onResponse(
                    call: Call<List<ItemBDD>>,
                    response: Response<List<ItemBDD>>
                ) {
                    print(response)
                    if (response.isSuccessful) {
                        if (response.body() != null) {
                            EraseAndUploadAllFromDB().execute()
                        }
                    }
                }
                override fun onFailure(call: Call<List<ItemBDD>>, t: Throwable) {
                }
            })
        }
    }
    VERIFICADOR = true
}

class EraseAndUploadAllFromDB(): AsyncTask<Void, Void, Void>() {
    override fun doInBackground(vararg params: Void?): Void? {
        copyAllListsWithItemsFromDB?.forEach {
            databaseLoader.deleteListItems(it.list.id)
            databaseLoader.deleteList(it.list)
        }
        allListsWithItemsFromDB?.forEach {
            it.list.isOnline = true
            databaseLoader.insertList(it.list)
            if (it.items != null) {
                it.items.forEach {
                    it.isOnline = true
                    databaseLoader.insertItem(it)
                }
            }
        }
        return null
    }
}

fun compareListsAndItems(){
    allListsWithItemsFromApi.forEach {
        InsertListToBdd(it.items).execute(it.list)
    }
    VERIFICADOR = true
}

class InsertListToBdd(val items: List<ItemBDD>?): AsyncTask<ListBDD, Void, Void>(){
    override fun doInBackground(vararg params: ListBDD): Void? {
        params[0].isOnline = true
        databaseLoader.insertList(params[0])
        items?.forEach {
            it.isOnline = true
            databaseLoader.insertItem(it)
        }
        return null
    }
}
