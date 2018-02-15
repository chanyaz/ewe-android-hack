package com.expedia.bookings.extensions

import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormatter

fun DateTimeFormatter.safePrint(inDate: LocalDate?): String {
    if (inDate != null) {
        return this.print(inDate)
    } else {
        return ""
    }
}
