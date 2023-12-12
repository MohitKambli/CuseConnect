package com.example.cuseconnect

import org.json.JSONArray

data class Restaurant(
    val name: String,
    val rating: Float,
    val price: String,
    val image: String,
    val address: List<String>,
    val phoneNumber: String,
    val latitude: Double,
    val longitude: Double,
    val url: String
)
