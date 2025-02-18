package com.amigo.calllog

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.Parceler
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
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
            return calls
                .groupBy { 
                    Instant.ofEpochMilli(it.date)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate() 
                }
                .map { (date, groupedCalls) -> 
                    TimeFilter(date, groupedCalls.sortedByDescending { it.date })
                }
                .sortedByDescending { it.date }
        }
    }
}