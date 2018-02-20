package com.expedia.bookings.itin.vm

import android.content.Context
import com.expedia.bookings.utils.DateRangeUtils
import com.expedia.bookings.utils.ApiDateUtils
import com.mobiata.android.Log
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
            return ApiDateUtils.parseDurationMinutesFromISOFormat(durationISO)
        } catch (e: IllegalArgumentException) {
            Log.e("unsupported parsing format", e)
        }
        return 0
    }

    fun getFormattedDuration(durationMinutes: Int): String {
        return if (durationMinutes > 0) {
            DateRangeUtils.formatDurationDaysHoursMinutes(context, durationMinutes)
        } else ""
    }
}
