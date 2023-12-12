package com.example.cuseconnect

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Locale

class RecyclerViewActivityRestaurants : AppCompatActivity(), RestaurantAdapter.ItemClickListener, NavigationView.OnNavigationItemSelectedListener {

    private lateinit var toolbarTitle: TextView
    private lateinit var toolbar: Toolbar

    private lateinit var toggle: ActionBarDrawerToggle

    private lateinit var adapter: RestaurantAdapter
    private lateinit var recyclerView : RecyclerView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var auth: FirebaseAuth
    private lateinit var navView: NavigationView
    private var restaurantsList = ArrayList<Restaurant>()

    // Yelp API configuration
//    private val apiKey = "GZkg6b2vZHnTqulSuuRxPD0r74Ny53I6jbkxlXZFcR51lL0D3yyn1ZYiK2qwjHiuLcJIMD10nP1VPuOQGd-lHRQ7MmO2MBYqGmTb_jnvH7hVXURBAYZBy6jUNiseZXYx"
//    private val location = "SYR"
//    private val baseUrl = "https://api.yelp.com/v3/"

    private fun parseData(context: Context, callback: () -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            val apiKey = "GZkg6b2vZHnTqulSuuRxPD0r74Ny53I6jbkxlXZFcR51lL0D3yyn1ZYiK2qwjHiuLcJIMD10nP1VPuOQGd-lHRQ7MmO2MBYqGmTb_jnvH7hVXURBAYZBy6jUNiseZXYx"
            val location = "SYR"
            val baseUrl = "https://api.yelp.com/v3/businesses/search?location=$location"

            val client = OkHttpClient()

            val request = Request.Builder()
                .url(baseUrl)
                .header("Authorization", "Bearer $apiKey")
                .build()

            try {
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    println("Error: ${response.code}: ${response.message}")
                } else {
                    val responseBody = response.body?.string()
                    val jsonObject = JSONObject(responseBody)
                    val restaurantsArray = jsonObject.getJSONArray("businesses")
                    for (i in 0 until restaurantsArray.length()) {
                        val restaurantObject = restaurantsArray.getJSONObject(i)
                        val restaurantName = restaurantObject.getString("name")
                        val restaurantRating = restaurantObject.getString("rating").toFloat()
                        var restaurantPrice = ""
                        if (restaurantObject.has("price")) {
                            restaurantPrice = restaurantObject.getString("price")
                        } else {
                            restaurantPrice = "N/A" // For example, setting a default value.
                        }
                        val restaurantImage = restaurantObject.getString("image_url")
                        val restaurantUrl = restaurantObject.getString("url")
                        println("URL in RV: $restaurantUrl")
                        val addressList = restaurantObject.getJSONObject("location").getJSONArray("display_address")
                        // Convert JSONArray to List<String>
                        val restaurantAddress: List<String> = (0 until addressList.length()).map {
                            addressList.getString(it)
                        }
                        val restaurantPhoneNumber = restaurantObject.getString("phone")
                        val restaurantLatitude = restaurantObject.getJSONObject("coordinates").getString("latitude").toDouble()
                        val restaurantLongitude = restaurantObject.getJSONObject("coordinates").getString("longitude").toDouble()

                        val restaurant = Restaurant(
                            restaurantName,
                            restaurantRating,
                            restaurantPrice,
                            restaurantImage,
                            restaurantAddress,
                            restaurantPhoneNumber,
                            restaurantLatitude,
                            restaurantLongitude,
                            restaurantUrl
                        )
                        restaurantsList.add(restaurant)
                        println(restaurant)
                    }
                }
                callback.invoke()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
        toolbar.title = "Restaurants"
        setSupportActionBar(toolbar)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler_view_restaurants)

        setupToolbar()

//        val searchView = findViewById<SearchView>(R.id.searchView)
//        searchView.visibility = View.VISIBLE

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        auth = FirebaseAuth.getInstance()

        toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, 0, 0
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navView.setNavigationItemSelectedListener(this)

        parseData(this) {
            runOnUiThread {
                recyclerView = findViewById(R.id.recyclerView)
                recyclerView.layoutManager = LinearLayoutManager(this)
                adapter = RestaurantAdapter(this, this, recyclerView, restaurantsList)
                recyclerView.adapter = adapter


                // Initialize and set up the Spinner in the bottom action bar
                val sortSpinner: Spinner = findViewById(R.id.sortSpinner)
                ArrayAdapter.createFromResource(
                    this,
                    R.array.sort_options_restaurants,
                    R.layout.spinner_item
                ).also { adapter ->
                    adapter.setDropDownViewResource(R.layout.spinner_item)
                    sortSpinner.adapter = adapter
                }

                // Set a default selection to avoid automatic sorting
                sortSpinner.setSelection(0, false)

                // Handle Spinner item selection
                sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        if (position > 0) { // Ensure the selected position is not the default one
                            when (position) {
                                0 -> {}
                                1 -> sortByNameAscending()
                                2 -> sortByNameDescending()
                                3 -> sortByRatingAscending()
                                4 -> sortByRatingDescending()
                            }
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        // Do nothing
                    }
                }
            }
        }

        // Display the user icon and handle its click event
        val userIcon: ImageView = findViewById(R.id.userIcon)
        userIcon.visibility = View.VISIBLE
        userIcon.setOnClickListener {
            showUserDetailsPopup(it)
        }
    }



    private fun sortByNameAscending() {
        restaurantsList.sortBy { it.name }
        adapter.notifyDataSetChanged()
    }

    private fun sortByNameDescending() {
        restaurantsList.sortByDescending { it.name }
        adapter.notifyDataSetChanged()
    }

    private fun sortByRatingAscending() {
        restaurantsList.sortBy { it.rating }
        adapter.notifyDataSetChanged()
    }

    private fun sortByRatingDescending() {
        restaurantsList.sortByDescending { it.rating }
        adapter.notifyDataSetChanged()
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


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { adapter.filter(query) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { adapter.filter(newText) }
                return true
            }
        })
        return false

        //return super.onCreateOptionsMenu(menu)
    }

    override fun onItemClicked(
        restaurantName: String?,
        restaurantRating: Float,
        restaurantPrice: String,
        restaurantImage: String?,
        restaurantAddress: List<String>,
        restaurantPhoneNumber: String?,
        restaurantLatitude: Double,
        restaurantLongitude: Double,
        restaurantUrl: String
    ) {

        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

        val fragment = RestaurantDetailFragment.newInstance(
            restaurantName, restaurantRating, restaurantPrice, restaurantImage, restaurantAddress, restaurantPhoneNumber, restaurantLatitude, restaurantLongitude, restaurantUrl
        )
        fragment.setMovieDetails(restaurantName, restaurantRating, restaurantPrice, restaurantImage, restaurantAddress, restaurantPhoneNumber, restaurantLatitude, restaurantLongitude, restaurantUrl)
        supportFragmentManager.beginTransaction()
            .replace(R.id.recyclerViewLayout, fragment)
            .addToBackStack(null)
            .commit()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Check if there are fragments in the back stack
        if (supportFragmentManager.backStackEntryCount > 0) {
            // Hide the keyboard
            hideKeyboard()
            // Pop the fragment from the back stack
            supportFragmentManager.popBackStack()
        } else {
            // If there are no fragments in the back stack, proceed with default back button behavior
            super.onBackPressed()
        }
    }

    private fun hideKeyboard() {
        currentFocus?.let { currentFocus ->
            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }

}

class CustomSpinnerAdapterRestaurants(context: Context, resource: Int, objects: List<String>) :
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
