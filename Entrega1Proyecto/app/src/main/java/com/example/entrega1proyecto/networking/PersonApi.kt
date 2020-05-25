package com.example.entrega1proyecto.networking

import com.example.entrega1proyecto.model.User
import retrofit2.Call
import retrofit2.http.GET

interface PersonApi {
    @GET("get_user_info")
    fun getUsers(): Call<User>
}