package com.expedia.bookings.itin.vm

import android.content.Context
import android.support.annotation.VisibleForTesting
import com.expedia.bookings.R
import com.expedia.bookings.utils.DateUtils
import com.mobiata.android.Log
import com.mobiata.flightlib.utils.DateTimeUtils
import com.squareup.phrase.Phrase
import rx.subjects.PublishSubject

abstract class ItinTimeDurationViewModel(private val context: Context) {

    data class TimeDurationWidgetParams(
            val text: String?,
            val contDesc: String?,
            val drawable: Int?
    )

    val createTimeDurationWidgetSubject: PublishSubject<TimeDurationWidgetParams> = PublishSubject.create<TimeDurationWidgetParams>()

    abstract fun updateWidget(duration: String)

    @VisibleForTesting
    fun getDurationMinutesFromISO(durationISO: String): Int {
        try {
            return DateUtils.parseDurationMinutesFromISOFormat(durationISO)
        } catch (e: IllegalArgumentException) {
            Log.e("unsupported parsing format", e)
        }
        return 0
    }

    @VisibleForTesting
    fun getFormattedDuration(durationMinutes: Int): String? {
        return if (durationMinutes > 0) {
            return DateTimeUtils.formatDurationDaysHoursMinutes(context, durationMinutes)
        } else null
    }

    @VisibleForTesting
    fun getDurationContDesc(durationMinutes: Int): String? {
        if (durationMinutes <= 0) {
            return null
        }
        val minutes = Math.abs(durationMinutes % 60)
        val hours = Math.abs(durationMinutes / 60 % 24)
        val days = Math.abs(durationMinutes / 24 / 60)
        val contDesc: String?
        if (days > 0 && hours > 0 && minutes > 0) {
            contDesc = Phrase.from(context, R.string.flight_duration_days_hours_minutes_cont_desc_TEMPLATE)
                    .put("days", days)
                    .put("hours", hours)
                    .put("minutes", minutes).format().toString()
        } else if (days > 0 && hours > 0) {
            contDesc = Phrase.from(context, R.string.flight_duration_days_hours_cont_desc_TEMPLATE)
                    .put("days", days)
                    .put("hours", hours).format().toString()
        } else if (days > 0 && minutes > 0) {
            contDesc = Phrase.from(context, R.string.flight_duration_days_minutes_cont_desc_TEMPLATE)
                    .put("days", days)
                    .put("minutes", minutes).format().toString()
        } else if (days > 0) {
            contDesc = Phrase.from(context, R.string.flight_duration_days_cont_desc_TEMPLATE)
                    .put("days", days).format().toString()
        } else if (hours > 0 && minutes > 0) {
            contDesc = Phrase.from(context, R.string.flight_duration_hours_minutes_cont_desc_TEMPLATE)
                    .put("hours", hours)
                    .put("minutes", minutes).format().toString()
        } else if (hours > 0) {
            contDesc = Phrase.from(context, R.string.flight_duration_hours_cont_desc_TEMPLATE)
                    .put("hours", hours).format().toString()
        } else if (minutes > 0) {
            contDesc = Phrase.from(context, R.string.flight_duration_minutes_cont_desc_TEMPLATE)
                    .put("minutes", minutes).format().toString()
        } else {
            return null
        }
        return contDesc
    }
}