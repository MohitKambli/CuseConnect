package com.example.cuseconnect

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RatingBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat.finishAffinity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import org.json.JSONArray

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RestaurantDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RestaurantDetailFragment : Fragment(), OnMapReadyCallback {
    // TODO: Rename and change types of parameters
    private var restaurantName: String? = null
    private var restaurantRating: Float = 0.0f
    private var restaurantPrice: String? = null
    private var restaurantImage: String? = null
    private var restaurantAddress: List<String> = emptyList()
    private var restaurantPhoneNumber: String? = null
    private var restaurantLatitude: Double = 0.0
    private var restaurantLongitude: Double = 0.0
    private var restaurantUrl: String? = null

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = view.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Get restaurant coordinates and set a marker on the map
        //val restaurantCoordinates = LatLng(43.05269, -76.1546)
        val restaurantCoordinates = LatLng(restaurantLatitude.toDouble(), restaurantLongitude.toDouble())
        googleMap.addMarker(MarkerOptions().position(restaurantCoordinates).title("Restaurant Location"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(restaurantCoordinates, 15f))
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isZoomGesturesEnabled = true
        googleMap.uiSettings.isScrollGesturesEnabled = true
        googleMap.uiSettings.isScrollGesturesEnabledDuringRotateOrZoom = true
        googleMap.uiSettings.isMapToolbarEnabled = true
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }


    fun setMovieDetails(restaurantName: String?, restaurantRating: Float, restaurantPrice: String?, restaurantImage: String?, restaurantAddress: List<String>, restaurantPhoneNumber: String?, restaurantLatitude: Double, restaurantLongitude: Double, restaurantUrl: String) {
        this.restaurantName = restaurantName
        this.restaurantRating = restaurantRating
        this.restaurantPrice = restaurantPrice
        this.restaurantImage = restaurantImage
        this.restaurantAddress = restaurantAddress
        this.restaurantPhoneNumber = restaurantPhoneNumber
        this.restaurantLatitude = restaurantLatitude
        this.restaurantLongitude = restaurantLongitude
        this.restaurantUrl = restaurantUrl
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            restaurantName = it.getString(ARG_RESTAURANT_NAME)
            restaurantRating = it.getFloat(ARG_RESTAURANT_RATING)
            restaurantPrice = it.getString(ARG_RESTAURANT_PRICE)
            restaurantImage = it.getString(ARG_RESTAURANT_IMAGE)
            restaurantAddress = it.getStringArrayList(ARG_RESTAURANT_ADDRESS) ?: emptyList()
            restaurantPhoneNumber = it.getString(ARG_RESTAURANT_PHONE_NUMBER)
            restaurantLatitude = it.getDouble(ARG_RESTAURANT_LATITUDE)
            restaurantLongitude = it.getDouble(ARG_RESTAURANT_LONGITUDE)
            restaurantUrl = it.getString(ARG_RESTAURANT_URL)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_restaurant_detail, container, false)
        if(
            restaurantName != null ||
            restaurantPrice != null ||
            restaurantImage != null ||
            restaurantAddress != null ||
            restaurantPhoneNumber != null ||
            restaurantUrl != null
        ) {

            val name = view.findViewById<TextView>(R.id.name)
            val price = view.findViewById<TextView>(R.id.price)
            val rating = view.findViewById<RatingBar>(R.id.rating)
            val image = view.findViewById<ImageView>(R.id.image)
            val address1 = view.findViewById<TextView>(R.id.restaurantAddressLine1)
            val address2 = view.findViewById<TextView>(R.id.restaurantAddressLine2)
            val phoneNumber = view.findViewById<TextView>(R.id.phoneNumber)
            rating.visibility = View.VISIBLE

            name.text = restaurantName
            price.text = restaurantPrice
            rating.rating = restaurantRating
            address1.text = restaurantAddress[0]
            address2.text = restaurantAddress[1]
            phoneNumber.text = "Phone: $restaurantPhoneNumber"

            println("RestaurantUrl (in DF): $restaurantUrl")

            // Set values in the Toolbar
//            val toolbarTitle = view.findViewById<TextView>(R.id.toolbarTitle)
//            toolbarTitle.text = restaurantName

            if (restaurantImage != null) {
                Picasso.get()
                    .load(restaurantImage)
                    .into(image, object : Callback {
                        override fun onSuccess() {
                            // Image loaded successfully
                        }

                        override fun onError(e: Exception?) {
                            // Handle the error
                            println("Error: ${e.toString()}")
                            println("Error ST: ${e?.printStackTrace()}")
                            e?.printStackTrace()
                        }
                    })
            }
        }

        val visitLinkButton: Button = view.findViewById(R.id.visitLinkButton)
        visitLinkButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            println("URL: $restaurantUrl")
            intent.data = Uri.parse(restaurantUrl)
            startActivity(intent)
        }

        return view
    }

    companion object {
        private const val ARG_RESTAURANT_NAME = "restaurantName"
        private const val ARG_RESTAURANT_RATING = "restaurantRating"
        private const val ARG_RESTAURANT_PRICE = "restaurantPrice"
        private const val ARG_RESTAURANT_IMAGE = "restaurantImage"
        private const val ARG_RESTAURANT_ADDRESS = "restaurantAddress"
        private const val ARG_RESTAURANT_PHONE_NUMBER = "restaurantPhoneNumber"
        private const val ARG_RESTAURANT_LATITUDE = "restaurantLatitude"
        private const val ARG_RESTAURANT_LONGITUDE = "restaurantLongitude"
        private const val ARG_RESTAURANT_URL = "restaurantUrl"
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RestaurantDetailFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(restaurantName: String?,
                        restaurantRating: Float,
                        restaurantPrice: String?,
                        restaurantImage: String?,
                        restaurantAddress: List<String>,
                        restaurantPhoneNumber: String?,
                        restaurantLatitude: Double,
                        restaurantLongitude: Double,
                        restaurantUrl: String) =
            RestaurantDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_RESTAURANT_NAME, restaurantName)
                    putFloat(ARG_RESTAURANT_RATING, restaurantRating)
                    putString(ARG_RESTAURANT_PRICE, restaurantPrice)
                    putString(ARG_RESTAURANT_IMAGE, restaurantImage)
                    putStringArrayList(ARG_RESTAURANT_ADDRESS, ArrayList(restaurantAddress))
                    putString(ARG_RESTAURANT_PHONE_NUMBER, restaurantPhoneNumber)
                    putDouble(ARG_RESTAURANT_LATITUDE, restaurantLatitude)
                    putDouble(ARG_RESTAURANT_LONGITUDE, restaurantLongitude)
                    putString(ARG_RESTAURANT_URL, restaurantUrl)
                }
            }
    }
}