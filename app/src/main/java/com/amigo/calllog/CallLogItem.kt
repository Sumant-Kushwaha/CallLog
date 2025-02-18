package com.amigo.calllog

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CallLogItem(
    val number: String,
    val name: String?,
    val type: Int,
    val count: Int = 1  // Default to 1 for backward compatibility
) : Parcelable