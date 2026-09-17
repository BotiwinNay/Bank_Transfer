package com.example.banktransfer

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import android.util.Log
import com.google.firebase.auth.FirebaseUser

class RegisterActivity : AppCompatActivity() {

    lateinit var txtEmailCreate: EditText
    lateinit var txtPasswordCreate: EditText
    lateinit var buttonSubmit: Button
    lateinit var email: String
    lateinit var password: String

    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        txtEmailCreate = findViewById<EditText>(R.id.txtEmailCreate)
        txtPasswordCreate = findViewById<EditText>(R.id.txtPasswordCreate)
        buttonSubmit = findViewById<Button>(R.id.buttonSubmit)

        mAuth = FirebaseAuth.getInstance()
        buttonSubmit!!.setOnClickListener {
            createAccount()
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        if(user != null){
            return
        }
    }

    private fun createAccount() {
        email = txtEmailCreate!!.text.toString()
        password = txtPasswordCreate!!.text.toString()
        mAuth!!.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this){
            task -> if (task.isSuccessful){
                Log.d("createAccount", "Create Account Successful")
                val user = mAuth!!.currentUser
            updateUI(user)
            }
        }
    }
}