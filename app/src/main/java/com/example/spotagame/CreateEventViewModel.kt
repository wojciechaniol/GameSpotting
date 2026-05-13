package com.example.spotagame

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotagame.data.Event
import com.example.spotagame.data.EventLocation
import com.example.spotagame.data.SkillLevel
import com.example.spotagame.data.SportType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class SaveState {
    object Idle : SaveState()
    object Loading : SaveState()
    object Success : SaveState()
    data class Error(val message: String) : SaveState()
}

class CreateEventViewModel : ViewModel() {

    var title by mutableStateOf("")
    var sport by mutableStateOf(SportType.FOOTBALL)
    var skillLevel by mutableStateOf(SkillLevel.ALL)
    var dateTimeMs by mutableStateOf(System.currentTimeMillis())
    var durationMins by mutableStateOf("60")
    var playerLimit by mutableStateOf("10")
    var description by mutableStateOf("")
    var location by mutableStateOf<EventLocation?>(null)

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    fun save() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val loc = location ?: return
        val dur = durationMins.toIntOrNull() ?: return
        val limit = playerLimit.toIntOrNull() ?: return
        if (title.isBlank()) return

        viewModelScope.launch {
            _saveState.value = SaveState.Loading
            try {
                val event = Event(
                    title = title.trim(),
                    sport = sport.name,
                    skillLevel = skillLevel.name,
                    location = loc,
                    dateTime = dateTimeMs,
                    durationMins = dur,
                    hostedBy = uid,
                    playerLimit = limit,
                    description = description.trim(),
                    createdAt = System.currentTimeMillis(),
                    isActive = true
                )
                val ref = FirebaseFirestore.getInstance()
                    .collection("events")
                    .add(event)
                    .await()
                ref.update("id", ref.id).await()
                _saveState.value = SaveState.Success
            } catch (e: Exception) {
                _saveState.value = SaveState.Error(e.message ?: "Failed to save event")
            }
        }
    }

    fun resetSaveState() {
        _saveState.value = SaveState.Idle
    }
}
