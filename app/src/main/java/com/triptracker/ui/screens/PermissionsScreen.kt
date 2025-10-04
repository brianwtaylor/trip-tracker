package com.triptracker.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker

/**
 * Individual permission item component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PermissionItem(
    title: String,
    description: String,
    granted: Boolean,
    onRequestClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Granted",
                        tint = Color(0xFF4CAF50), // Green
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "Not granted",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!granted) {
                Button(
                    onClick = onRequestClick,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                ) {
                    Text("Grant Permission")
                }
            }
        }
    }
}

/**
 * Screen for requesting necessary permissions for trip tracking
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsScreen(
    onPermissionsGranted: () -> Unit,
    onPermissionsDenied: () -> Unit,
    isManagementMode: Boolean = false,
    onBackClick: (() -> Unit)? = null
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

            activityRecognitionGranted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACTIVITY_RECOGNITION
            ) == PermissionChecker.PERMISSION_GRANTED
        } else {
            backgroundLocationGranted = locationPermissionGranted
            activityRecognitionGranted = true
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationsGranted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PermissionChecker.PERMISSION_GRANTED
        } else {
            notificationsGranted = true
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

    val content = @Composable {
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
            text = if (isManagementMode) "Manage Permissions" else "Permissions Required",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        if (!isManagementMode) {
            Text(
                text = "Trip Tracker needs these permissions to provide accurate trip tracking and analysis.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

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
        if (isManagementMode) {
            // In management mode, just show a back hint
            Text(
                text = "Use the back button to return to settings",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        } else {
            if (allPermissionsGranted) {
                Button(
                    onClick = onPermissionsGranted,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Continue")
                }
            } else {
                OutlinedButton(
                    onClick = onPermissionsDenied,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Skip for Now")
                }
            }
        }
    }
    }

    // Use Scaffold in management mode, direct content otherwise
    if (isManagementMode) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Manage Permissions") },
                    navigationIcon = {
                        onBackClick?.let { backClick ->
                            IconButton(onClick = backClick) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                content()
            }
        }
    } else {
        content()
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
