package com.example.spotagame.data.repository

import com.example.spotagame.data.Event
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class EventRepository {
    private val db = Firebase.firestore
    private val eventsCol = db.collection("events")

    suspend fun createEvent(event: Event): String {
        val doc = eventsCol.document()
        val withId = event.copy(id = doc.id, createdAt = System.currentTimeMillis())
        doc.set(withId).await()
        return doc.id
    }
}