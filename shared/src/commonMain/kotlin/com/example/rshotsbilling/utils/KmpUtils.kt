package com.example.rshotsbilling.utils

import kotlinx.datetime.*

fun getCurrentTimestamp(): Long {
    return Clock.System.now().toEpochMilliseconds()
}

fun formatTimestamp(timestamp: Long): String {
    val instant = Instant.fromEpochMilliseconds(timestamp)
    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${dateTime.dayOfMonth}/${dateTime.monthNumber}/${dateTime.year} ${dateTime.hour}:${dateTime.minute.toString().padStart(2, '0')}"
}
