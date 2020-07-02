package com.example.entrega1proyecto

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.example.entrega1proyecto.configuration.API_KEY
import com.example.entrega1proyecto.model.Database
import com.example.entrega1proyecto.model.ListDao
import com.example.entrega1proyecto.model.User
import com.example.entrega1proyecto.model.UserBBDD
import com.example.entrega1proyecto.networking.PersonApi
import com.example.entrega1proyecto.networking.UserService
import com.example.entrega1proyecto.networking.isOnline
import com.example.entrega1proyecto.networking.loaders.setDB
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.Serializable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class MainActivity : AppCompatActivity() {
    var user: User? = null

    // Db
    lateinit var database: ListDao
    var online = false
    var onlinef = false
    var listo = false
    // FIREBASE
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            user = savedInstanceState?.getSerializable("user details update") as User
        } catch (e: Exception) {
            AsyncRunnable(this)
        }

        if(isOnline(this)) {
            setDB(this)
        }
        else{
            VERIFICADOR = true
        }

        auth = FirebaseAuth.getInstance()
        database = Room.databaseBuilder(this, Database::class.java, "ListsBDD")
            .build().ListDao()

        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setLogo(R.drawable.logo_50dp)
                .setTheme(R.style.ThemeOverlay_AppCompat_DayNight)
                .setAlwaysShowSignInMethodScreen(true)
                .setIsSmartLockEnabled(false)
                .build(),
            RC_SIGN_IN
        )
    }

    // [START onactivityresult]
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = FirebaseAuth.getInstance().currentUser
                Toast.makeText(this,"Welcome ${account!!.displayName}",Toast.LENGTH_SHORT).show()
                goToList()
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                println(e)
            }
        }

        else if (resultCode == Activity.RESULT_OK) {
            AuthUI.getInstance().signOut(this)
            user = data?.getSerializableExtra("user details finish") as User
            onlinef = data.getBooleanExtra("online", false)
            val providers = arrayListOf(
                AuthUI.IdpConfig.GoogleBuilder().build()
            )
            startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .setLogo(R.drawable.logo_50dp)
                    .setTheme(R.style.ThemeOverlay_AppCompat_DayNight)
                    .setAlwaysShowSignInMethodScreen(true)
                    .setIsSmartLockEnabled(false)
                    .build(),
                RC_SIGN_IN
            )
        }
    }
    // [END onactivityresult]

    fun AsyncRunnable(listaActivity: MainActivity) {
        Thread(Runnable {
            while(!VERIFICADOR){}
            ObtainUserFromDB(listaActivity).execute()
            VERIFICADOR = false
            listo = true
        }).start()
    }

    class ObtainUserFromDB(private val listaActivity: MainActivity) : AsyncTask<Void, Void, User?>() {
        override fun doInBackground(vararg params: Void?): User? {
            var userBd = listaActivity.database.getUser()
            if (userBd != null){
                listaActivity.user = User(
                    userBd.email,
                    userBd.first_name,
                    userBd.last_name,
                    userBd.phone,
                    userBd.profile_photo
                )
                return listaActivity.user
            }
            return null
        }
    }

    fun goToList() {
        Thread(Runnable {
            while(!listo){}
        }).start()
        if (user != null) {
            val intent = Intent(this, ListaActivity::class.java)
            intent.putExtra("coming from Log In", true)
            intent.putExtra("user details start", user as Serializable)
            intent.putExtra("online", online)
            startActivityForResult(intent, 2)
        } else {
            Toast.makeText(this, "Debe tener internet para hacer Log In!", Toast.LENGTH_LONG)
                .show()
        }
    }

    companion object {
        var LOGGED = "LOGGED"
        var VERIFICADOR = false
        private const val RC_SIGN_IN = 123
    }

}
