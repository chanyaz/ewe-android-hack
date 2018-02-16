package com.expedia.bookings.itin.vm

import android.content.Context
import com.expedia.bookings.utils.DateUtils
import com.mobiata.android.Log
import com.mobiata.flightlib.utils.DateTimeUtils
import io.reactivex.subjects.PublishSubject

abstract class ItinTimeDurationViewModel(private val context: Context) {

    data class TimeDurationWidgetParams(
            val text: String,
            val contDesc: String?,
            val drawable: Int?,
            val durationType: DurationType
    )

    enum class DurationType {
        TOTAL_DURATION,
        LAYOVER,
        NONE
    }

    val createTimeDurationWidgetSubject: PublishSubject<TimeDurationWidgetParams> = PublishSubject.create<TimeDurationWidgetParams>()

    abstract fun updateWidget(durationISO: String)

    fun getDurationMinutesFromISO(durationISO: String): Int {
        try {
            return DateUtils.parseDurationMinutesFromISOFormat(durationISO)
        } catch (e: IllegalArgumentException) {
            Log.e("unsupported parsing format", e)
        }
        return 0
    }

    fun getFormattedDuration(durationMinutes: Int): String {
        return if (durationMinutes > 0) {
            DateTimeUtils.formatDurationDaysHoursMinutes(context, durationMinutes)
        } else ""
    }
}
