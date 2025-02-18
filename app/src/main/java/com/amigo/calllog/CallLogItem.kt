package com.amigo.calllog

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
data class CallLogItem(
    val number: String,
    val name: String?,
    val type: Int,
    val date: LocalDate = LocalDate.now(),
    val count: Int = 1  // Default to 1 for backward compatibility
) : Parcelable {
    companion object {
        const val MISSED_CALL_TYPE = 3
        const val RECEIVED_CALL_TYPE = 1
        const val DIALED_CALL_TYPE = 2
    }
}