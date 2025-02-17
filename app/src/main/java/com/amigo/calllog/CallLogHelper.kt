package com.amigo.calllog

import android.content.Context
import android.provider.CallLog
import java.util.Calendar

object CallLogHelper {
    fun getCallLogs(context: Context): Triple<List<TimeFilter>, List<TimeFilter>, List<TimeFilter>> {
        val missed = mutableListOf<CallLogItem>()
        val received = mutableListOf<CallLogItem>()
        val dialed = mutableListOf<CallLogItem>()

        val projection = arrayOf(
            CallLog.Calls.NUMBER,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.TYPE,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION
        )

        context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            null,
            null,
            CallLog.Calls.DATE + " DESC"
        )?.use { cursor ->
            val numberIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER)
            val nameIndex = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)
            val typeIndex = cursor.getColumnIndex(CallLog.Calls.TYPE)
            val dateIndex = cursor.getColumnIndex(CallLog.Calls.DATE)
            val durationIndex = cursor.getColumnIndex(CallLog.Calls.DURATION)

            while (cursor.moveToNext()) {
                val number = cursor.getString(numberIndex)
                val name = cursor.getString(nameIndex)
                val type = cursor.getInt(typeIndex)
                val date = cursor.getLong(dateIndex)
                val duration = cursor.getLong(durationIndex)

                val callLogItem = CallLogItem(number, name, type, date, duration)

                when (type) {
                    CallLog.Calls.MISSED_TYPE -> missed.add(callLogItem)
                    CallLog.Calls.INCOMING_TYPE -> received.add(callLogItem)
                    CallLog.Calls.OUTGOING_TYPE -> dialed.add(callLogItem)
                }
            }
        }

        return Triple(
            groupByTimePeriod(missed),
            groupByTimePeriod(received),
            groupByTimePeriod(dialed)
        )
    }

    private fun groupByTimePeriod(calls: List<CallLogItem>): List<TimeFilter> {
        val calendar = Calendar.getInstance()
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

        // First group by time period
        val groupedByTime = calls.groupBy { call ->
            calendar.timeInMillis = call.date
            when {
                isSameDay(calendar, today) -> "Today"
                isSameDay(calendar, yesterday) -> "Yesterday"
                calendar.get(Calendar.WEEK_OF_YEAR) == today.get(Calendar.WEEK_OF_YEAR) -> "This Week"
                else -> {
                    // Format date as "MMM dd" (e.g., "Feb 17")
                    val month = when(calendar.get(Calendar.MONTH)) {
                        Calendar.JANUARY -> "Jan"
                        Calendar.FEBRUARY -> "Feb"
                        Calendar.MARCH -> "Mar"
                        Calendar.APRIL -> "Apr"
                        Calendar.MAY -> "May"
                        Calendar.JUNE -> "Jun"
                        Calendar.JULY -> "Jul"
                        Calendar.AUGUST -> "Aug"
                        Calendar.SEPTEMBER -> "Sep"
                        Calendar.OCTOBER -> "Oct"
                        Calendar.NOVEMBER -> "Nov"
                        Calendar.DECEMBER -> "Dec"
                        else -> ""
                    }
                    val day = calendar.get(Calendar.DAY_OF_MONTH)
                    "$month $day"
                }
            }
        }

        // Then for each time period, group by number and create consolidated CallLogItems
        val consolidatedGroups = groupedByTime.mapValues { (_, timePeriodCalls) ->
            timePeriodCalls.groupBy { it.number }
                .map { (_, numberCalls) ->
                    // Use the most recent call's data but add count
                    val mostRecent = numberCalls.maxBy { it.date }
                    CallLogItem(
                        number = mostRecent.number,
                        name = mostRecent.name,
                        type = mostRecent.type,
                        date = mostRecent.date,
                        duration = mostRecent.duration,
                        count = numberCalls.size
                    )
                }
        }

        // Sort the dates for older calls
        val olderDates = groupedByTime.keys
            .filter { it !in listOf("Today", "Yesterday", "This Week") }
            .sortedByDescending { date ->
                // Parse the date string back to milliseconds for sorting
                val parts = date.split(" ")
                if (parts.size == 2) {
                    val month = when(parts[0]) {
                        "Jan" -> Calendar.JANUARY
                        "Feb" -> Calendar.FEBRUARY
                        "Mar" -> Calendar.MARCH
                        "Apr" -> Calendar.APRIL
                        "May" -> Calendar.MAY
                        "Jun" -> Calendar.JUNE
                        "Jul" -> Calendar.JULY
                        "Aug" -> Calendar.AUGUST
                        "Sep" -> Calendar.SEPTEMBER
                        "Oct" -> Calendar.OCTOBER
                        "Nov" -> Calendar.NOVEMBER
                        "Dec" -> Calendar.DECEMBER
                        else -> 0
                    }
                    val day = parts[1].toIntOrNull() ?: 1
                    Calendar.getInstance().apply {
                        set(Calendar.MONTH, month)
                        set(Calendar.DAY_OF_MONTH, day)
                    }.timeInMillis
                } else 0L
            }

        return (listOf(
            TimeFilter("Today", consolidatedGroups["Today"] ?: emptyList()),
            TimeFilter("Yesterday", consolidatedGroups["Yesterday"] ?: emptyList()),
            TimeFilter("This Week", consolidatedGroups["This Week"] ?: emptyList())
        ) + olderDates.map { date ->
            TimeFilter(date, consolidatedGroups[date] ?: emptyList())
        }).filter { it.calls.isNotEmpty() }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}