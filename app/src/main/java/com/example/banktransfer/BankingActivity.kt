package com.example.banktransfer

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.Timestamp

class BankingActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var currentBalance = 0.0

    private val thaiBanks = arrayOf(
        "Kasikorn Bank (KBank)",
        "Siam Commercial Bank (SCB)",
        "Bangkok Bank (BBL)",
        "Krungthai Bank (KTB)",
        "Bank of Ayudhya (Krungsri)",
        "TMBThanachart Bank (ttb)",
        "Government Savings Bank (GSB)",
        "CIMB Thai Bank"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_banking)

        auth = FirebaseAuth.getInstance()
        db = Firebase.firestore
        val currentUser = auth.currentUser

        if (currentUser == null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        val textWelcome = findViewById<TextView>(R.id.textWelcome)
        val textBalance = findViewById<TextView>(R.id.textBalance)
        val spinnerBanks = findViewById<Spinner>(R.id.spinnerBanks)
        val editRecipient = findViewById<EditText>(R.id.editRecipientAccount)
        val editAmount = findViewById<EditText>(R.id.editAmount)
        val btnTransfer = findViewById<Button>(R.id.btnTransfer)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        // Setup Spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, thaiBanks)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerBanks.adapter = adapter

        textWelcome.text = "Welcome, ${currentUser.email}"

        // Listen for balance updates
        val userRef = db.collection("users").document(currentUser.uid)
        userRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Toast.makeText(this, "Error loading balance: ${e.message}", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                currentBalance = snapshot.getDouble("balance") ?: 0.0
                textBalance.text = "$ %.2f".format(currentBalance)
            }
        }

        btnTransfer.setOnClickListener {
            val selectedBank = spinnerBanks.selectedItem.toString()
            val recipientEmail = editRecipient.text.toString().trim()
            val amountStr = editAmount.text.toString().trim()

            if (recipientEmail.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(this, "Please enter recipient email and amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = amountStr.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (amount > currentBalance) {
                Toast.makeText(this, "Insufficient funds", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            performTransfer(recipientEmail, amount, selectedBank)
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun performTransfer(recipientEmail: String, amount: Double, bankName: String) {
        val senderUid = auth.currentUser!!.uid
        
        // Find recipient by email
        db.collection("users")
            .whereEqualTo("email", recipientEmail)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty()) {
                    Toast.makeText(this, "Recipient not found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val recipientDoc = documents.documents.first()
                val recipientUid = recipientDoc.id

                if (recipientUid == senderUid) {
                    Toast.makeText(this, "You cannot transfer to yourself", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // Execute Transaction
                val senderRef = db.collection("users").document(senderUid)
                val recipientRef = db.collection("users").document(recipientUid)

                db.runTransaction { transaction ->
                    val senderSnapshot = transaction.get(senderRef)
                    val senderBalance = senderSnapshot.getDouble("balance") ?: 0.0

                    if (senderBalance < amount) {
                        throw Exception("Insufficient funds")
                    }

                    val recipientSnapshot = transaction.get(recipientRef)
                    val recipientBalance = recipientSnapshot.getDouble("balance") ?: 0.0

                    transaction.update(senderRef, "balance", senderBalance - amount)
                    transaction.update(recipientRef, "balance", recipientBalance + amount)
                    
                    // Add a transaction record
                    val transactionData = hashMapOf(
                        "from" to senderUid,
                        "to" to recipientUid,
                        "amount" to amount,
                        "bank" to bankName,
                        "timestamp" to Timestamp.now()
                    )
                    transaction.set(db.collection("transactions").document(), transactionData)

                    null
                }.addOnSuccessListener {
                    Toast.makeText(this, "Transfer of $%.2f to $bankName successful".format(amount), Toast.LENGTH_LONG).show()
                    findViewById<EditText>(R.id.editRecipientAccount).text.clear()
                    findViewById<EditText>(R.id.editAmount).text.clear()
                }.addOnFailureListener { e ->
                    Toast.makeText(this, "Transfer Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error finding recipient: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}