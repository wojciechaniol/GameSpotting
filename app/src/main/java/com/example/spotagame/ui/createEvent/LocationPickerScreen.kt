package com.example.spotagame.ui.createEvent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.spotagame.data.EventLocation
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun LocationPickerScreen(
    onLocationConfirmed: (EventLocation) -> Unit,
    onBack: () -> Unit
) {
    var markerPosition by remember { mutableStateOf<LatLng?>(null) }
    var locationName   by remember { mutableStateOf("") }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(51.5074, -0.1278), 10f) // London
    }

    Column(Modifier.fillMaxSize()) {
        GoogleMap(
            modifier             = Modifier.weight(1f),
            cameraPositionState  = cameraPositionState,
            onMapClick           = { latLng -> markerPosition = latLng }
        ) {
            markerPosition?.let {
                Marker(state = MarkerState(position = it))
            }
        }

        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (markerPosition != null) {
                OutlinedTextField(
                    value         = locationName,
                    onValueChange = { locationName = it },
                    label         = { Text("Venue name (e.g. Hackney Marshes, Pitch 3)") },
                    modifier      = Modifier.fillMaxWidth()
                )
                Button(
                    onClick  = {
                        val pos = markerPosition ?: return@Button
                        onLocationConfirmed(
                            EventLocation(
                                latitude  = pos.latitude,
                                longitude = pos.longitude,
                                name      = locationName.ifBlank { "Custom location" }
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Confirm location")
                }
            } else {
                Text(
                    "Tap anywhere on the map to drop a pin",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}