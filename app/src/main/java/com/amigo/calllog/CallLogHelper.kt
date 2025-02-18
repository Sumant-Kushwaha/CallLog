package com.amigo.calllog

import android.content.Context
import android.provider.CallLog
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
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
        // Group calls by date and number, merging duplicate calls
        val consolidatedCalls = calls
            .groupBy { call ->
                // Group by date and number
                Pair(
                    Instant.ofEpochMilli(call.date)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate(),
                    call.number
                )
            }
            .map { (key, groupedCalls) ->
                // Merge calls with the same date and number
                val mostRecentCall = groupedCalls.maxByOrNull { it.date }!!
                CallLogItem(
                    number = mostRecentCall.number,
                    name = mostRecentCall.name,
                    type = mostRecentCall.type,
                    date = mostRecentCall.date,
                    duration = groupedCalls.sumOf { it.duration },
                    count = groupedCalls.size
                )
            }

        // Group consolidated calls by date
        val consolidatedGroups = consolidatedCalls
            .groupBy { 
                Instant.ofEpochMilli(it.date)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate() 
            }

        // Create TimeFilters for each date group
        return consolidatedGroups.map { (date, calls) ->
            TimeFilter(date, calls.sortedByDescending { it.date })
        }.sortedByDescending { it.date }
    }
}