package com.expedia.bookings.itin.vm

import android.content.Context
import android.support.annotation.VisibleForTesting
import com.expedia.bookings.R
import com.expedia.bookings.utils.DateUtils
import com.mobiata.android.Log
import com.mobiata.flightlib.utils.DateTimeUtils
import com.squareup.phrase.Phrase
import io.reactivex.subjects.PublishSubject

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
}