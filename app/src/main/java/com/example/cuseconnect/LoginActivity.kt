package com.example.cuseconnect

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        // Check if the user is already authenticated in onCreate
        val userUid = FirebaseAuth.getInstance().currentUser?.uid
        if (userUid != null) {
            checkLoggedInUser(userUid)
            finish() // Finish LoginActivity if the user is already authenticated
        }

        // Login button click listener
        val loginButton: Button = findViewById(R.id.login)
        loginButton.setOnClickListener {
            // TODO: Add login logic here

            val emailEditText: EditText = findViewById(R.id.email)
            val passwordEditText: EditText = findViewById(R.id.password)

            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                signInWithEmailAndPassword(email, password)
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Redirect to Signup Activity when "Create Account" is clicked
        val signupRedirect: TextView = findViewById(R.id.signup_redirect)
        signupRedirect.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    private fun signInWithEmailAndPassword(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        // Check if the user is an admin
                        checkLoggedInUser(currentUser.uid)
                    } else {
                        // Handle the case where the current user is null
                        Toast.makeText(this, "Error: Current user is null", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkLoggedInUser(userId: String) {
        val usersCollection = FirebaseFirestore.getInstance().collection("users")
        val adminsCollection = FirebaseFirestore.getInstance().collection("admins")

        println("UserID is here in checkedLoggedInUser: $userId")

        // Check if the user is in the "users" collection
        usersCollection.document(userId).get()
            .addOnSuccessListener { userDocument ->
                if (userDocument.exists()) {
                    // User is not an admin, start regular user activity
                    Toast.makeText(this, "Logged In Successfully", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HomePageActivity::class.java))
                    finish()
                } else {
                    // User is not in the "users" collection, check the "admins" collection
                    adminsCollection.document(userId).get()
                        .addOnSuccessListener { adminDocument ->
                            if (adminDocument.exists()) {
                                // User is an admin, start admin activity
                                println("UserID is here in admin document exists: $userId")
                                Toast.makeText(this, "Logged In Successfully", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, HomePageActivityAdmin::class.java))
                                finish()
                            } else {
                                // User is not in the "admins" collection either
                                // Handle accordingly (e.g., show an error message)
                                Toast.makeText(this, "User not found in 'users' or 'admins' collection", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener { e ->
                            // Handle failure when checking "admins" collection
                            Toast.makeText(this, "Error checking 'admins' collection: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                // Handle failure when checking "users" collection
                Toast.makeText(this, "Error checking 'users' collection: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}