package com.example.cuseconnect

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SplashActivity : AppCompatActivity() {

    private val SPLASH_TIME_OUT: Long = 2000
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        auth = FirebaseAuth.getInstance()

        Handler().postDelayed({
            // Check if the user is already authenticated
            val userUid = auth.currentUser?.uid
            if (userUid != null) {
                // The user is already authenticated, start the main activity accordingly
                checkLoggedInUser(userUid)
            } else {
                // The user is not authenticated, start the login activity
                startActivity(Intent(this, LoginActivity::class.java))
            }

            // Close this activity
            //finish()
        }, SPLASH_TIME_OUT)
    }

    private fun checkLoggedInUser(userId: String) {
        val usersCollection = FirebaseFirestore.getInstance().collection("users")
        val adminsCollection = FirebaseFirestore.getInstance().collection("admins")

        // Check if the user is in the "users" collection
        usersCollection.document(userId).get()
            .addOnSuccessListener { userDocument ->
                if (userDocument.exists()) {
                    // User is not an admin, start regular user activity
                    startRegularUserActivity()
                } else {
                    // User is not in the "users" collection, check the "admins" collection
                    adminsCollection.document(userId).get()
                        .addOnSuccessListener { adminDocument ->
                            if (adminDocument.exists()) {
                                // User is an admin, start admin activity
                                startAdminActivity()
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

    private fun startRegularUserActivity() {
        // Logic to start regular user activity
        startActivity(Intent(this, HomePageActivity::class.java))
        finish() // Optional: finish the current activity if needed
    }

    private fun startAdminActivity() {
        // Logic to start admin activity
        startActivity(Intent(this, HomePageActivityAdmin::class.java))
        finish() // Optional: finish the current activity if needed
    }

}