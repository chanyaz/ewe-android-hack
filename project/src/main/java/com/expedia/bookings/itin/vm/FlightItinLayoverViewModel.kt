package com.expedia.bookings.itin.vm

import android.content.Context
import com.expedia.bookings.R
import com.mobiata.flightlib.utils.DateTimeUtils
import com.squareup.phrase.Phrase

class FlightItinLayoverViewModel(val context: Context) : ItinTimeDurationViewModel(context) {

    override fun updateWidget(layoverDurationISO: String) {
        val layoverDurationMinutes = getDurationMinutesFromISO(layoverDurationISO)
        val formattedDuration = getFormattedDuration(layoverDurationMinutes)
        var contDescDuration = ""
        if (!DateTimeUtils.getDurationContDescDaysHoursMins(context, layoverDurationMinutes).isNullOrBlank()) {
            contDescDuration = DateTimeUtils.getDurationContDescDaysHoursMins(context, layoverDurationMinutes)
        }
        createTimeDurationWidgetSubject.onNext(TimeDurationWidgetParams(
                formattedDuration,
                contDescDuration,
                R.drawable.itin_flight_layover_icon, DurationType.LAYOVER
        ))
    }
}
