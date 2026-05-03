package com.example.spotagame.data

// Here we can add custom drawables to represent each sport

enum class SportType(val displayName: String) {
    FOOTBALL  ("Football"),
    BASKETBALL("Basketball"),
    TENNIS    ("Tennis"),
    VOLLEYBALL("Volleyball"),
    RUNNING   ("Running"),
    CYCLING   ("Cycling"),
    SWIMMING  ("Swimming"),
    OTHER     ("Other");

    companion object {
        fun fromString(value: String) =
            entries.firstOrNull { it.name == value } ?: OTHER
    }
}