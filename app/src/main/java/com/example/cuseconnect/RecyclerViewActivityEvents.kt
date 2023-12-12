package com.example.cuseconnect

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView

class RecyclerViewActivityEvents : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var eventsRecyclerView: RecyclerView
    private lateinit var statusTextView: TextView
    private lateinit var adapter: MyEventsAdapter
    private var events: MutableList<Event> = mutableListOf()

    private lateinit var toolbarTitle: TextView
    private lateinit var toolbar: Toolbar
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var auth: FirebaseAuth
    private lateinit var navView: NavigationView

    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
        toolbar.title = "Events"
        setSupportActionBar(toolbar)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler_view_events)

        setupToolbar()

//        drawerLayout = findViewById(R.id.drawer_layout)
//        toggle = ActionBarDrawerToggle(
//            this, drawerLayout, toolbar, 0, 0
//        )
//        drawerLayout.addDrawerListener(toggle)
//        toggle.syncState()

//        val searchView = findViewById<SearchView>(R.id.searchView)

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
//        val userIcon: ImageView = findViewById(R.id.userIcon)
//        userIcon.visibility = View.VISIBLE
//        userIcon.setOnClickListener {
//            showUserDetailsPopup(it)
//        }

        val sortSpinner: Spinner = findViewById(R.id.sortSpinner)
        // Set up sortSpinner adapter and listener
        // Set up filterSpinner adapter and listener
        val spinner: Spinner = findViewById(R.id.sortSpinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.sort_options,
            R.layout.spinner_item // Use your custom layout here
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.spinner_item) // And here
            spinner.adapter = adapter
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                if (position > 0) { // Ensure the selected position is not the default one
                    when (position) {
                        0 -> {}
                        1 -> sortByNameAscending()
                        2 -> sortByNameDescending()
                        3 -> sortByDateAscending()
                        4 -> sortByDateDescending()
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Another interface callback
            }
        }
        statusTextView = findViewById(R.id.status_text_view)
        eventsRecyclerView = findViewById(R.id.events_recycler_view)

        // Initialize your RecyclerView adapter here
        adapter = MyEventsAdapter(emptyList(), this)
        eventsRecyclerView.adapter = adapter
        eventsRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter.resetFilter()

        // Display the user icon and handle its click event
        val userIcon: ImageView = findViewById(R.id.userIcon)
        userIcon.visibility = View.VISIBLE
        userIcon.setOnClickListener {
            showUserDetailsPopup(it)
        }

        // You will update your adapter data when your data changes.
        // For this example, let's just show how you would set some dummy data.
        val viewModel: EventOverviewViewModel by viewModels()
        viewModel.events.observe(this, { events ->
            // Update your adapter and the status text view
            statusTextView.text = if (events.isEmpty()) "Loading..." else ""
            adapter.updateEvents(events)
            println("EVENTS LOADED: " +events)
        })

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

    private fun sortByNameAscending() {
        adapter.sortEvents { it.sortedBy { event -> event.name } }
    }

    private fun sortByNameDescending() {
        adapter.sortEvents { it.sortedByDescending { event -> event.name } }
    }

    private fun sortByDateAscending() {
        adapter.sortEvents { it.sortedBy { event -> SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).parse(event.dates.start.dateTime) } }
    }

    private fun sortByDateDescending() {
        adapter.sortEvents { it.sortedByDescending { event -> SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).parse(event.dates.start.dateTime) } }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_search, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as? SearchView

        // Customize the SearchView
        val searchEditText = searchView?.findViewById(androidx.appcompat.R.id.search_src_text) as? EditText
        searchEditText?.setTextColor(Color.WHITE)
        searchEditText?.setHintTextColor(Color.WHITE)
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { performSearch(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { performSearch(it) }
                return true
            }
        })
        return false
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

    private fun performSearch(query: String) {
        adapter.filterEvents(query)
    }
}


class CustomSpinnerAdapter(context: Context, resource: Int, objects: List<String>) :
    ArrayAdapter<String>(context, resource, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent) as TextView
        // Set the "Sort By" label only for the first item
        if (position == 0) {
            view.text = "Sort By"
        } else {
            view.text = getItem(position)
        }
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        // This changes the items only in the dropdown view, not the selected item view
        val view = super.getDropDownView(position, convertView, parent) as TextView
        if (position == 0) {
            // Make the "Sort By" item look like a disabled item or a prompt
            view.setTextColor(ContextCompat.getColor(context, R.color.white)) // Set color to gray or any prompt-like appearance
        }
        return view
    }

    // Optional: Override this method if you don't want "Sort By" to be selectable
    override fun isEnabled(position: Int): Boolean {
        return position != 0 // Disable the first item ("Sort By")
    }
}
