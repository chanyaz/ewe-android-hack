package com.expedia.bookings.data.travelgraph

import com.expedia.bookings.data.SuggestionV4
import org.joda.time.Days
import org.joda.time.LocalDate

class SearchInfo(val destination: SuggestionV4, val startDate: LocalDate, val endDate: LocalDate, val travelers: TravelerInfo = TravelerInfo()) {

    fun totalStay(): Int {
        return Days.daysBetween(startDate, endDate).days
    }

    fun isValid(): Boolean {
        val now = LocalDate.now()
        return !(startDate.isBefore(now) || endDate.isBefore(now) || endDate.isBefore(startDate))
    }

    class Builder {
        private var destination: SuggestionV4? = null
        private var startDate: LocalDate? = null
        private var endDate: LocalDate? = null
        private var travelerInfo: TravelerInfo? = null

        fun destination(location: SuggestionV4?): Builder {
            this.destination = location?.copy()
            return this
        }

        fun startDate(date: LocalDate?): Builder {
            this.startDate = date
            return this
        }

        fun endDate(date: LocalDate?): Builder {
            this.endDate = date
            return this
        }

        fun travelers(travelerInfo: TravelerInfo?): Builder {
            this.travelerInfo = travelerInfo ?: TravelerInfo()
            return this
        }

        fun build(): SearchInfo? {
            if (destination != null && startDate != null && endDate != null) {
                return SearchInfo(destination!!, startDate!!, endDate!!, travelerInfo!!)
            }
            return null
        }
    }
}

data class TravelerInfo(val numOfAdults: Int = 1, val agesOfChildren: List<Int> = emptyList()) {
    fun totalTravelers(): Int {
        return numOfAdults + agesOfChildren.size
    }
}
