package com.example.banktransfer

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.firestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var txtEmailCreate: EditText
    private lateinit var txtPasswordCreate: EditText
    private lateinit var buttonSubmit: Button

    private var mAuth: FirebaseAuth? = null
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        
        txtEmailCreate = findViewById(R.id.txtEmailCreate)
        txtPasswordCreate = findViewById(R.id.txtPasswordCreate)
        buttonSubmit = findViewById(R.id.buttonSubmit)

        mAuth = FirebaseAuth.getInstance()
        buttonSubmit.setOnClickListener {
            createAccount()
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            // Initialize user data in Firestore
            val userData = hashMapOf(
                "email" to user.email,
                "balance" to 1000.0 // Starting balance for new users
            )

            db.collection("users").document(user.uid)
                .set(userData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Registration Successful with $1000 bonus!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Log.w("Register", "Error adding document", e)
                    Toast.makeText(this, "User created, but failed to initialize balance.", Toast.LENGTH_LONG).show()
                    finish()
                }
        }
    }

    private fun createAccount() {
        val email = txtEmailCreate.text.toString()
        val password = txtPasswordCreate.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            return
        }

        mAuth?.createUserWithEmailAndPassword(email, password)?.addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Log.d("createAccount", "Create Account Successful")
                val user = mAuth?.currentUser
                updateUI(user)
            } else {
                Log.w("createAccount", "Create Account Failed", task.exception)
                Toast.makeText(this, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}