package com.example.cuseconnect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import android.widget.Spinner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class AdminDetailActivity : AppCompatActivity() {
    private lateinit var grievance: AdminGrievance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_detail)

        // Retrieve the Grievance object from the intent extras
        grievance = intent.getSerializableExtra("grievance") as AdminGrievance

        // Populate the TextViews and ImageView with grievance data
        val nameTextView = findViewById<TextView>(R.id.nameTextView)
        //val statusTextView = findViewById<TextView>(R.id.statusTextView)
        val facilityTextView = findViewById<TextView>(R.id.facilityTextView)
        val subfacilityTextView = findViewById<TextView>(R.id.subfacilityTextView)
        val userNameTextView = findViewById<TextView>(R.id.userNameTextView)
        val descriptionTextView = findViewById<TextView>(R.id.descriptionTextView)
       // val imageView = findViewById<ImageView>(R.id.imageView)

        nameTextView.text = "Name: ${grievance.name}"
        // statusTextView.text = "Status: ${grievance.status}"
        facilityTextView.text = "Facility: ${grievance.facility}"
        subfacilityTextView.text = "Subfacility: ${grievance.subfacility}"
        // Get a reference to the Firestore instance
        val firestore = FirebaseFirestore.getInstance()

// Assuming your userId is stored in the grievance object as "userId"
        val userId = grievance.userId

// Check if userId is not null
        if (userId != null) {
            // Reference to the user document in Firestore
            val userDocRef = firestore.collection("users").document(userId)

            userDocRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Document exists, fetch user data
                        val firstName = document.getString("first_name")
                        val lastName = document.getString("last_name")

                        // Combine first_name and last_name to get full name
                        val fullName = "$firstName $lastName"

                        // Now, set the full name in your TextView
                        userNameTextView.text = "User Name: $fullName"
                    } else {
                        // Handle the case when the document does not exist
                        userNameTextView.text = "User Name: (User Not Found)"
                    }
                }
                .addOnFailureListener { e ->
                    // Handle any errors that may occur during the fetch
                    Log.e("UserFetch", "Error fetching user data", e)
                }
        }


        descriptionTextView.text = "Description: ${grievance.description}"

        // Initialize the Spinner
        val statusSpinner = findViewById<Spinner>(R.id.statusSpinner)

        // Create an ArrayAdapter to populate the Spinner with status options
        val statusOptions = arrayOf("Open", "Resolved")
        val adapter = ArrayAdapter(this, R.layout.custom_spinner_item, statusOptions)
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item)

        // Set the ArrayAdapter on the Spinner
        statusSpinner.adapter = adapter

        // Set an item selected listener if needed
        statusSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedStatus = statusOptions[position]
                // Handle the selected status here (e.g., update the grievance status in Firestore)
                Log.d("StatusSelected", "Selected Status: $selectedStatus")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle when nothing is selected
            }
        }


        // Load images if available (assuming you have image URLs)
        // You can use a library like Picasso or Glide to load images efficiently
        val images = grievance.images as? List<String>
        if (images != null) {
            val recyclerView = findViewById<RecyclerView>(R.id.imageView)
            recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            val imageAdapter = GrievanceImagesAdapter(images)
            recyclerView.adapter = imageAdapter
        } else {
            // Handle the case when images are null (e.g., no images available)
            val recyclerView = findViewById<RecyclerView>(R.id.imageView)
            recyclerView.visibility = View.GONE // Hide the RecyclerView or handle as needed
        }

        val updateStatusButton = findViewById<Button>(R.id.updateStatusButton)
        val statusEditText = findViewById<EditText>(R.id.statusEditText)

        updateStatusButton.setOnClickListener {
            val selectedStatus = statusSpinner.selectedItem.toString() // Get the selected status from the Spinner
            val feedback = statusEditText.text.toString() // Get the text from the EditText

            // Create a HashMap with nullable types
            val updateData = hashMapOf<String, Any?>(
                "status" to selectedStatus,
                "feedback" to feedback
            )

// Update the Grievance in Firestore
            val firestore = FirebaseFirestore.getInstance()
            val grievanceRef = firestore.collection("grievances").document(grievance.id!!)

            grievanceRef.update(updateData as Map<String, Any>)
                .addOnSuccessListener {
                    // Handle success
                    Log.d("UpdateStatus", "Grievance status updated successfully")
                    val intent = Intent(this@AdminDetailActivity, HomePageActivityAdmin::class.java)

                    // Add any extra data you want to pass back to the RecyclerView activity
                    // For example, you can pass a success message or updated data

                    // Start the RecyclerView activity
                    startActivity(intent)

                    // Finish the current activity (optional, if you don't want to go back to it)
                    finish()
                }
                .addOnFailureListener { e ->
                    // Handle failure
                    Log.e("UpdateStatus", "Error updating grievance status", e)
                }

        }




    }






    }