package com.amigo.calllog

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TimeFilter(
    val title: String,
    val calls: List<CallLogItem>
) : Parcelable