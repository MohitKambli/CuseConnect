package com.example.cuseconnect

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    data class Admin(
        val name: String,
        val email: String,
        val password: String,
        val facility: String,
        val subFacility: String,
        val suid: String,
        val isAdmin: Boolean
    )

    // Example list of admin details
    private val adminsList = listOf(
        Admin("Admin One", "admin1@syr.edu", "Password1", "Building", "Bird Library","123456789", true),
        Admin("Admin Two", "admin2@syr.edu", "Password2", "Building", "Bird Library","987654321", true),
        Admin("Admin Three", "admin3@syr.edu", "Password3", "Building", "Bird Library","234567890", true),
        Admin("Admin Four", "admin4@syr.edu", "Password4", "Building", "Carnegie Library","876543210", true),
        Admin("Admin Five", "admin5@syr.edu", "Password5", "Building", "Carnegie Library","345678901", true),
        Admin("Admin Six", "admin6@syr.edu", "Password6", "Building", "Carnegie Library","109876543", true),
        Admin("Admin Seven", "admin7@syr.edu", "Password7", "Building", "Barnes Center","456789012", true),
        Admin("Admin Eight", "admin8@syr.edu", "Password8", "Building", "Barnes Center","890123456", true),
        Admin("Admin Nine", "admin9@syr.edu", "Password9", "Building", "Barnes Center","567890123", true),
        Admin("Admin Ten", "admin10@syr.edu", "Password10", "Dorm", "Booth Hall", "321098765", true),
        Admin("Admin Eleven", "admin11@syr.edu", "Password11", "Dorm", "Booth Hall", "678901234", true),
        Admin("Admin Twelve", "admin12@syr.edu", "Password12", "Dorm", "Booth Hall", "210987654", true),
        Admin("Admin Thirteen", "admin13@syr.edu", "Password13", "Dorm", "Marion Hall", "543210987", true),
        Admin("Admin Fourteen", "admin14@syr.edu", "Password14", "Dorm", "Marion Hall", "901234567", true),
        Admin("Admin Fifteen", "admin15@syr.edu", "Password15", "Dorm", "Marion Hall", "432109876", true),
        Admin("Admin Sixteen", "admin16@syr.edu", "Password16", "Dorm", "Kimmel Hall","789012345", true),
        Admin("Admin Seventeen", "admin17@syr.edu", "Password17", "Dorm", "Kimmel Hall","876543211", true),
        Admin("Admin Eighteen", "admin18@syr.edu", "Password18", "Dorm", "Kimmel Hall","123456788", true),
        Admin("Admin Nineteen ", "admin19@syr.edu", "Password19", "Dining Hall", "Sadler Dining Hall", "234567899", true),
        Admin("Admin Twenty ", "admin20@syr.edu", "Password20", "Dining Hall", "Sadler Dining Hall", "987654322", true),
        Admin("Admin Twenty One", "admin21@syr.edu", "Password21", "Dining Hall", "Sadler Dining Hall", "345678900", true),
        Admin("Admin Twenty Two", "admin22@syr.edu", "Password22", "Dining Hall", "Ernie Dining Hall", "109876544", true),
        Admin("Admin Twenty Three", "admin23@syr.edu", "Password23", "Dining Hall", "Ernie Dining Hall", "456789011", true),
        Admin("Admin Twenty Four", "admin24@syr.edu", "Password24", "Dining Hall", "Ernie Dining Hall", "890123455", true),
        Admin("Admin Twenty Five", "admin25@syr.edu", "Password25", "Dining Hall", "Shaw Dining Hall", "567890122", true),
        Admin("Admin Twenty Six", "admin26@syr.edu", "Password26", "Dining Hall", "Shaw Dining Hall", "321098766", true),
        Admin("Admin Twenty Seven", "admin27@syr.edu", "Password27", "Dining Hall", "Shaw Dining Hall", "678901233", true),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createAdminAccounts(adminsList)

        startActivity(Intent(this, LoginActivity::class.java))
    }
    private fun createAdminAccounts(adminsList: List<Admin>) {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        adminsList.forEach { admin ->
            // Create Firebase Auth user account
            auth.createUserWithEmailAndPassword(admin.email, admin.password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser = task.result?.user
                        firebaseUser?.let { user ->
                            // Add additional details to Firestore admins collection
                            val adminDetails = hashMapOf(
                                "suid" to admin.suid,
                                "name" to admin.name,
                                "email" to admin.email,
                                "facility" to admin.facility,
                                "subFacility" to admin.subFacility
                            )
                            db.collection("admins").document(user.uid).set(adminDetails)
                                .addOnSuccessListener {
                                    Log.d("AdminCreation", "Admin details added for: ${admin.name}")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("AdminCreation", "Failed to add admin details for: ${admin.name}", e)
                                }
                        }
                    } else {
                        Log.e("AdminCreation", "Failed to create account for: ${admin.name}", task.exception)
                    }
                }
        }
    }

}