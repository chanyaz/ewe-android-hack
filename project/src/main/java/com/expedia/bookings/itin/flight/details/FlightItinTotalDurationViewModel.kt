package com.expedia.bookings.itin.flight.details

import android.content.Context
import com.expedia.bookings.itin.common.ItinTimeDurationViewModel
import com.expedia.bookings.utils.DateRangeUtils

class FlightItinTotalDurationViewModel(val context: Context) : ItinTimeDurationViewModel(context) {

    override fun updateWidget(durationISO: String) {
        val totalDurationMinutes = getDurationMinutesFromISO(durationISO)
        val formattedDuration = getFormattedDuration(totalDurationMinutes)
        val contDescDuration = DateRangeUtils.getDurationContDescDaysHoursMins(context, totalDurationMinutes) ?: ""
        createTimeDurationWidgetSubject.onNext(TimeDurationWidgetParams(
                formattedDuration,
                contDescDuration,
                null, DurationType.TOTAL_DURATION
        ))
    }
}
