package com.example.cuseconnect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Get references to the EditText fields
        val firstNameEditText: EditText = findViewById(R.id.first_name)
        val lastNameEditText: EditText = findViewById(R.id.last_name)
        val emailEditText: EditText = findViewById(R.id.signup_email)
        val passwordEditText: EditText = findViewById(R.id.signup_password)
        val suidEditText: EditText = findViewById(R.id.signup_suid)

        val signupButton: Button = findViewById(R.id.signup)
        signupButton.setOnClickListener {
            if (validateForm()) {
                val firstName = firstNameEditText.text.toString()
                val lastName = lastNameEditText.text.toString()
                val email = emailEditText.text.toString()
                val password = passwordEditText.text.toString()
                val suid = suidEditText.text.toString()

                // Create user with Firebase Authentication
                createUserWithEmailAndPassword(firstName, lastName, email, password, suid)
            }
        }
    }

    private fun createUserWithEmailAndPassword(firstName: String, lastName: String, email: String, password: String, suid: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                    val userDetails = hashMapOf(
                        "first_name" to firstName,
                        "last_name" to lastName,
                        "suid" to suid,
                        "email" to email
                    )

                    // Add additional details to Firestore
                    db.collection("users").document(userId).set(userDetails)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Successful sign up", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Log.w("SignUpActivity", "Error adding user details to Firestore", e)
                            Toast.makeText(this, "Failed to store additional user details", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun validateForm(): Boolean {
        val firstName = findViewById<EditText>(R.id.first_name).text.toString()
        val lastName = findViewById<EditText>(R.id.last_name).text.toString()
        val email = findViewById<EditText>(R.id.signup_email).text.toString()
        val password = findViewById<EditText>(R.id.signup_password).text.toString()
        val suid = findViewById<EditText>(R.id.signup_suid).text.toString()

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || suid.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!email.endsWith("syr.edu")) {
            Toast.makeText(this, "Email must end with syr.edu", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.length < 8) {
            Toast.makeText(this, "Password must be at least 8 characters long", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
}