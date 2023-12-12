package com.example.cuseconnect

import java.io.Serializable

data class AdminGrievance(
    val id: String? = null, // Add this line to include the document ID

    val assignedAdmin: String? = null,
    val description: String? = null,
    val facility: String? = null,
    val images: List<String>? = null,
    val name: String? = null,
    val status: String? = null,
    val subfacility: String? = null,
    val userId: String? = null
): Serializable