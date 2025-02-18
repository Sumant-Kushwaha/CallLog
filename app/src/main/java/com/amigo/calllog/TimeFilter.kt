package com.amigo.calllog

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.Parceler
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Parcelize
data class TimeFilter(
    val date: LocalDate,
    val calls: List<CallLogItem>
) : Parcelable {
    val title: String = when {
        date == LocalDate.now() -> "Today"
        date == LocalDate.now().minusDays(1) -> "Yesterday"
        else -> date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))
    }

    companion object : Parceler<TimeFilter> {
        override fun TimeFilter.write(parcel: Parcel, flags: Int) {
            parcel.writeSerializable(date)
            parcel.writeInt(calls.size)
            calls.forEach { parcel.writeParcelable(it, flags) }
        }

        override fun create(parcel: Parcel): TimeFilter {
            val date = parcel.readSerializable() as LocalDate
            val size = parcel.readInt()
            val calls = ArrayList<CallLogItem>(size)
            repeat(size) {
                calls.add(parcel.readParcelable(CallLogItem::class.java.classLoader)!!)
            }
            return TimeFilter(date, calls)
        }

        fun groupCallsByDate(calls: List<CallLogItem>): List<TimeFilter> {
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
}