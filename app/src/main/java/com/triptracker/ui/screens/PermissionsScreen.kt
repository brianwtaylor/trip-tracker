package com.triptracker.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.triptracker.R

/**
 * Screen for requesting necessary permissions for trip tracking
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsScreen(
    onPermissionsGranted: () -> Unit,
    onPermissionsDenied: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Track permission states
    var locationPermissionGranted by remember { mutableStateOf(false) }
    var backgroundLocationGranted by remember { mutableStateOf(false) }
    var activityRecognitionGranted by remember { mutableStateOf(false) }
    var notificationsGranted by remember { mutableStateOf(false) }

    // Check initial permission states
    LaunchedEffect(Unit) {
        locationPermissionGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PermissionChecker.PERMISSION_GRANTED

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            backgroundLocationGranted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PermissionChecker.PERMISSION_GRANTED
        } else {
            backgroundLocationGranted = true // Not required on older versions
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activityRecognitionGranted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACTIVITY_RECOGNITION
            ) == PermissionChecker.PERMISSION_GRANTED
        } else {
            activityRecognitionGranted = true // Not required on older versions
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationsGranted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PermissionChecker.PERMISSION_GRANTED
        } else {
            notificationsGranted = true // Not required on older versions
        }
    }

    // Permission launchers
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        locationPermissionGranted = granted
        if (granted && allPermissionsGranted(
                locationPermissionGranted,
                backgroundLocationGranted,
                activityRecognitionGranted,
                notificationsGranted
            )
        ) {
            onPermissionsGranted()
        }
    }

    val backgroundLocationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        backgroundLocationGranted = granted
        if (granted && allPermissionsGranted(
                locationPermissionGranted,
                backgroundLocationGranted,
                activityRecognitionGranted,
                notificationsGranted
            )
        ) {
            onPermissionsGranted()
        }
    }

    val activityRecognitionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        activityRecognitionGranted = granted
        if (granted && allPermissionsGranted(
                locationPermissionGranted,
                backgroundLocationGranted,
                activityRecognitionGranted,
                notificationsGranted
            )
        ) {
            onPermissionsGranted()
        }
    }

    val notificationsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationsGranted = granted
        if (granted && allPermissionsGranted(
                locationPermissionGranted,
                backgroundLocationGranted,
                activityRecognitionGranted,
                notificationsGranted
            )
        ) {
            onPermissionsGranted()
        }
    }

    // Check if all required permissions are granted
    val allPermissionsGranted = allPermissionsGranted(
        locationPermissionGranted,
        backgroundLocationGranted,
        activityRecognitionGranted,
        notificationsGranted
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "Permissions Required",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Trip Tracker needs these permissions to provide accurate driver/passenger detection and trip tracking.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Location Permission
        PermissionItem(
            title = "Location Access",
            description = "Required to track your trip route and provide accurate location data.",
            granted = locationPermissionGranted,
            onRequestClick = {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        )

        // Background Location (Android 10+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            PermissionItem(
                title = "Background Location",
                description = "Allows trip tracking even when the app is not in use.",
                granted = backgroundLocationGranted,
                onRequestClick = {
                    backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }
            )
        }

        // Activity Recognition (Android 10+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            PermissionItem(
                title = "Physical Activity",
                description = "Enables driver/passenger detection based on phone movement patterns.",
                granted = activityRecognitionGranted,
                onRequestClick = {
                    activityRecognitionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                }
            )
        }

        // Notifications (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionItem(
                title = "Notifications",
                description = "Shows trip tracking status and alerts.",
                granted = notificationsGranted,
                onRequestClick = {
                    notificationsLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        if (allPermissionsGranted) {
            Button(
                onClick = onPermissionsGranted,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue to Trip Tracker")
            }
        } else {
            OutlinedButton(
                onClick = {
                    // Open app settings
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Open App Settings")
            }

            TextButton(
                onClick = onPermissionsDenied
            ) {
                Text("Skip for Now")
            }
        }
    }
}

@Composable
private fun PermissionItem(
    title: String,
    description: String,
    granted: Boolean,
    onRequestClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (granted)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                if (granted) {
                    Text(
                        text = "✓ Granted",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Text(
                        text = "Required",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!granted) {
                Button(
                    onClick = onRequestClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Grant Permission")
                }
            }
        }
    }
}

private fun allPermissionsGranted(
    location: Boolean,
    backgroundLocation: Boolean,
    activityRecognition: Boolean,
    notifications: Boolean
): Boolean {
    return location && backgroundLocation && activityRecognition && notifications
}
