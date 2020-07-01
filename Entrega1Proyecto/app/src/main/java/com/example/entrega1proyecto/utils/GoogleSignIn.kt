package com.example.entrega1proyecto.utils

import android.content.Context
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider


interface FirebaseCallback {
    fun onGoogleLogin(user: FirebaseUser?)
    fun onLogout()
}

class GoogleSignIn(context: Context, listener: FirebaseCallback) {

    private val listener: FirebaseCallback
    private val context: Context
    private var firebaseAuth: FirebaseAuth

    init {
        firebaseAuth = FirebaseAuth.getInstance()
        this.context = context
        this.listener = listener
    }


    fun sigInWithGoogle(account: GoogleSignInAccount?) {
        if (account != null) {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            firebaseAuth.signInWithCredential(credential).addOnCompleteListener {
                if (it.isSuccessful) {
                    this@GoogleSignIn.listener.onGoogleLogin(firebaseAuth.currentUser)
                } else {
                    Toast.makeText(
                        this.context, "Authentication with Google failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }


    fun logout() {
        firebaseAuth.signOut()
        this@GoogleSignIn.listener.onLogout()
    }
}