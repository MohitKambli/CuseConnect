package com.example.cuseconnect

data class UserGrievance(
    val title: String,
    val description: String,
    val facility: String,
    val subFacility: String,
    val images: List<String>,
    val status: String,
    val feedback: String
)