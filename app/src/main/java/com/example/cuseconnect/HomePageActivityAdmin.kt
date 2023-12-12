package com.example.cuseconnect

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomePageActivityAdmin : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdminGrievanceAdapter
    private val grievances = mutableListOf<AdminGrievance>()


    private lateinit var toolbar: Toolbar

    //private lateinit var drawerLayout: DrawerLayout
    private lateinit var auth: FirebaseAuth

//    private lateinit var toggle: ActionBarDrawerToggle

    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home_page_admin)

        setupToolbar()

        //drawerLayout = findViewById(R.id.drawer_layout)
        auth = FirebaseAuth.getInstance()

//        toggle = ActionBarDrawerToggle(
//            this, drawerLayout, toolbar, 0, 0
//        )
//        drawerLayout.addDrawerListener(toggle)
//        toggle.syncState()

        // Display the user icon and handle its click event
        val userIcon: ImageView = findViewById(R.id.userIcon)
        userIcon.visibility = View.VISIBLE
        userIcon.setOnClickListener {
            showUserDetailsPopup(it)
        }

        recyclerView = findViewById(R.id.grievanceRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = AdminGrievanceAdapter(grievances)
        recyclerView.adapter = adapter

        fetchGrievances()

    }

    private fun fetchGrievances() {
        val db = FirebaseFirestore.getInstance()
        val adminId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("grievances")
            .whereEqualTo("assignedAdmin", adminId)
            .get()
            .addOnSuccessListener { documents ->
                grievances.clear()
                for (document in documents) {
                    val grievance = document.toObject(AdminGrievance::class.java).copy(id = document.id)
                    Log.e("YourActivity", "Grievance display")
                    println("grivance diapsplay: "+grievance.name)


                    grievances.add(grievance)

                    println("grivance display: $grievances")

                }
                //updateRecyclerView()
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("YourActivity", "Error fetching grievances", e)
            }
    }

    private fun updateRecyclerView() {
        val parentLayout: ViewGroup = findViewById(R.id.grievanceRecyclerView)

        if (grievances.isEmpty()) {
            // Show "No Grievances Raised" text when there are no grievances
            val noGrievancesTextView = TextView(this)
            noGrievancesTextView.text = "No Grievances Raised"
            noGrievancesTextView.gravity = Gravity.CENTER
            noGrievancesTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)

            // Remove the RecyclerView from the parent layout
            parentLayout.removeAllViews()

            // Add the "No Grievances Raised" text to the parent layout
            parentLayout.addView(noGrievancesTextView)
        } else {
            // Update the RecyclerView with the grievances
            val recyclerView = findViewById<RecyclerView>(R.id.grievanceRecyclerView)

            // Null check for recyclerView
            recyclerView?.let {
                // If RecyclerView is already a child, remove it
                if (it.parent != null) {
                    (it.parent as ViewGroup).removeView(it)
                }

                // Add the RecyclerView to the parent layout
                parentLayout.addView(it)

                // Set up the adapter and notify data changes
                adapter.notifyDataSetChanged()
            }
        }
    }




    private fun showUserDetailsPopup(view: View) {
        // Inflate the popup layout
        val popupView = layoutInflater.inflate(R.layout.popup_user_details, null)

        // Create the PopupWindow
        val popupWindow = PopupWindow(
            popupView,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            true
        )

        // Customize the PopupWindow as needed (e.g., set animation, background, etc.)
        //popupWindow.animationStyle = R.style.PopupAnimation
        popupWindow.setBackgroundDrawable(ColorDrawable())
        popupWindow.isOutsideTouchable = true

        // Set up views and actions within the popup
        val userDetailsTextView: TextView = popupView.findViewById(R.id.userDetailsTextView)
        val signOutButton: Button = popupView.findViewById(R.id.signOutButton)

        // Populate user details (replace with actual user details)
        // Assuming auth is a FirebaseAuth instance
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val uid = currentUser.uid
            val usersCollection = FirebaseFirestore.getInstance().collection("admins")
            val userDocRef = usersCollection.document(uid)
            userDocRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        // User details are available
                        val name = documentSnapshot.getString("name") ?: "N/A"
                        val suid = documentSnapshot.getString("suid") ?: "N/A"
                        val facility = documentSnapshot.getString("facility") ?: "N/A"
                        val email = currentUser.email

                        userDetailsTextView.text = "Name: $name\nSUID: $suid\nFacility: $facility\nEmail: $email"
                    } else {
                        // User details not found
                        userDetailsTextView.text = "User details not available"
                    }
                }
                .addOnFailureListener { e ->
                    // Handle failure
                    userDetailsTextView.text = "Error: ${e.message}"
                }
        } else {
            // No user is signed in
            userDetailsTextView.text = "User not signed in"
        }

        // Set click listener for the sign out button
        signOutButton.setOnClickListener {
            // Handle sign out logic
            //showSignOutDialog()
            signOut()
            popupWindow.dismiss()
        }

        // Show the popup at a specific location relative to the user icon
        popupWindow.showAsDropDown(view, -200, -20) // Adjust these values based on your UI

        // Dismiss the popup when clicked outside
        popupView.setOnClickListener {
            popupWindow.dismiss()
        }
    }

    private fun signOut() {
        auth.signOut()
        // Redirect to the login screen or perform other actions
        startActivity(Intent(this, LoginActivity::class.java))
        // Finish all activities in the stack
        finishAffinity()
    }
}