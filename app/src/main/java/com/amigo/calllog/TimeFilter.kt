package com.amigo.calllog

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.Parceler
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Parcelize
data class TimeFilter(
    val date: LocalDate = LocalDate.now(),
    val calls: List<CallLogItem>
) : Parcelable {
    val title: String = when {
        date == LocalDate.now() -> "Calls"
        else -> "Calls"
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
            return listOf(TimeFilter(LocalDate.now(), calls))
        }
    }
}