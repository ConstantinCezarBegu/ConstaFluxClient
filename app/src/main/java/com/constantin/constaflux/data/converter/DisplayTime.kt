package com.constantin.constaflux.data.converter

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class DisplayTime(
    val instant: String,
    val displayTime: String,
    val unixTimeStamp: Long
) : Parcelable

