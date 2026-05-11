package com.example.spotagame.data.repository

import com.example.spotagame.data.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val db = Firebase.firestore
    private val userCol = db.collection("users")

    suspend fun createUser(user: User) =
        userCol.document(user.id).set(user).await()

    suspend fun getUser(userId: String): User? =
        userCol.document(userId).get().await().toObject(User::class.java)
}