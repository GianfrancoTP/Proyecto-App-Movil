package com.example.entrega1proyecto

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.example.entrega1proyecto.ListaActivity.Companion.LISTS
import com.example.entrega1proyecto.model.User
import com.google.gson.annotations.SerializedName
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_user_details.*
import java.io.Serializable

class UserDetails : AppCompatActivity() {

    var listaList: ArrayList<ListaItem> = ArrayList()
    var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_details)

        listaList = intent.getSerializableExtra("lista de listas")!! as ArrayList<ListaItem>
        user = intent.getSerializableExtra("user details")!! as User

        user_name_text_view.text = user!!.name + user!!.last_name
        user_email_text_view.text = user!!.email
        Picasso.get().load(user!!.profile_photo).into(banner_picture_user_image_view);
    }

    fun seeMyProfile(view: View) {

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
}
