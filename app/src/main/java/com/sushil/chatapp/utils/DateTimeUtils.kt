package com.sushil.chatapp.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateTimeUtils {

    fun formatTimestamp(timestamp: Long): String {
        val now = Calendar.getInstance()
        val messageTime = Calendar.getInstance().apply {
            timeInMillis = timestamp
        }

        val dateFormat = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault())

        return when {
            isSameDay(now, messageTime) -> "Today"
            isYesterday(now, messageTime) -> "Yesterday"
            else -> dateFormat.format(Date(timestamp))
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isYesterday(today: Calendar, messageTime: Calendar): Boolean {
        val yesterday = Calendar.getInstance().apply {
            timeInMillis = today.timeInMillis
            add(Calendar.DAY_OF_YEAR, -1)
        }
        return isSameDay(yesterday, messageTime)
    }

    fun formatTimestampToTime(timestamp: Long): String {
        val date = Date(timestamp)
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        return sdf.format(date)
    }

}