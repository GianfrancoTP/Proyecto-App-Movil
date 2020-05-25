package com.example.entrega1proyecto

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.entrega1proyecto.ListaActivity.Companion.LISTS
import com.example.entrega1proyecto.model.User
import com.example.entrega1proyecto.networking.PersonApi
import com.example.entrega1proyecto.networking.UserService
import kotlinx.android.synthetic.main.fragment_log.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.Serializable

class LogFragment : Fragment() {
    var listaDeListas: ArrayList<ListaItem>? = null
    var user: User? = null

    fun goToList(){
        val intent = Intent(activity, ListaActivity::class.java)

        intent.putExtra("lista",listaDeListas)
        intent.putExtra("coming from Log In", true)
        intent.putExtra("user details start",user as Serializable)
        startActivityForResult(intent, 2)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data != null) {
            if (resultCode == Activity.RESULT_OK){
                listaDeListas = data.getSerializableExtra("lista de listas") as ArrayList<ListaItem>
                user = data.getSerializableExtra("user details finish") as User
                emailTextView.setText(user!!.email)
                passwordTextView.setText(user!!.name)
            }
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_log, container, false)
        val button = rootView.findViewById<Button>(R.id.IngresarButton)

        try{
            user = savedInstanceState?.getSerializable("user details update") as User
            emailTextView.setText(user!!.email)
            passwordTextView.setText(user!!.name)
        }catch (e: Exception){
            val request = UserService.buildService(PersonApi::class.java)
            val call = request.getUsers()
            call.enqueue(object: Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if (response.isSuccessful) {
                        if (response.body() != null) {
                            user = response.body()!!
                            emailTextView.setText(user!!.email)
                            passwordTextView.setText(user!!.name)
                        }
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    Toast.makeText(activity!!.applicationContext, "${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        button.setOnClickListener { goToList() }
        return rootView
    }

}
