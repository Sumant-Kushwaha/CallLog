package com.amigo.calllog

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CallLogItem(
    val number: String,
    val name: String?,
    val type: Int,
    val date: Long,
    val duration: Long
) : Parcelable