package com.introid.instagram_firestore

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private val TAG : String = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null){
            goPostActivity()
        }

        btnLogin.setOnClickListener {
            btnLogin.isEnabled = false
            val email= etEmail.text.toString()
            val password = etPassword.text.toString()
            if (email.isBlank() || password.isBlank()){
                Toast.makeText(this, "Email / Password Cannot be blank" , Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            // firebase Authentication

            auth.signInWithEmailAndPassword(email , password).addOnCompleteListener{task ->
                btnLogin.isEnabled = true
                if (task.isSuccessful){
                    Toast.makeText(this, "Success!" , Toast.LENGTH_SHORT).show()
                    goPostActivity()
                }else {
                    Log.i(TAG , "sign in with email failed" , task.exception)
                    Toast.makeText(this, "Login Failed : ${task.exception}" , Toast.LENGTH_SHORT).show()

                }
            }
        }
    }

    private fun goPostActivity() {
        Log.i(TAG , "goPostActivity")
        val intent = Intent(this, PostsActivity::class.java)
        startActivity(intent)
        finish()


    }
}