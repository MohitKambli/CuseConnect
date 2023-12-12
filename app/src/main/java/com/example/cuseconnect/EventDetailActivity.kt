package com.example.cuseconnect

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class EventDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var event: Event

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_detail)

        Log.d("EventDetailActivity", "Activity started")

        // Retrieve event data from intent
        event = intent.getSerializableExtra("EVENT_DATA") as Event
        Log.d("EventDetailActivity", "Event received: ${event.name}")
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)


        // Check if venues data is available
        val venues = event.embedded.venues
        if (!venues.isNullOrEmpty()) {
            Log.d("EventDetailActivity", "Venues available for event: ${event.name}")
            for (venue in venues) {
                Log.d(
                    "EventDetailActivity",
                    "Venue Name: ${venue.name}, Address: ${venue.address?.line1}, Location: Lat ${venue.location.latitude}, Long ${venue.location.longitude}"
                )
            }
        } else {
            Log.d("EventDetailActivity", "No venues data available for event: ${event.name}")
        }


        // Populate views
        findViewById<TextView>(R.id.eventNameTextView).text = event.name
        findViewById<ImageView>(R.id.eventImageView).let { imageView ->
            Glide.with(this).load(event.images.firstOrNull()?.url).into(imageView)
        }
        findViewById<TextView>(R.id.eventInfoTextView).text =
            event.info ?: "No additional information available"

        val priceRangeText = event.priceRanges?.joinToString(separator = "\n") {
            "Ticket Price Range: ${it.currency} ${it.minPrice} - ${it.maxPrice}"
        } ?: "Price information not available"
        findViewById<TextView>(R.id.eventPriceRangeTextView).text = priceRangeText

        findViewById<TextView>(R.id.eventDateTextView).text = "Date: ${event.dates.start.localDate}"
        findViewById<TextView>(R.id.eventStartTimeTextView).text =
            "Start Time: ${event.dates.start.localTime}"

        Log.d("EventDetailActivity", "Views populated")

        val venue = event.embedded.venues?.firstOrNull()
        findViewById<TextView>(R.id.venueNameTextView).text = venue?.name ?: "Venue not available"
        findViewById<TextView>(R.id.venueAddressTextView).text =
            venue?.address?.line1 ?: "Address not available"
        //findViewById<TextView>(R.id.venueLatitudeTextView).text = "Latitude: ${venue?.location?.latitude ?: "Not available"}"
        //findViewById<TextView>(R.id.venueLongitudeTextView).text = "Longitude: ${venue?.location?.longitude ?: "Not available"}"

        val buyTicketsButton: Button = findViewById(R.id.buyTicketsButton)
        buyTicketsButton.setOnClickListener {
            val eventUrl = event.url
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(eventUrl)
            startActivity(intent)

        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        Log.d("EventDetailActivity", "Map is ready")

        try {
            val venueLocation = event.embedded?.venues?.firstOrNull()?.location
            if (venueLocation != null) {
                val coordinates = LatLng(venueLocation.latitude, venueLocation.longitude)
                googleMap.addMarker(MarkerOptions().position(coordinates).title(event.name))
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 15f))

                Log.d("EventDetailActivity", "Marker added to map: ${venueLocation.latitude}, ${venueLocation.longitude}")
            } else {
                Log.e("EventDetailActivity", "Venue location is null")
            }
        } catch (e: Exception) {
            Log.e("EventDetailActivity", "Error setting up map: ${e.message}")
        }


        // Map UI settings
        googleMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isZoomGesturesEnabled = true
            isScrollGesturesEnabled = true
            isScrollGesturesEnabledDuringRotateOrZoom = true
            isMapToolbarEnabled = true
        }
    }

    // Implement lifecycle methods for mapView
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

}