package com.triptracker.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.triptracker.core.domain.model.UserRole

/**
 * Simple data class for 4 values
 */
private data class RoleDisplayData(
    val backgroundColor: androidx.compose.ui.graphics.Color,
    val contentColor: androidx.compose.ui.graphics.Color,
    val label: String,
    val subtitle: String
)

/**
 * Visual indicator for driver/passenger role with confidence level
 */
@Composable
fun RoleIndicator(
    role: UserRole,
    confidence: Float,
    size: Dp = 24.dp,
    showLabel: Boolean = true,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, contentColor, label) = when (role) {
        UserRole.DRIVER -> Triple(
            Color(0xFF374151), // Dark grey
            Color.White,
            "Driver"
        )
        UserRole.PASSENGER -> Triple(
            Color(0xFF6B7280), // Medium grey
            Color.White,
            "Passenger"
        )
        UserRole.UNKNOWN -> Triple(
            Color(0xFF9CA3AF), // Light grey
            Color(0xFF0F0F0F),
            "Unknown"
        )
    }

    Row(
        modifier = modifier.semantics {
            contentDescription = "$label role with ${(confidence * 100).toInt()}% confidence"
        },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Role badge with confidence indicator
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            // Background circle
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(backgroundColor)
            )

            // Confidence arc (if confidence < 1.0)
            if (confidence < 1.0f) {
                // Simple confidence indicator using text
                Text(
                    text = "${(confidence * 100).toInt()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor
                )
            } else {
                // Full confidence - show icon or text
                Text(
                    text = label.first().toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }
        }

        // Label text
        if (showLabel) {
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                )
                if (confidence < 1.0f) {
                    Text(
                        text = "${(confidence * 100).toInt()}% confidence",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Compact version for use in lists/cards
 */
@Composable
fun CompactRoleIndicator(
    role: UserRole,
    confidence: Float,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, contentColor) = when (role) {
        UserRole.DRIVER -> Color(0xFF1976D2) to Color.White
        UserRole.PASSENGER -> Color(0xFF388E3C) to Color.White
        UserRole.UNKNOWN -> Color(0xFF757575) to Color.White
    }

    Box(
        modifier = modifier
            .size(16.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .semantics {
                contentDescription = "${role.name.lowercase()} role, ${(confidence * 100).toInt()}% confidence"
            },
        contentAlignment = Alignment.Center
    ) {
        if (confidence >= 0.8f) {
            // High confidence - show checkmark or initial
            Text(
                text = "✓",
                style = MaterialTheme.typography.labelSmall,
                color = contentColor
            )
        }
        // Low confidence - just show color
    }
}

/**
 * Large version for active trip screen
 */
@Composable
fun LargeRoleIndicator(
    role: UserRole,
    confidence: Float,
    modifier: Modifier = Modifier
) {
    val displayData = when (role) {
        UserRole.DRIVER -> RoleDisplayData(
            backgroundColor = Color(0xFF374151),  // Dark grey for Driver
            contentColor = Color.White,
            label = "Driver",
            subtitle = "Safe driving detected"
        )
        UserRole.PASSENGER -> RoleDisplayData(
            backgroundColor = Color(0xFF6B7280),  // Medium grey for Passenger
            contentColor = Color.White,
            label = "Passenger",
            subtitle = "Phone usage detected"
        )
        UserRole.UNKNOWN -> RoleDisplayData(
            backgroundColor = Color(0xFF9CA3AF),  // Light grey for Unknown
            contentColor = Color(0xFF0F0F0F),
            label = "Analyzing",
            subtitle = "Determining role..."
        )
    }

    val backgroundColor = displayData.backgroundColor
    val contentColor = displayData.contentColor
    val label = displayData.label
    val subtitle = displayData.subtitle

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Large circular indicator
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(color = backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Text(
                text = label,
                style = MaterialTheme.typography.headlineSmall,
                color = contentColor,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        }

        // Confidence and subtitle
        androidx.compose.material3.Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (confidence < 1.0f) {
            androidx.compose.material3.Text(
                text = "${(confidence * 100).toInt()}% confidence",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Confidence progress bar
        LinearProgressIndicator(
            progress = confidence,
            modifier = Modifier
                .width(120.dp)
                .height(4.dp),
            color = backgroundColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}
