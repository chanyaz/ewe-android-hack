package com.expedia.bookings.itin.vm

import android.content.Context
import com.mobiata.flightlib.utils.DateTimeUtils

class FlightItinTotalDurationViewModel(val context: Context) : ItinTimeDurationViewModel(context) {

    override fun updateWidget(durationISO: String) {
        val totalDurationMinutes = getDurationMinutesFromISO(durationISO)
        val formattedDuration = getFormattedDuration(totalDurationMinutes)
        val contDescDuration = DateTimeUtils.getDurationContDescDaysHoursMins(context, totalDurationMinutes) ?: ""
        createTimeDurationWidgetSubject.onNext(ItinTimeDurationViewModel.TimeDurationWidgetParams(
                formattedDuration,
                contDescDuration,
                null, DurationType.TOTAL_DURATION
        ))
    }
}
