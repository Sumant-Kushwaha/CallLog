package com.amigo.calllog

import android.content.Context
import android.provider.CallLog
import java.time.LocalDate
import java.util.Calendar

object CallLogHelper {
    fun getCallLogs(context: Context): Triple<List<TimeFilter>, List<TimeFilter>, List<TimeFilter>> {
        val missed = mutableListOf<CallLogItem>()
        val received = mutableListOf<CallLogItem>()
        val dialed = mutableListOf<CallLogItem>()

        val projection = arrayOf(
            CallLog.Calls.NUMBER,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.TYPE
        )

        context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            null,
            null,
            null
        )?.use { cursor ->
            val numberIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER)
            val nameIndex = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)
            val typeIndex = cursor.getColumnIndex(CallLog.Calls.TYPE)

            while (cursor.moveToNext()) {
                val number = cursor.getString(numberIndex)
                val name = cursor.getString(nameIndex)
                val type = cursor.getInt(typeIndex)

                val callLogItem = CallLogItem(number, name, type)

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
        // Group calls by number, merging duplicate calls
        val consolidatedCalls = calls
            .groupBy { call -> call.number }
            .map { (_, groupedCalls) ->
                // Merge calls with the same number
                CallLogItem(
                    number = groupedCalls.first().number,
                    name = groupedCalls.first().name,
                    type = groupedCalls.first().type,
                    count = groupedCalls.size
                )
            }

        // Create TimeFilters for the consolidated calls
        return listOf(TimeFilter(LocalDate.now(), consolidatedCalls.sortedBy { it.number }))
    }
}