package com.expedia.bookings.model

import com.mobiata.android.time.util.JodaUtils
import org.joda.time.LocalDate

data class HotelStayDates(private val startDate: LocalDate?, private val endDate: LocalDate?) {
    fun getStartDate(): LocalDate? {
        return startDate
    }

    fun getEndDate(): LocalDate? {
        if (startDate == null) {
            return null
        }
        if (endDate == null) {
            return startDate.plusDays(1)
        }
        return endDate
    }

    fun sameHotelStayDates(other: HotelStayDates?): Boolean {
        if (other == null) {
            return false
        }

        return JodaUtils.isEqual(getStartDate(), other.getStartDate()) && JodaUtils.isEqual(getEndDate(), other.getEndDate())
    }
}
