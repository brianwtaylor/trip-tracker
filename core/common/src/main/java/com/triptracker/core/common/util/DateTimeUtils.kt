package com.triptracker.core.common.util

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object DateTimeUtils {
    
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault())
    
    /**
     * Format timestamp to date string (e.g., "Jan 15, 2024")
     */
    fun formatDate(timestampMillis: Long): String {
        return dateFormat.format(Date(timestampMillis))
    }
    
    /**
     * Format timestamp to time string (e.g., "3:45 PM")
     */
    fun formatTime(timestampMillis: Long): String {
        return timeFormat.format(Date(timestampMillis))
    }
    
    /**
     * Format timestamp to date and time string (e.g., "Jan 15, 2024 at 3:45 PM")
     */
    fun formatDateTime(timestampMillis: Long): String {
        return dateTimeFormat.format(Date(timestampMillis))
    }
    
    /**
     * Format duration in milliseconds to readable string
     * Examples: "5m", "1h 23m", "2h 15m"
     */
    fun formatDuration(durationMillis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(durationMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis) % 60
        
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "${seconds}s"
        }
    }
    
    /**
     * Format duration with seconds (e.g., "1:23:45")
     */
    fun formatDurationDetailed(durationMillis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(durationMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis) % 60
        
        return when {
            hours > 0 -> "%d:%02d:%02d".format(hours, minutes, seconds)
            else -> "%d:%02d".format(minutes, seconds)
        }
    }
    
    /**
     * Get relative time string (e.g., "Today", "Yesterday", "2 days ago")
     */
    fun getRelativeTime(timestampMillis: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestampMillis
        
        val days = TimeUnit.MILLISECONDS.toDays(diff)
        
        return when {
            days == 0L -> "Today"
            days == 1L -> "Yesterday"
            days < 7 -> "$days days ago"
            days < 30 -> "${days / 7} weeks ago"
            else -> formatDate(timestampMillis)
        }
    }
}
