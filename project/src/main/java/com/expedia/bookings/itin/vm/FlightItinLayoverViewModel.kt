package com.expedia.bookings.itin.vm

import android.content.Context
import com.expedia.bookings.R
import com.mobiata.flightlib.utils.DateTimeUtils
import com.squareup.phrase.Phrase

class FlightItinLayoverViewModel(val context: Context) : ItinTimeDurationViewModel(context) {

    override fun updateWidget(layoverDurationISO: String) {
        val layoverDurationMinutes = getDurationMinutesFromISO(layoverDurationISO)
        val formattedDuration = getFormattedDuration(layoverDurationMinutes)
        val contDescDuration = DateTimeUtils.getDurationContDescDaysHoursMins(context, layoverDurationMinutes)
        createTimeDurationWidgetSubject.onNext(TimeDurationWidgetParams(
                Phrase.from(context, R.string.itin_flight_layover_TEMPLATE).put("layover", formattedDuration).format().toString(),
                Phrase.from(context, R.string.itin_flight_layover_TEMPLATE).put("layover", contDescDuration).format().toString(),
                R.drawable.itin_flight_layover_icon
        ))
    }
}
