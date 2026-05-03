package com.example.spotagame.data

import androidx.annotation.Keep

@Keep
data class Event(
    val id:           String        = "",           // Firestore document ID
    val title:        String        = "",           // e.g. "Sunday 5-a-side"
    val sport:        String        = "OTHER",      // SportType.name
    val skillLevel:   String        = "ALL",        // SkillLevel.name
    val location:     EventLocation = EventLocation(),
    val dateTime:     Long          = 0L,           // Unix timestamp (ms)
    val durationMins: Int           = 60,
    val hostedBy:     String        = "",           // User.id
    val attendees:    List<String>  = emptyList(),  // List of User.id
    val playerLimit:  Int           = 10,
    val description:  String        = "",
    val createdAt:    Long          = 0L,
    val isActive:     Boolean       = true          // false = cancelled
) {
    // Computed helpers — NOT stored in Firestore
    val spotsLeft: Int     get() = playerLimit - attendees.size
    val isFull:    Boolean get() = attendees.size >= playerLimit
    fun isAttending(userId: String) = userId in attendees
}
