package com.example.banktransfer

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import android.widget.Button
import android.widget.Toast
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
        if (user != null) {
            Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
            finish() // Returns to the previous screen (Login)
        }
    }

    private fun createAccount() {
        email = txtEmailCreate!!.text.toString()
        password = txtPasswordCreate!!.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            return
        }

        mAuth!!.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Log.d("createAccount", "Create Account Successful")
                val user = mAuth!!.currentUser
                updateUI(user)
            } else {
                Log.w("createAccount", "Create Account Failed", task.exception)
                Toast.makeText(this, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}