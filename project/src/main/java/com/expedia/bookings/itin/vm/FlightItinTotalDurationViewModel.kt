package com.expedia.bookings.itin.vm

import android.content.Context
import com.expedia.bookings.R
import com.mobiata.flightlib.utils.DateTimeUtils
import com.squareup.phrase.Phrase

class FlightItinTotalDurationViewModel(val context: Context) : ItinTimeDurationViewModel(context) {

    override fun updateWidget(totalDurationISO: String) {
        val totalDurationMinutes = getDurationMinutesFromISO(totalDurationISO)
        val formattedDuration = getFormattedDuration(totalDurationMinutes)
        val contDescDuration = DateTimeUtils.getDurationContDescDaysHoursMins(context, totalDurationMinutes)
        createTimeDurationWidgetSubject.onNext(ItinTimeDurationViewModel.TimeDurationWidgetParams(
                Phrase.from(context, R.string.itin_flight_total_duration_TEMPLATE).put("duration", formattedDuration).format().toString(),
                Phrase.from(context, R.string.itin_flight_total_duration_TEMPLATE).put("duration", contDescDuration).format().toString(),
                null
        ))
    }
}
