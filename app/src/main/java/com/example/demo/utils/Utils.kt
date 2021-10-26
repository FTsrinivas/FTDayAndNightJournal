package com.example.demo.utils

import java.util.*

object Utils {

    fun getYearHeading(date: Date): String {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return "" + calendar[Calendar.YEAR]
    }
}