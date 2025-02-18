package com.amigo.calllog

import android.content.Context
import android.content.pm.PackageManager
import android.provider.CallLog
import androidx.core.content.ContextCompat
import java.time.LocalDate
import java.util.Calendar

object CallLogHelper {
    fun getCallLogs(context: Context): Triple<List<TimeFilter>, List<TimeFilter>, List<TimeFilter>> {
        // Check and request permissions if not granted
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CALL_LOG)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return Triple(emptyList(), emptyList(), emptyList())
        }

        // Fetch call logs from the system
        val callLogs = context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            null,
            null,
            null,
            CallLog.Calls.DATE + " DESC"
        )?.use { cursor ->
            val numberIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER)
            val nameIndex = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)
            val typeIndex = cursor.getColumnIndex(CallLog.Calls.TYPE)
            val dateIndex = cursor.getColumnIndex(CallLog.Calls.DATE)

            val callLogItems = mutableListOf<CallLogItem>()

            while (cursor.moveToNext()) {
                val number = cursor.getString(numberIndex) ?: continue
                val name = cursor.getString(nameIndex)
                val type = cursor.getInt(typeIndex)
                val date = LocalDate.ofEpochDay(cursor.getLong(dateIndex) / 86400000)

                callLogItems.add(
                    CallLogItem(
                        number = number,
                        name = name,
                        type = type,
                        date = date
                    )
                )
            }
            callLogItems
        } ?: emptyList()

        // Group and filter call logs
        val missedCalls = callLogs
            .filter { it.type == CallLogItem.MISSED_CALL_TYPE }
            .let { groupByTimePeriod(it) }

        val receivedCalls = callLogs
            .filter { it.type == CallLogItem.RECEIVED_CALL_TYPE }
            .let { groupByTimePeriod(it) }

        val dialedCalls = callLogs
            .filter { it.type == CallLogItem.DIALED_CALL_TYPE }
            .let { groupByTimePeriod(it) }

        return Triple(missedCalls, receivedCalls, dialedCalls)
    }

    private fun groupByTimePeriod(calls: List<CallLogItem>): List<TimeFilter> {
        // Group calls by date and number
        val consolidatedCalls = calls
            .groupBy { call -> 
                Pair(call.date, call.number)
            }
            .map { (_, groupedCalls) ->
                // Merge calls with the same date and number, keeping track of total count
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

        // Create TimeFilters for each date group, preserving original order
        return consolidatedGroups.map { (date, calls) ->
            TimeFilter(date, calls)
        }
    }
}