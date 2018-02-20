package com.expedia.bookings.itin.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.utils.DateRangeUtils

class FlightItinLayoverViewModel(val context: Context) : ItinTimeDurationViewModel(context) {

    override fun updateWidget(durationISO: String) {
        val layoverDurationMinutes = getDurationMinutesFromISO(durationISO)
        val formattedDuration = getFormattedDuration(layoverDurationMinutes)
        val contDescDuration = DateRangeUtils.getDurationContDescDaysHoursMins(context, layoverDurationMinutes) ?: ""
        createTimeDurationWidgetSubject.onNext(TimeDurationWidgetParams(
                formattedDuration, contDescDuration,
                R.drawable.itin_flight_layover_icon,
                DurationType.LAYOVER
        ))
    }
}
