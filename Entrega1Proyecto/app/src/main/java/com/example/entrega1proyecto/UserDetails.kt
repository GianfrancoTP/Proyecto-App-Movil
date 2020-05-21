package com.example.entrega1proyecto

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.entrega1proyecto.model.User
import kotlinx.android.synthetic.main.activity_user_details.*
import kotlinx.android.synthetic.main.edit_user_popup.view.*
import java.io.Serializable

class UserDetails : AppCompatActivity() {

    var listaList: ArrayList<ListaItem> = ArrayList()
    var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_details)

        listaList = intent.getSerializableExtra("lista de listas")!! as ArrayList<ListaItem>
        user = intent.getSerializableExtra("user details")!! as User

        user_name_text_view.text = user!!.name + " " +user!!.last_name
        user_email_text_view.text = user!!.email
        Glide.with(this).load(user!!.profile_photo).apply(RequestOptions.circleCropTransform())
            .into(banner_picture_user_image_view)
    }

    fun seeMyProfile(view: View) {
        // We created a Dialog to ask what things he wants to change
        var builder: AlertDialog.Builder = AlertDialog.Builder(this)
        var inflater: LayoutInflater = layoutInflater
        var view: View = inflater.inflate(R.layout.edit_user_popup,null)

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
            }
        })

        // To go back to the Log in activity
        builder.setPositiveButton("Confirmar",object:  DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                user!!.email = view.emailTextView.text.toString()
                var fullname = verificador(view.name_text_view)
                if (fullname.size == 1){
                    user!!.name = fullname[0]
                }
                else{
                    user!!.name = fullname[0]+" "+fullname[1]
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
                user_name_text_view.text = user!!.name + " " +user!!.last_name
                user_email_text_view.text = user!!.email
                dialog?.dismiss()
            }
        })
        var dialog: Dialog = builder.create()
        dialog.show()
    }

    fun setData(view: View){
        view.emailTextView.text = user!!.email
        view.name_text_view.setText(user!!.name)
        view.last_name_text_view.setText(user!!.last_name)
        view.phone_text_view.setText(user!!.phone)
    }

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

    fun enableEditTextInput(editText: EditText){
        editText.inputType = InputType.TYPE_TEXT_VARIATION_PERSON_NAME
        editText.hint = editText.text.toString()
        editText.setText("")
    }

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

    fun logOutPopUp(view: View) {
        // We created a Dialog to ask if he really wants to log out
        var builder: AlertDialog.Builder = AlertDialog.Builder(this)
        var inflater: LayoutInflater = layoutInflater
        var view: View = inflater.inflate(R.layout.log_out_pop_up,null)
        builder.setView(view)

        // To resume what was going on in the app if he does't want to log out
        builder.setNegativeButton("Cancelar", object: DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                dialog?.dismiss()
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
                finish()
            }
        })
        var dialog: Dialog = builder.create()
        dialog.show()
    }

    override fun onBackPressed() {
        val goBackIntent = Intent()
        goBackIntent.putExtra("user details update", user as Serializable)
        setResult(3, goBackIntent)
        super.onBackPressed()
    }



}
