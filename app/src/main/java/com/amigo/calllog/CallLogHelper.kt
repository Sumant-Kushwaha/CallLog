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
            CallLog.Calls.TYPE,
            CallLog.Calls.DATE
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

            while (cursor.moveToNext()) {
                val number = cursor.getString(numberIndex)
                val name = cursor.getString(nameIndex)
                val type = cursor.getInt(typeIndex)
                val date = LocalDate.ofEpochDay(cursor.getLong(dateIndex) / 86400000)

                val callLogItem = CallLogItem(number, name, type, date)

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
                Pair(call.date, call.number)
            }
            .map { (key, groupedCalls) ->
                // Merge calls with the same date and number
                CallLogItem(
                    number = groupedCalls.first().number,
                    name = groupedCalls.first().name,
                    type = groupedCalls.first().type,
                    date = groupedCalls.first().date,
                    count = groupedCalls.size
                )
            }

        // Group consolidated calls by date
        val consolidatedGroups = consolidatedCalls
            .groupBy { it.date }

        // Create TimeFilters for each date group
        return consolidatedGroups.map { (date, calls) ->
            TimeFilter(date, calls.sortedBy { it.number })
        }.sortedByDescending { it.date }
    }
}