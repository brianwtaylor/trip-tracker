package com.triptracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Settings screen providing app configuration and permission management
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onPermissionsClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Permissions Section - Most Important
            item {
                SettingsSection(title = "Privacy & Permissions")
            }

            item {
                PermissionManagementCard(onPermissionsClick = onPermissionsClick)
            }

            // App Settings
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSection(title = "App Settings")
            }

            items(appSettings) { setting ->
                SettingItem(
                    icon = setting.icon,
                    title = setting.title,
                    subtitle = setting.subtitle,
                    onClick = setting.onClick
                )
            }

            // About Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSection(title = "About")
            }

            items(aboutItems) { item ->
                SettingItem(
                    icon = item.icon,
                    title = item.title,
                    subtitle = item.subtitle,
                    onClick = item.onClick
                )
            }
        }
    }
}

/**
 * Permission management card - highlighted for importance
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PermissionManagementCard(onPermissionsClick: () -> Unit) {
    Card(
        onClick = onPermissionsClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Manage Permissions",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Control location, activity recognition, and notification permissions",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }

            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = "Navigate to permissions",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Section header
 */
@Composable
private fun SettingsSection(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

/**
 * Individual setting item
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Data classes for settings
private data class SettingItemData(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val onClick: () -> Unit
)

// App settings data
private val appSettings = listOf(
    SettingItemData(
        icon = Icons.Filled.Settings,
        title = "Theme",
        subtitle = "Light, Dark, or System default",
        onClick = { /* TODO: Implement theme selection */ }
    ),
    SettingItemData(
        icon = Icons.Filled.Notifications,
        title = "Notifications",
        subtitle = "Trip alerts and reminders",
        onClick = { /* TODO: Implement notification settings */ }
    ),
    SettingItemData(
        icon = Icons.Filled.List,
        title = "Data Management",
        subtitle = "Export trips, clear data",
        onClick = { /* TODO: Implement data management */ }
    )
)

// About items data
private val aboutItems = listOf(
    SettingItemData(
        icon = Icons.Filled.Info,
        title = "About Trip Tracker",
        subtitle = "Version 1.0.0",
        onClick = { /* TODO: Show about dialog */ }
    ),
    SettingItemData(
        icon = Icons.Filled.Info,
        title = "Help & Support",
        subtitle = "FAQs and contact support",
        onClick = { /* TODO: Show help screen */ }
    ),
    SettingItemData(
        icon = Icons.Filled.Build,
        title = "Report Issue",
        subtitle = "Send feedback or report bugs",
        onClick = { /* TODO: Show feedback form */ }
    )
)
