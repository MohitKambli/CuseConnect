package com.example.cuseconnect

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserGrievancesActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, UserGrievanceAdapter.OnFeedbackButtonClickListener {

    private lateinit var toolbarTitle: TextView
    private lateinit var toolbar: Toolbar
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var auth: FirebaseAuth

    private lateinit var adapter: UserGrievanceAdapter
    private lateinit var recyclerView : RecyclerView
    private lateinit var navView: NavigationView
    private var grievancesList = ArrayList<UserGrievance>()

    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
        toolbar.title = "Grievances"
        setSupportActionBar(toolbar)
    }

    private fun parseGrievances(callback: () -> Unit) {
        // TODO: Implement your logic to retrieve grievances for the current user from Firebase Firestore

        // Assuming you have the currently logged-in user's ID
        val currentUserID = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserID != null) {
            val db = FirebaseFirestore.getInstance()
            val grievancesCollection = db.collection("grievances")

            // Query the grievances collection to get documents only for the current user
            grievancesCollection.whereEqualTo("userId", currentUserID)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val title = document.getString("name") ?: ""
                        val description = document.getString("description") ?: ""
                        val facility = document.getString("facility") ?: ""
                        val subfacility = document.getString("subfacility") ?: ""
                        val status = document.getString("status") ?: ""
                        val feedback = document.getString("feedback") ?: ""
                        val images = document.get("images") as? List<String> ?: emptyList()


                        val grievance = UserGrievance(title, description, facility, subfacility, images, status, feedback)
                        grievancesList.add(grievance)
                    }
                    callback.invoke()
                }
                .addOnFailureListener { exception ->
                    // Handle failure
                    exception.printStackTrace()
                }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grievances)

        setupToolbar()

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, 0, 0
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navView.setNavigationItemSelectedListener(this)

//        val searchView = findViewById<SearchView>(R.id.searchView)
//        searchView.visibility = View.VISIBLE
        auth = FirebaseAuth.getInstance()

        parseGrievances {
            recyclerView = findViewById(R.id.recyclerView)
            recyclerView.layoutManager = LinearLayoutManager(this)
            if (grievancesList.isEmpty()) {
                val noGrievancesTextView = TextView(this)
                noGrievancesTextView.text = "No grievances!"
                val darkGrey = ContextCompat.getColor(this, R.color.dark_gray)
                noGrievancesTextView.setTextColor(darkGrey)
                noGrievancesTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
                noGrievancesTextView.textAlignment = View.TEXT_ALIGNMENT_CENTER
                noGrievancesTextView.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                val parentView = recyclerView.parent as ViewGroup
                parentView.removeAllViews()
                parentView.addView(noGrievancesTextView)
            } else {
                adapter = UserGrievanceAdapter(this, this, recyclerView, grievancesList, this)
                recyclerView.adapter = adapter
            }
        }

        // Display the user icon and handle its click event
        val userIcon: ImageView = findViewById(R.id.userIcon)
        userIcon.visibility = View.VISIBLE
        userIcon.setOnClickListener {
            showUserDetailsPopup(it)
        }

        // Initialize FloatingActionButton
        val fabAddGrievance = findViewById<FloatingActionButton>(R.id.fabAddGrievance)
        fabAddGrievance.visibility = View.VISIBLE

        // Set click listener for the FloatingActionButton
        fabAddGrievance.setOnClickListener {
            // Handle the click event (e.g., navigate to the screen for creating a new grievance)
            finish()
            startActivity(Intent(this, CreateGrievanceActivity::class.java))
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
            // User is signed in
            val uid = currentUser.uid

            // Assuming your Firestore collection is named "users"
            val usersCollection = FirebaseFirestore.getInstance().collection("users")

            // Get the document reference for the user's UID
            val userDocRef = usersCollection.document(uid)

            // Read data from the Firestore document
            userDocRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        // User details are available
                        val firstName = documentSnapshot.getString("first_name") ?: "N/A"
                        val lastName = documentSnapshot.getString("last_name") ?: "N/A"
                        val suid = documentSnapshot.getString("suid") ?: "N/A"
                        val email = currentUser.email

                        userDetailsTextView.text = "First Name: $firstName\nLast Name: $lastName\nSUID: $suid\nEmail: $email"
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

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.grievances -> {
                startActivity(Intent(this, UserGrievancesActivity::class.java))
                finish()
            }
            R.id.events -> {
                startActivity(Intent(this, RecyclerViewActivityEvents::class.java))
                finish()
            }
            R.id.restaurants -> {
                startActivity(Intent(this, RecyclerViewActivityRestaurants::class.java))
                finish()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    fun onItemClicked(title: String, description: String, facility: String, subFacility: String, images: List<String>, status: String, feedback: String) {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

        val fragment = UserGrievanceDetailFragment.newInstance(
            title, description, facility, subFacility, ArrayList(images), status, feedback
        )
        fragment.setMovieDetails(title, description, facility, subFacility, ArrayList(images), status, feedback)
        supportFragmentManager.beginTransaction()
            .replace(R.id.recyclerViewLayout, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onFeedbackButtonClicked(feedback: String) {
        // Show a popup window with the feedback
        showFeedbackPopup(feedback)
    }

    private fun showFeedbackPopup(feedback: String) {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.popup_feedback, null)

        val popupWindow = PopupWindow(
            view,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            true
        )

        // Set up views and actions within the popup
        val feedbackTextView: TextView = view.findViewById(R.id.popupFeedbackTextView)
        feedbackTextView.text = feedback

        val closePopupButton: Button = view.findViewById(R.id.closePopupButton)
        closePopupButton.setOnClickListener {
            popupWindow.dismiss()
        }

        // Show the popup at a specific location relative to the parent view
        val parentView = findViewById<View>(R.id.recyclerView)  // or use any other parent view
        popupWindow.showAtLocation(parentView, Gravity.CENTER, 0, 0)
    }

}