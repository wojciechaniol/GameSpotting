package com.example.spotagame.data

import androidx.annotation.Keep

@Keep
data class User(
    val id:              String       = "",  // Firebase Auth UID
    val displayName:     String       = "",
    val photoUrl:        String       = "",
    val bio:             String       = "",
    val preferredSports: List<String> = emptyList(), // List of SportType.name
    val createdAt:       Long         = 0L
)
