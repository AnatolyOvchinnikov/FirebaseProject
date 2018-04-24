package com.shakuro.firebaseproject.extentions

import android.text.format.DateFormat
import java.util.*

fun Long.timestampToDate(): String {
    val cal = Calendar.getInstance()
    cal.setTimeInMillis(this)
    return DateFormat.format("dd-MM-yyyy", cal).toString()
}