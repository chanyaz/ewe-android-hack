package com.expedia.bookings.data.packages

import com.expedia.bookings.data.SuggestionV4
import org.joda.time.LocalDate

data class PackageSearchParamsForSaving(
        val origin: SuggestionV4?,
        val destination: SuggestionV4?,
        val startDate: LocalDate,
        val endDate: LocalDate?,
        val adults: Int,
        val children: List<Int>,
        val infantSeatingInLap: Boolean = false)