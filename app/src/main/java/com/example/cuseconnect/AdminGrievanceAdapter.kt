package com.example.cuseconnect

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class AdminGrievanceAdapter(private val grievances: List<AdminGrievance>) : RecyclerView.Adapter<AdminGrievanceAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView =
            view.findViewById(R.id.grievance_name_text_view) // replace with your actual TextView ID
        val userTextView: TextView =
            view.findViewById(R.id.grievance_user) // replace with your actual TextView ID
        // ... other UI components
        val statusTextView: TextView =
            view.findViewById(R.id.grievance_status)
        val cardView: CardView = view.findViewById(R.id.cardView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.admin_grievance_item, parent, false)
        Log.d("Adapter", "Inflated admin_grievance_item layout") // Add this log statement
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val grievance = grievances[position]


        // Set the background color based on the status
        val cardBackgroundColor = when (grievance.status) {
            "Open" -> Color.parseColor("#ff7f50") // Yellow color
            "Resolved" -> Color.parseColor("#69a765") // Green color
            else -> Color.parseColor("#FFFFFF") // Default color
        }
        holder.cardView.setCardBackgroundColor(cardBackgroundColor)

        val grievanceNameLabel = "Grievance Name: "
        val statusLabel ="Status: "

        holder.nameTextView.text = grievanceNameLabel + grievance.name
        fetchUserName(grievance.userId, holder.userTextView)
        holder.statusTextView.text = statusLabel + grievance.status

        println("holder " + holder.nameTextView.text)
        // ... set other fields

        // Set click listener for the CardView
        holder.itemView.setOnClickListener {
            // Handle card click here
            val context = holder.itemView.context
            val intent = Intent(context, AdminDetailActivity::class.java)

            // Pass any data to the detail activity using intent extras if needed
            intent.putExtra("grievance",grievance) // Example: passing grievance ID

            context.startActivity(intent)

        }
    }

    private fun fetchUserName(userId: String?, userTextView: TextView) {
        // You need to implement the logic to fetch the user name based on the userId.
        // This can involve querying your database (e.g., Firebase Firestore) to retrieve the user name.
        // Once you have the user name, set it in the userTextView.
        val grievanceNameValue = "Created By: "
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("users").document(userId ?: "")
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val firstName = document.getString("first_name")
                    val lastName = document.getString("last_name")
                    val userName = "$firstName $lastName"
                    userTextView.text = grievanceNameValue + userName
                } else {
                    // Handle the case when the user document does not exist
                }
            }
            .addOnFailureListener { exception ->
                // Handle any errors that may occur during the query
                Log.w(TAG, "Error getting user document.", exception)
            }
    }


    override fun getItemCount() = grievances.size
}