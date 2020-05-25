package com.example.entrega1proyecto.model

import java.io.Serializable

// Here we set the User we want to get from the URL given
data class User(var email:String, var name:String, var last_name:String, var phone:String, var profile_photo:String): Serializable