package com.example.entrega1proyecto.networking

import com.example.entrega1proyecto.model.User
import retrofit2.Call
import retrofit2.http.*

interface PersonApi {
    // Bet user Information
    @GET("users/get_self")
    fun getUsers(@Header("token") api_key: String?): Call<User>

    //Update user Information
    @PUT("users/update_self")
    fun updateUser(@Body BODY: User,
                   @Header("token") api_key: String?
    ) :Call<User>

    /*
    //Create List
    @POST("lists/")
    fun postList(@Body list: Lists,
                 @Header("token")  api_key: String?
    ): Call<Lists>

    //Update list by ID
    @PUT("lists/{list_id}")
    fun updateList(@Path("list_id") id: Int,
                   @Body list: Lists,
                   @Header("token")  api_key: String?
    ): Call<Lists>

    //Get list information by ID
    @GET("lists/{list_id}")
    fun getList(@Path("list_id") id: Int,
                @Header("token")  api_key: String?
    ): Call<Lists>

    //Get all list information
    @GET("lists/")
    fun getAllList(@Header("token")  api_key: String?): Call<List<Lists>>

    //Delete list bi ID
    @DELETE("lists/{list_id}")
    fun deleteList(@Path("list_id") id: Int,
                   @Header("token")  api_key: String?
    ): Call<Lists>

    //Create item
    @POST("items")
    fun postItem(@Body item: ListItems,
                 @Header("token")  api_key: String?
    ): Call<List<Items>>

    //Update item by ID
    @PUT("items/{id}")
    fun updateItem(@Path("id") id: Int,
                   @Body item: Items,
                   @Header("token")  api_key: String?
    ): Call<Items>

    //Get item information by ID
    @GET("items/{id}")
    fun getItem(@Path("id") id: Int,
                @Header("token")  api_key: String?
    ): Call<Items>

    //Get all items information
    @GET("items/")
    fun getAllItem(@Query("list_id") id: Int,
                   @Header("token")  api_key: String?
    ): Call<List<Items>>

    //Delete item bi ID
    @DELETE("items/{item_id}")
    fun deleteItem(@Path("item_id") id: Int,
                   @Header("token")  api_key: String?
    ): Call<Items>
     */
}