package com.example.cuseconnect

import com.squareup.moshi.Json
import java.io.Serializable

data class EventResponse(
    @Json(name = "_embedded") val embedded: EmbeddedEvents
)

data class EmbeddedEvents(
    val events: List<Event>
)

data class Event(
    val name: String,
    val type: String,
    val id: String,
    val test: Boolean,
    val url: String,
    val images: List<Image>,
    val sales: Sales,
    val dates: Dates,
    val info: String?, // Add info field
    val priceRanges: List<PriceRange>?, // Add priceRanges field
    @Json(name = "_embedded") val embedded: Embedded

    // Other fields as required...
) : Serializable

data class Embedded(
    val venues: List<Venue>?
) : Serializable

data class Image(
    val ratio: String?,
    val url: String,
    val width: Int,
    val height: Int,
    val fallback: Boolean
) : Serializable

data class Venue(
    val name: String,
    val type: String,
    val id: String,
    val url: String,
    val city: City,
    val country: Country,
    val address: Address?,
    val location: Location
) : Serializable

data class City(
    val name: String
) : Serializable

data class Country(
    val name: String
) : Serializable

data class Location(
    val latitude: Double,
    val longitude: Double
) : Serializable

data class Address(
    val line1: String?
) : Serializable

data class Sales(
    val public: PublicSales,
    // Other fields as required...
) : Serializable

data class PublicSales(
    @Json(name = "startDateTime") val startDateTime: String,
    @Json(name = "endDateTime") val endDateTime: String
    // Other fields as required...
) : Serializable

data class Dates(
    val start: StartDate,
    // Other fields as required...
) : Serializable

data class StartDate(
    @Json(name = "localDate") val localDate: String,
    @Json(name = "localTime") val localTime: String,
    @Json(name = "dateTime") val dateTime: String
    // Other fields as required...
) : Serializable

data class PriceRange(
    val type: String,
    val currency: String,
    @Json(name = "min") val minPrice: Double,
    @Json(name = "max") val maxPrice: Double
    // Add other fields as required...
) : Serializable
