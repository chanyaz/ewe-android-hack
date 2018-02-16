package com.expedia.bookings.itin.vm

import android.content.Context
import com.expedia.bookings.R
import com.mobiata.flightlib.utils.DateTimeUtils

class FlightItinLayoverViewModel(val context: Context) : ItinTimeDurationViewModel(context) {

    override fun updateWidget(durationISO: String) {
        val layoverDurationMinutes = getDurationMinutesFromISO(durationISO)
        val formattedDuration = getFormattedDuration(layoverDurationMinutes)
        val contDescDuration = DateTimeUtils.getDurationContDescDaysHoursMins(context, layoverDurationMinutes) ?: ""
        createTimeDurationWidgetSubject.onNext(TimeDurationWidgetParams(
                formattedDuration, contDescDuration,
                R.drawable.itin_flight_layover_icon,
                DurationType.LAYOVER
        ))
    }
}
