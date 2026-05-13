package com.example.spotagame.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.spotagame.CreateEventViewModel
import com.example.spotagame.SaveState
import com.example.spotagame.data.EventLocation
import com.example.spotagame.data.SkillLevel
import com.example.spotagame.data.SportType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: CreateEventViewModel = viewModel()
) {
    val saveState by viewModel.saveState.collectAsState()

    // Read location result set by LocationPickerScreen into this entry's savedStateHandle
    LaunchedEffect(Unit) {
        navController.currentBackStackEntry?.savedStateHandle
            ?.getStateFlow<Double?>("pick_lat", null)
            ?.collect { lat ->
                if (lat != null) {
                    val handle = navController.currentBackStackEntry?.savedStateHandle ?: return@collect
                    val lng = handle.get<Double>("pick_lng") ?: return@collect
                    val name = handle.get<String>("pick_name") ?: ""
                    viewModel.location = EventLocation(lat, lng, name)
                    handle.remove<Double>("pick_lat")
                    handle.remove<Double>("pick_lng")
                    handle.remove<String>("pick_name")
                }
            }
    }

    LaunchedEffect(saveState) {
        if (saveState is SaveState.Success) {
            viewModel.resetSaveState()
            navController.popBackStack()
        }
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = viewModel.dateTimeMs)

    val dateDisplay = remember(viewModel.dateTimeMs) {
        SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(Date(viewModel.dateTimeMs))
    }
    val timeDisplay = remember(viewModel.dateTimeMs) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(viewModel.dateTimeMs))
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedMs ->
                        val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                            timeInMillis = selectedMs
                        }
                        val currentCal = Calendar.getInstance().apply {
                            timeInMillis = viewModel.dateTimeMs
                        }
                        viewModel.dateTimeMs = Calendar.getInstance().apply {
                            set(Calendar.YEAR, utcCal.get(Calendar.YEAR))
                            set(Calendar.MONTH, utcCal.get(Calendar.MONTH))
                            set(Calendar.DAY_OF_MONTH, utcCal.get(Calendar.DAY_OF_MONTH))
                            set(Calendar.HOUR_OF_DAY, currentCal.get(Calendar.HOUR_OF_DAY))
                            set(Calendar.MINUTE, currentCal.get(Calendar.MINUTE))
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val cal = Calendar.getInstance().apply { timeInMillis = viewModel.dateTimeMs }
        val timePickerState = rememberTimePickerState(
            initialHour = cal.get(Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(Calendar.MINUTE)
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.dateTimeMs = Calendar.getInstance().apply {
                        timeInMillis = viewModel.dateTimeMs
                        set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        set(Calendar.MINUTE, timePickerState.minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("New Event", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = viewModel.title,
            onValueChange = { viewModel.title = it },
            label = { Text("Title") },
            placeholder = { Text("e.g. Sunday 5-a-side") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Text("Sport", style = MaterialTheme.typography.labelLarge)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SportType.entries.forEach { type ->
                FilterChip(
                    selected = viewModel.sport == type,
                    onClick = { viewModel.sport = type },
                    label = { Text(type.displayName) }
                )
            }
        }

        Text("Skill Level", style = MaterialTheme.typography.labelLarge)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SkillLevel.entries.forEach { level ->
                FilterChip(
                    selected = viewModel.skillLevel == level,
                    onClick = { viewModel.skillLevel = level },
                    label = { Text(level.displayName) }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val dateSource = remember { MutableInteractionSource() }
            val datePressedState by dateSource.collectIsPressedAsState()
            LaunchedEffect(datePressedState) { if (datePressedState) showDatePicker = true }

            OutlinedTextField(
                value = dateDisplay,
                onValueChange = {},
                label = { Text("Date") },
                readOnly = true,
                modifier = Modifier.weight(1f),
                trailingIcon = { Icon(Icons.Default.DateRange, null) },
                interactionSource = dateSource
            )

            val timeSource = remember { MutableInteractionSource() }
            val timePressedState by timeSource.collectIsPressedAsState()
            LaunchedEffect(timePressedState) { if (timePressedState) showTimePicker = true }

            OutlinedTextField(
                value = timeDisplay,
                onValueChange = {},
                label = { Text("Time") },
                readOnly = true,
                modifier = Modifier.weight(1f),
                trailingIcon = { Icon(Icons.Default.Schedule, null) },
                interactionSource = timeSource
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = viewModel.durationMins,
                onValueChange = { viewModel.durationMins = it.filter(Char::isDigit) },
                label = { Text("Duration (min)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            OutlinedTextField(
                value = viewModel.playerLimit,
                onValueChange = { viewModel.playerLimit = it.filter(Char::isDigit) },
                label = { Text("Player limit") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        }

        OutlinedTextField(
            value = viewModel.description,
            onValueChange = { viewModel.description = it },
            label = { Text("Description (optional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        val locationSource = remember { MutableInteractionSource() }
        val locationPressedState by locationSource.collectIsPressedAsState()
        LaunchedEffect(locationPressedState) {
            if (locationPressedState) navController.navigate("location_picker")
        }

        OutlinedTextField(
            value = viewModel.location?.name ?: "",
            onValueChange = {},
            label = { Text("Location") },
            placeholder = { Text("Tap to pick on map") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = { Icon(Icons.Default.LocationOn, null) },
            interactionSource = locationSource
        )

        if (saveState is SaveState.Error) {
            Text(
                text = (saveState as SaveState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        val isValid = viewModel.title.isNotBlank() &&
                viewModel.location != null &&
                viewModel.durationMins.toIntOrNull() != null &&
                viewModel.playerLimit.toIntOrNull() != null

        Button(
            onClick = { viewModel.save() },
            enabled = isValid && saveState !is SaveState.Loading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        ) {
            if (saveState is SaveState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Create Event")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
