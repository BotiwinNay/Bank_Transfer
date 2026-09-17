package com.example.banktransfer

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class BankingActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var balance = 1250.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_banking)

        auth = FirebaseAuth.getInstance()

        val textBalance = findViewById<TextView>(R.id.textBalance)
        val editRecipient = findViewById<EditText>(R.id.editRecipientAccount)
        val editAmount = findViewById<EditText>(R.id.editAmount)
        val btnTransfer = findViewById<Button>(R.id.btnTransfer)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        textBalance.text = "$ %.2f".format(balance)

        btnTransfer.setOnClickListener {
            val recipient = editRecipient.text.toString()
            val amountStr = editAmount.text.toString()

            if (recipient.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(this, "Please enter recipient and amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = amountStr.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (amount > balance) {
                Toast.makeText(this, "Insufficient funds", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Perform transfer (mock)
            balance -= amount
            textBalance.text = "$ %.2f".format(balance)
            editRecipient.text.clear()
            editAmount.text.clear()

            Toast.makeText(this, "Transfer of $ %.2f to $recipient successful".format(amount), Toast.LENGTH_LONG).show()
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}