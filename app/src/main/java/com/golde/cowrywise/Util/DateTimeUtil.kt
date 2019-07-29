package com.golde.cowrywise.Util

import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime

class DateTimeUtil {

    companion object {
        fun format(dt : DateTime) : String{
            val dateFormat: DateFormat = DateFormat("yyyy-MM-dd")
            return dt.format(dateFormat)
        }

        fun format2(dt : DateTime) : String{
            val dateFormat: DateFormat = DateFormat("MMM dd")
            return dt.format(dateFormat)
        }
    }
}