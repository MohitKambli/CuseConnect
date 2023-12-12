package com.example.cuseconnect

import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomePageActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var toolbar: Toolbar
    private lateinit var toolbarTitle: TextView

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var auth: FirebaseAuth

    private lateinit var toggle: ActionBarDrawerToggle

    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
//        toolbarTitle = findViewById(R.id.toolbarTitle)
//        toolbarTitle.text = "CuseConnect"
        setSupportActionBar(toolbar)
    }

    private val images = arrayOf(
        R.drawable.image_restaurants,
        R.drawable.image_events,
        R.drawable.image_grievances
    )
    private var currentImageIndex = 0
    private lateinit var slideshowImageView: ImageView
    private lateinit var imageTitleTextView: TextView
    private val imageTitles = arrayOf(
        "Explore Restaurants",
        "Discover Events",
        "Report Grievances"
    )
    private val changeImageRunnable = object : Runnable {
        override fun run() {
            // Set the image resource
            slideshowImageView.setImageResource(images[currentImageIndex])

            // Set the text for the image title
            imageTitleTextView.text = imageTitles[currentImageIndex]

            // Increment the index for the next image and title
            currentImageIndex = (currentImageIndex + 1) % images.size

            // Schedule the next change
            handler.postDelayed(this, 2000)  // Change image every 2 seconds
        }
     }
    private val handler = Handler(Looper.getMainLooper())

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(changeImageRunnable)  // Important to avoid memory leaks
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        slideshowImageView = findViewById(R.id.slideshowImageView)
        imageTitleTextView = findViewById(R.id.titleTextView)
        handler.post(changeImageRunnable)

        setupToolbar()

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        auth = FirebaseAuth.getInstance()

        toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, 0, 0
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navView.setNavigationItemSelectedListener(this)

        // Display the user icon and handle its click event
        val userIcon: ImageView = findViewById(R.id.userIcon)
        userIcon.visibility = View.VISIBLE
        userIcon.setOnClickListener {
            showUserDetailsPopup(it)
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
            }
            R.id.events -> {
                startActivity(Intent(this, RecyclerViewActivityEvents::class.java))
                //Toast.makeText(this, "Task-2 clicked", Toast.LENGTH_SHORT).show()
            }
            R.id.restaurants -> {
                startActivity(Intent(this, RecyclerViewActivityRestaurants::class.java))
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

}