package com.expedia.bookings.utils.rail

import android.content.Context
import android.support.annotation.NonNull
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.rail.requests.RailSearchRequest
import com.expedia.bookings.utils.DateFormatUtils
import com.expedia.bookings.utils.JodaUtils
import com.mobiata.flightlib.utils.DateTimeUtils
import com.squareup.phrase.Phrase
import org.joda.time.DateTime

object RailUtils {
    @JvmStatic
    fun formatRailChangesText(context: Context, changesCount: Int): String {
        if (changesCount == 0) {
            return context.getString(R.string.rail_direct)
        }
        return context.resources.getQuantityString(R.plurals.rail_changes_TEMPLATE, changesCount, changesCount)
    }

    fun addAndFormatMoney(firstPrice: Money, secondPrice: Money): String {
        val price = Money(firstPrice)
        var formattedPrice = ""
        if (price.add(secondPrice.amount)) {
            formattedPrice = price.formattedMoney
        }
        return formattedPrice
    }

    fun subtractAndFormatMoney(firstPrice: Money, secondPrice: Money): String {
        val price = Money(firstPrice)
        var formattedPrice = ""
        if (price.subtract(secondPrice.amount)) {
            formattedPrice = price.formattedMoney
        }
        return formattedPrice
    }

    fun getToolbarTitleFromSearchRequest(searchRequest: RailSearchRequest): String {
        return "${searchRequest.origin?.regionNames?.shortName} - ${searchRequest.destination?.regionNames?.shortName}"
    }

    fun getToolbarSubtitleFromSearchRequest(context: Context, searchRequest: RailSearchRequest): String {
        val travelerPart = context.resources.getQuantityString(R.plurals.number_of_travelers_TEMPLATE, searchRequest.guests,
                searchRequest.guests)
        val dateString = DateFormatUtils.formatRailDateRange(context, searchRequest.startDate, searchRequest.endDate)
        val subtitle = Phrase.from(context, R.string.rail_results_toolbar_subtitle_TEMPLATE)
                .put("searchdates", dateString)
                .put("travelerspart", travelerPart).format().toString()
        return subtitle
    }

    @JvmStatic
    fun formatTimeInterval(@NonNull context: Context, @NonNull start: DateTime, @NonNull end: DateTime): String {
        val dateFormat = DateTimeUtils.getDeviceTimeFormat(context)
        val formattedStart = JodaUtils.format(start, dateFormat)
        val formattedEnd = JodaUtils.format(end, dateFormat)
        val elapsedDays = Math.abs(JodaUtils.daysBetween(start, end))
        if (elapsedDays > 0) {
            return Phrase.from(context, R.string.departure_arrival_time_multi_day_TEMPLATE)
                    .put("departuretime", formattedStart)
                    .put("arrivaltime", formattedEnd)
                    .put("elapseddays", elapsedDays).format().toString()
        }
        else {
            return context.getString(R.string.date_time_range_TEMPLATE, formattedStart, formattedEnd);
        }
    }
}