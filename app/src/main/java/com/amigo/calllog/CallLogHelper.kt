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

        val grouped = calls.groupBy { call ->
            calendar.timeInMillis = call.date
            when {
                isSameDay(calendar, today) -> "Today"
                isSameDay(calendar, yesterday) -> "Yesterday"
                calendar.get(Calendar.WEEK_OF_YEAR) == today.get(Calendar.WEEK_OF_YEAR) -> "This Week"
                else -> "Older"
            }
        }

        return listOf(
            TimeFilter("Today", grouped["Today"] ?: emptyList()),
            TimeFilter("Yesterday", grouped["Yesterday"] ?: emptyList()),
            TimeFilter("This Week", grouped["This Week"] ?: emptyList()),
            TimeFilter("Older", grouped["Older"] ?: emptyList())
        ).filter { it.calls.isNotEmpty() }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}