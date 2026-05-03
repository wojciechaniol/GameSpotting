package com.example.spotagame.data

import androidx.annotation.Keep
import com.google.android.gms.maps.model.LatLng

@Keep
data class EventLocation(
    val latitude:  Double = 0.0,
    val longitude: Double = 0.0,
    val name:      String = ""   // e.g. "Hackney Marshes, Pitch 3"
) {
    // Pass directly into Maps Compose Marker
    fun toLatLng() = LatLng(latitude, longitude)
}
