package com.example.spotagame.data

enum class SkillLevel(val displayName: String, val description: String) {
    BEGINNER    ("Beginner",     "First time or casual"),
    INTERMEDIATE("Intermediate", "Play regularly"),
    ADVANCED    ("Advanced",     "Competitive level"),
    ALL         ("All welcome",  "Any skill level");

    companion object {
        fun fromString(value: String) =
            entries.firstOrNull { it.name == value } ?: ALL
    }
}