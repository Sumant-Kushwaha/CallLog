package com.amigo.calllog

import android.content.Context
import android.provider.CallLog

object CallLogHelper {
    fun getCallLogs(context: Context): Triple<List<CallLogItem>, List<CallLogItem>, List<CallLogItem>> {
        val missedCalls = mutableListOf<CallLogItem>()
        val receivedCalls = mutableListOf<CallLogItem>()
        val dialedCalls = mutableListOf<CallLogItem>()

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
                    CallLog.Calls.MISSED_TYPE -> missedCalls.add(callLogItem)
                    CallLog.Calls.INCOMING_TYPE -> receivedCalls.add(callLogItem)
                    CallLog.Calls.OUTGOING_TYPE -> dialedCalls.add(callLogItem)
                }
            }
        }

        return Triple(missedCalls, receivedCalls, dialedCalls)
    }
}