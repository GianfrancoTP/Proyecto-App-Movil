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
    // FIREBASE
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(isOnline(this)) {
            setDB(this)
        }
        else{
            VERIFICADOR = true
        }
        try {
            user = savedInstanceState?.getSerializable("user details update") as User
        } catch (e: Exception) {
            AsyncRunnable(this)
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
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = FirebaseAuth.getInstance().currentUser
                Toast.makeText(this,"Welcome ${account!!.displayName}",Toast.LENGTH_SHORT).show()
                /*if (account!!.phoneNumber == null){
                    if (account.photoUrl == null){
                        user = User(account.email!!, account.displayName!!,"","1111111111", "@assets/account_icon_50dp")
                    }
                    else{
                        user = User(account.email!!, account.displayName!!,"","1111111111", account.photoUrl.toString())
                    }
                }
                else{
                    if (account.photoUrl == null){
                        user = User(
                            account.email!!, account.displayName!!,"",
                            account.phoneNumber!!, "@assets/account_icon_50dp")
                    }
                    else{
                        user = User(
                            account.email!!, account.displayName!!,"",
                            account.phoneNumber!!, account.photoUrl.toString())
                    }
                }
                updateUser(user!!, this)*/
                goToList()
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                println(e)
            }
        }

        else if (resultCode == Activity.RESULT_OK) {
            user = data?.getSerializableExtra("user details finish") as User
            onlinef = data.getBooleanExtra("online", false)
            FirebaseAuth.getInstance().signOut()
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

    /*fun updateUser(user: User, frag: MainActivity){
        val request = UserService.buildService(PersonApi::class.java)
        val call = request.updateUser(user, API_KEY)
        call.enqueue(object : Callback<User> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<User>,
                response: Response<User>
            ) {
                if (response.isSuccessful) {
                    if(response.body() != null) {
                        val current = LocalDateTime.now()
                        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                        val formatted = current.format(formatter)
                        var userp = response.body()
                        val us = UserBBDD(
                            1,
                            userp!!.first_name,
                            userp.last_name,
                            userp.email,
                            userp.phone,
                            userp.profile_photo,
                            formatted,
                            true

                        )
                        UpdateUserBdd(frag).execute(us)
                    }
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                TODO("Not yet implemented")
            }
        })
    }

    class UpdateUserBdd(private val listaActivity: MainActivity) : AsyncTask<UserBBDD, Void, Void?>() {
        override fun doInBackground(vararg params: UserBBDD?): Void? {
            listaActivity.database.updateUser(params[0]!!)
            return null
        }

        override fun onPostExecute(result: Void?) {
            listaActivity.goToList()
        }
    }*/

    companion object {
        var LOGGED = "LOGGED"
        var VERIFICADOR = false
        private const val RC_SIGN_IN = 123
    }

}
