package com.expedia.vm

import android.content.Context
import com.expedia.bookings.extensions.ObservableOld
import com.expedia.bookings.R
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.utils.SuggestionStrUtils

class FlightToolbarViewModel(private val context: Context) : BaseToolbarViewModel(context) {

    init {
        ObservableOld.combineLatest(refreshToolBar, isOutboundSearch, regionNames, travelers, date,
                { isResults, isOutboundSearch, regionNamesOptional, numTravelers, date ->
                    regionNamesOptional.value?.let { regionNames ->
                        titleSubject.onNext(getFlightTitle(isResults, isOutboundSearch, regionNames.displayName))
                    }
                    subtitleSubject.onNext(getSubtitle(date, numTravelers))
                    menuVisibilitySubject.onNext(isResults)
                }).subscribe()
    }

    private fun getFlightTitle(isResults: Boolean, isOutboundSearch: Boolean, displayName: String): String {
        val resultsTitle: String = SuggestionStrUtils.formatCityName(context.resources.getString(R.string.select_flight_to, HtmlCompat.stripHtml(displayName)))
        val overviewTitle: String = SuggestionStrUtils.formatCityName(context.resources.getString(R.string.flight_to_template, HtmlCompat.stripHtml(displayName)))
        val resultsOutInboundTitle: String = context.resources.getString(R.string.select_return_flight)
        return if (isResults && !isOutboundSearch) resultsOutInboundTitle else if (isResults) resultsTitle else overviewTitle
    }
}
