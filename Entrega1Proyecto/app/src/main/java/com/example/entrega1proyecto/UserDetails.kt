package com.example.entrega1proyecto

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.entrega1proyecto.configuration.API_KEY
import com.example.entrega1proyecto.model.Database
import com.example.entrega1proyecto.model.ListDao
import com.example.entrega1proyecto.model.User
import com.example.entrega1proyecto.model.UserBBDD
import com.example.entrega1proyecto.networking.PersonApi
import com.example.entrega1proyecto.networking.UserService
import com.example.entrega1proyecto.networking.isOnline
import kotlinx.android.synthetic.main.activity_user_details.*
import kotlinx.android.synthetic.main.edit_user_popup.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.Serializable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class UserDetails : AppCompatActivity() {

    var user: User? = null
    var logOutdialog: Dialog? = null
    var dialog: Dialog? = null
    var isShowingDialogExit = false
    var isShowingDialogProfile = false
    var online = false
    var onlinep = false
    var onlinef = false
    lateinit var database: ListDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_details)

        // Here we create the db
        database = Room.databaseBuilder(this, Database::class.java, "ListsBDD").build().ListDao()

        user = intent.getSerializableExtra("user details")!! as User

        user_name_text_view.text = user!!.first_name + " " +user!!.last_name
        user_email_text_view.text = user!!.email
        Glide.with(this).load(user!!.profile_photo).apply(RequestOptions.circleCropTransform())
            .into(banner_picture_user_image_view)
        if(savedInstanceState != null){
            isShowingDialogExit = savedInstanceState.getBoolean("IS_SHOWING_DIALOG_EXIT", false)
            onlinef = savedInstanceState!!.getBoolean("online1",false)
            if(isShowingDialogExit) {
                logOutPopUp(View(this))
            }
            isShowingDialogProfile = savedInstanceState.getBoolean("IS_SHOWING_DIALOG_PROFILE", false)
            if(isShowingDialogProfile) {
                seeMyProfile(View(this))
            }
        }

        onlinep = intent.getBooleanExtra("online", false)

        online = onlinep || isOnline(this) || onlinef

        if(isOnline(this) && !onlinep && !onlinef){
            online = true
        }
    }

    fun seeMyProfile(view: View) {
        // We created a Dialog to ask what things he wants to change
        var builder: AlertDialog.Builder = AlertDialog.Builder(this)
        var inflater: LayoutInflater = layoutInflater
        var view: View = inflater.inflate(R.layout.edit_user_popup,null)
        builder.setCancelable(false)

        // Here we disable the input type to don't edit nothing without clicking the icon to edit
        disableEditTextInput(view.name_text_view)
        disableEditTextInput(view.last_name_text_view)
        disableEditTextInput(view.phone_text_view)

        setData(view)

        Glide.with(this).load(user!!.profile_photo).apply(RequestOptions.circleCropTransform())
            .into(view.profile_photo)
        view.edit_name.setOnClickListener{
            if (view.name_text_view.inputType == InputType.TYPE_NULL) {
                enableEditTextInput(view.name_text_view)
            }
            else {
                disableEditTextInput(view.name_text_view)
            }

        }
        view.edit_last_name.setOnClickListener{
            if (view.last_name_text_view.inputType == InputType.TYPE_NULL) {
                enableEditTextInput(view.last_name_text_view)
            }
            else {
                disableEditTextInput(view.last_name_text_view)
            }

        }

        view.edit_phone.setOnClickListener {
            if (view.phone_text_view.inputType == InputType.TYPE_NULL) {
                enableEditTextInput(view.phone_text_view)
            }
            else {
                disableEditTextInput(view.phone_text_view)
            }
        }
        builder.setView(view)

        // To resume what was going on in the app if he does't want to log out
        builder.setNegativeButton("Cancelar", object: DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                dialog?.dismiss()
                isShowingDialogProfile = false
            }
        })

        // To go back to the Log in activity
        builder.setPositiveButton("Confirmar",object:  DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                user!!.email = view.emailTextView.text.toString()
                var fullname = verificador(view.name_text_view)
                if (fullname.size == 1){
                    user!!.first_name = fullname[0]
                }
                else{
                    user!!.first_name = fullname[0]+" "+fullname[1]
                }
                fullname = verificador(view.last_name_text_view)
                if (fullname.size == 1){
                    user!!.last_name = fullname[0]
                }
                else{
                    user!!.last_name = fullname[0]+" "+fullname[1]
                }

                if(view.phone_text_view.text.toString() == "") {
                    user!!.phone = view.phone_text_view.hint.toString()
                }
                else{
                    user!!.phone = view.phone_text_view.text.toString()
                }
                user_name_text_view.text = user!!.first_name + " " +user!!.last_name
                user_email_text_view.text = user!!.email

                UpdateUser(this@UserDetails).execute(user)

                dialog?.dismiss()
                isShowingDialogProfile = false
            }
        })
        dialog = builder.create()
        dialog!!.show()
        isShowingDialogProfile = true
    }

    // Set the data modified to the user
    fun setData(view: View){
        view.emailTextView.text = user!!.email
        view.name_text_view.setText(user!!.first_name)
        view.last_name_text_view.setText(user!!.last_name)
        view.phone_text_view.setText(user!!.phone)
    }

    // To not be able to modify the attributes
    fun disableEditTextInput(editText: EditText){
        if (editText.text.toString() == "") {
            editText.setText(editText.hint.toString())
            editText.hint = ""
        }
        editText.inputType = InputType.TYPE_NULL
        editText.setTextIsSelectable(false)
        editText.setOnKeyListener { v, keyCode, event ->
            true // Blocks input from hardware keyboards.
        }
    }

    // To be able to modify the attributes
    fun enableEditTextInput(editText: EditText){
        editText.inputType = InputType.TYPE_TEXT_VARIATION_PERSON_NAME
        editText.hint = editText.text.toString()
        editText.setText("")
    }

    // To set it in text or hint the attributes of the user
    // Hint is to show that you are able to modify that parameter
    // and text is to show that you cant modify it
    fun verificador(editText: EditText): List<String>{
        var fullName: List<String>
        if(editText.text.toString() == ""){
            fullName = editText.hint.toString()
                .split(" ")
        }
        else{
            fullName = editText.text.toString()
                .split(" ")

        }
        return fullName
    }

    // When we want to log out
    fun logOutPopUp(view: View) {
        // We created a Dialog to ask if he really wants to log out
        var builder: AlertDialog.Builder = AlertDialog.Builder(this)
        var inflater: LayoutInflater = layoutInflater
        var view: View = inflater.inflate(R.layout.log_out_pop_up,null)
        builder.setCancelable(false)
        builder.setView(view)

        // To resume what was going on in the app if he does't want to log out
        builder.setNegativeButton("Cancelar", object: DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                dialog?.dismiss()
                isShowingDialogExit = false
            }
        })

        // To go back to the Log in activity
        builder.setPositiveButton("Confirmar",object:  DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                val myIntent = Intent()
                // We give the result to the Log in activity to maintain the information
                myIntent.putExtra("user details updated",user as Serializable)
                setResult(2, myIntent)
                dialog?.dismiss()
                isShowingDialogExit = false
                finish()
            }
        })
        logOutdialog = builder.create()
        logOutdialog!!.show()
        isShowingDialogExit = true
    }

    // To go back to the lists activity
    override fun onBackPressed() {
        val goBackIntent = Intent()
        goBackIntent.putExtra("user details update", user as Serializable)
        goBackIntent.putExtra("online", online)
        setResult(3, goBackIntent)
        super.onBackPressed()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("online1", online)
        outState.putBoolean("IS_SHOWING_DIALOG_EXIT", isShowingDialogExit)
        outState.putBoolean("IS_SHOWING_DIALOG_PROFILE", isShowingDialogProfile)
        super.onSaveInstanceState(outState)
    }

    // To maintain the dialogs states when the app changes of state
    override fun onPause() {
        if(logOutdialog!=null && logOutdialog!!.isShowing) {
            logOutdialog!!.dismiss()
        }
        if(dialog!=null && dialog!!.isShowing) {
            dialog!!.dismiss()
        }
        super.onPause()
    }

    class UpdateUser(private val listaActivity: UserDetails): AsyncTask<User?,Void,Void>(){
        override fun doInBackground(vararg params: User?): Void? {
            val request = UserService.buildService(PersonApi::class.java)
            val call = request.updateUser(params[0]!!, API_KEY)
            call.enqueue(object : Callback<User> {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(
                    call: Call<User>,
                    response: Response<User>
                ) {
                    if (response.isSuccessful) {
                        if (response.body() != null) {
                            val current = LocalDateTime.now()
                            val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                            val formatted = current.format(formatter)
                            val userUpdated = UserBBDD(1,response.body()!!.first_name,
                                response.body()!!.last_name, response.body()!!.email,
                                response.body()!!.phone, response.body()!!.profile_photo,
                                formatted, true)
                            UpdateUserBDD(listaActivity).execute(userUpdated)
                        }
                    }
                }
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onFailure(call: Call<User>, t: Throwable) {
                    println("NO FUNCIONA ${t.message}")
                    val current = LocalDateTime.now()
                    val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                    val formatted = current.format(formatter)
                    val userUpdated = UserBBDD(1,params[0]!!.first_name,
                        params[0]!!.last_name, params[0]!!.email,
                        params[0]!!.phone, params[0]!!.profile_photo,
                        formatted, false)
                    UpdateUserBDD(listaActivity).execute(userUpdated)
                }
            })
            return null
        }
    }

    class UpdateUserBDD(private val listaActivity: UserDetails): AsyncTask<UserBBDD?,Void,Void>() {
        override fun doInBackground(vararg params: UserBBDD?): Void? {
            listaActivity.database.updateUser(params[0]!!)
            return null
        }
    }
}
