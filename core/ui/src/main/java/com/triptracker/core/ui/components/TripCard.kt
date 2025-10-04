package com.triptracker.core.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.triptracker.core.domain.model.UserRole

/**
 * Data class for trip card display
 */
data class TripItem(
    val id: String,
    val date: String,
    val time: String,
    val duration: String,
    val distance: String,
    val averageSpeed: String,
    val role: UserRole,
    val confidence: Float,
    val locationCount: Int
)

/**
 * Card component displaying trip summary information
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripCard(
    trip: TripItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Trip on ${trip.date} at ${trip.time}, " +
                        "duration ${trip.duration}, distance ${trip.distance}, " +
                        "role ${trip.role.name.lowercase()}"
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row: Date/Time and Role
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = trip.date,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                    )
                    Text(
                        text = trip.time,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                RoleIndicator(
                    role = trip.role,
                    confidence = trip.confidence,
                    size = 32.dp,
                    showLabel = false
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Statistics row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                TripStat(
                    label = "Distance",
                    value = trip.distance,
                    modifier = Modifier.weight(1f)
                )
                TripStat(
                    label = "Duration",
                    value = trip.duration,
                    modifier = Modifier.weight(1f)
                )
                TripStat(
                    label = "Avg Speed",
                    value = trip.averageSpeed,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer: Location count and role confidence
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${trip.locationCount} locations",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (trip.role != com.triptracker.core.domain.model.UserRole.UNKNOWN) {
                androidx.compose.material3.Text(
                    text = "${(trip.confidence * 100).toInt()}% confident",
                    style = MaterialTheme.typography.bodySmall,
                        color = when (trip.role) {
                            com.triptracker.core.domain.model.UserRole.DRIVER -> MaterialTheme.colorScheme.primary
                            com.triptracker.core.domain.model.UserRole.PASSENGER -> MaterialTheme.colorScheme.secondary
                            com.triptracker.core.domain.model.UserRole.UNKNOWN -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

/**
 * Individual statistic display component
 */
@Composable
private fun TripStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {
        androidx.compose.material3.Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        androidx.compose.material3.Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

