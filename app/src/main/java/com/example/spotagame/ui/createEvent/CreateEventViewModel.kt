package com.example.spotagame.ui.createEvent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotagame.data.Event
import com.example.spotagame.data.EventLocation
import com.example.spotagame.data.SkillLevel
import com.example.spotagame.data.SportType
import com.example.spotagame.data.repository.EventRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateEventFormState(
    val title:      String          = "",
    val sport:      SportType       = SportType.OTHER,
    val skillLevel: SkillLevel      = SkillLevel.ALL,
    val location:   EventLocation?  = null,
    val dateTime:   Long?           = null,
    val durationMins: String        = "60",
    val playerLimit: String         = "10",
    val description: String         = "",

    val titleError: String?         = null,
    val locationError:  String?     = null,
    val dateTimeError:  String?     = null,
    val durationError:  String?     = null,
    val playerLimitError: String?   = null
)

sealed class CreateEventUiEvent {
    object Success: CreateEventUiEvent()
    data class Error(val message: String): CreateEventUiEvent()
}

class CreateEventViewModel : ViewModel() {

    private val repository = EventRepository()

    private val _formState = MutableStateFlow(CreateEventFormState())
    val formState = _formState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<CreateEventUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun onTitleChange(value: String) =
        _formState.update { it.copy(title = value, titleError = null) }

    fun onSportChange(value: SportType) =
        _formState.update { it.copy(sport = value) }

    fun onSkillLevelChange(value: SkillLevel) =
        _formState.update { it.copy(skillLevel = value) }

    fun onLocationPicked(value: EventLocation) =
        _formState.update { it.copy(location = value, locationError = null) }

    fun onDateTimePicked(value: Long) =
        _formState.update { it.copy(dateTime = value, dateTimeError = null) }

    fun onDurationChange(value: String) =
        _formState.update { it.copy(durationMins = value, durationError = null) }

    fun onPlayerLimitChange(value: String) =
        _formState.update { it.copy(playerLimit = value, playerLimitError = null) }

    fun onDescriptionChange(value: String) =
        _formState.update { it.copy(description = value) }

    private fun validate(state: CreateEventFormState): CreateEventFormState {
        return state.copy(
            titleError = if (state.title.isBlank()) "Title is required" else null,
            locationError = if (state.location == null) "Please pick a location" else null,
            dateTimeError = if (state.dateTime == null) "Please pick a date and time"
            else if (state.dateTime < System.currentTimeMillis()) "Event must be in the future"
            else null,
            durationError = when {
                state.durationMins.isBlank() -> "Required"
                state.durationMins.toIntOrNull() == null -> "Must be a number"
                state.durationMins.toInt() < 15 -> "Minimum 15 minutes"
                state.durationMins.toInt() > 480 -> "Maximum 8 hours"
                else -> null
            },
            playerLimitError = when {
                state.playerLimit.isBlank() -> "Required"
                state.playerLimit.toIntOrNull() == null -> "Must be a number"
                state.playerLimit.toInt() < 2 -> "Minimum 2 players"
                state.playerLimit.toInt() > 100 -> "Maximum 100 players"
                else -> null
            }
        )
    }

    fun submit() {
        val validated = validate(_formState.value)
        _formState.value = validated

        val hasErrors = listOf(
            validated.titleError, validated.locationError, validated.dateTimeError,
            validated.durationError, validated.playerLimitError
        ).any { it != null }

        if (hasErrors) return

        viewModelScope.launch {
            try {
                val uid  = Firebase.auth.currentUser?.uid ?: return@launch
                val user = Firebase.auth.currentUser!!

                val event = Event(
                    title = validated.title.trim(),
                    sport = validated.sport.name,
                    skillLevel = validated.skillLevel.name,
                    location = validated.location!!,
                    dateTime = validated.dateTime!!,
                    durationMins = validated.durationMins.toInt(),
                    hostedBy = uid,
                    attendees = listOf(uid),  // host auto-joins
                    playerLimit = validated.playerLimit.toInt(),
                    description = validated.description.trim()
                )
                repository.createEvent(event)
                _uiEvent.emit(CreateEventUiEvent.Success)

            } catch (e: Exception) {
                _uiEvent.emit(CreateEventUiEvent.Error(e.message ?: "Failed to create event"))
            }
        }
    }
}