package com.example.spotagame.ui.createEvent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spotagame.data.EventLocation
import com.example.spotagame.data.SkillLevel
import com.example.spotagame.data.SportType
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CreateEventScreen(
    onEventCreated: () -> Unit = {},     // What happens after successful creation of an event?
    onPickLocation: () -> Unit,          // navigates to LocationPickerScreen
    pickedLocation: EventLocation? = null, // returned from LocationPickerScreen
    viewModel: CreateEventViewModel = viewModel()
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    var showDateTimePicker by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Pass picked location back into ViewModel when it arrives
    LaunchedEffect(pickedLocation) {
        pickedLocation?.let { viewModel.onLocationPicked(it) }
    }

    // Handle one-shot events (success / error)
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is CreateEventUiEvent.Success ->
                    onEventCreated()
                is CreateEventUiEvent.Error   ->
                    scope.launch { snackbarHostState.showSnackbar(event.message) }
            }
        }
    }

    if (showDateTimePicker) {
        DateTimePickerDialog(
            onDateTimeSelected = { viewModel.onDateTimePicked(it); showDateTimePicker = false },
            onDismiss          = { showDateTimePicker = false }
        )
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        LazyColumn(
            contentPadding     = padding,
            modifier           = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title
            item {
                OutlinedTextField(
                    value         = formState.title,
                    onValueChange = viewModel::onTitleChange,
                    label         = { Text("Title") },
                    isError       = formState.titleError != null,
                    supportingText = formState.titleError?.let { { Text(it) } },
                    modifier      = Modifier.fillMaxWidth()
                )
            }

            // Sport dropdown
            item { SportDropdown(formState.sport, viewModel::onSportChange) }

            // Skill level dropdown
            item { SkillLevelDropdown(formState.skillLevel, viewModel::onSkillLevelChange) }

            // Location picker button
            item {
                OutlinedButton(
                    onClick  = onPickLocation,
                    modifier = Modifier.fillMaxWidth(),
                    colors   = if (formState.locationError != null)
                        ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    else ButtonDefaults.outlinedButtonColors()
                ) {
                    Text(formState.location?.name ?: "Pick location on map")
                }
                formState.locationError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall)
                }
            }

            // Date & time picker button
            item {
                OutlinedButton(
                    onClick  = { showDateTimePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val label = formState.dateTime?.let {
                        SimpleDateFormat("EEE dd MMM, HH:mm", Locale.getDefault()).format(Date(it))
                    } ?: "Pick date & time"
                    Text(label)
                }
                formState.dateTimeError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall)
                }
            }

            // Duration
            item {
                OutlinedTextField(
                    value         = formState.durationMins,
                    onValueChange = viewModel::onDurationChange,
                    label         = { Text("Duration (minutes)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError        = formState.durationError != null,
                    supportingText = formState.durationError?.let { { Text(it) } },
                    modifier       = Modifier.fillMaxWidth()
                )
            }

            // Player limit
            item {
                OutlinedTextField(
                    value         = formState.playerLimit,
                    onValueChange = viewModel::onPlayerLimitChange,
                    label         = { Text("Player limit") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError        = formState.playerLimitError != null,
                    supportingText = formState.playerLimitError?.let { { Text(it) } },
                    modifier       = Modifier.fillMaxWidth()
                )
            }

            // Description
            item {
                OutlinedTextField(
                    value         = formState.description,
                    onValueChange = viewModel::onDescriptionChange,
                    label         = { Text("Description (optional)") },
                    minLines      = 3,
                    modifier      = Modifier.fillMaxWidth()
                )
            }

            // Submit
            item {
                Button(
                    onClick  = viewModel::submit,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Create Event")
                }
            }
        }
    }
}

// --- Dropdown helpers ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SportDropdown(selected: SportType, onSelected: (SportType) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value            = selected.displayName,
            onValueChange    = {},
            readOnly         = true,
            label            = { Text("Sport") },
            trailingIcon     = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier         = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            SportType.entries.forEach { sport ->
                DropdownMenuItem(
                    text    = { Text(sport.displayName) },
                    onClick = { onSelected(sport); expanded = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillLevelDropdown(selected: SkillLevel, onSelected: (SkillLevel) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value         = selected.displayName,
            onValueChange = {},
            readOnly      = true,
            label         = { Text("Skill level") },
            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier      = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            SkillLevel.entries.forEach { level ->
                DropdownMenuItem(
                    text    = { Text("${level.displayName} — ${level.description}") },
                    onClick = { onSelected(level); expanded = false }
                )
            }
        }
    }
}