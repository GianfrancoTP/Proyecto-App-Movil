package com.example.entrega1proyecto

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.entrega1proyecto.ListaActivity.Companion.LISTS
import com.example.entrega1proyecto.model.User
import kotlinx.android.synthetic.main.fragment_log.*
import java.io.Serializable

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LogFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    var listaDeListas: ArrayList<ListaItem>? = null
    var user: User? = null
    var switch: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    fun goToList(){
        val intent = Intent(activity, ListaActivity::class.java)
        if (user!= null) {
            intent.putExtra("email", user as Serializable)
        }
        intent.putExtra("lista",listaDeListas)
        intent.putExtra("coming from Log In", true)
        intent.putExtra("switchFromStart", switch)
        startActivityForResult(intent, 2)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data != null) {
            if (resultCode == Activity.RESULT_OK){
                listaDeListas = data.getSerializableExtra("lista de listas") as ArrayList<ListaItem>
                user = data.getSerializableExtra("user details finish") as User
                switch = data.getBooleanExtra("switchStateToStart",false)
                emailTextView.setText(user!!.email)
                passwordTextView.setText(user!!.name)
            }
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_log, container, false)
        val button = rootView.findViewById<Button>(R.id.IngresarButton)
        button.setOnClickListener { goToList() }
        return rootView
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment LogFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


}
