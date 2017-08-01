package com.expedia.vm

import android.content.Context
import com.expedia.bookings.ObservableOld
import com.expedia.bookings.R
import com.expedia.bookings.utils.DateFormatUtils
import com.expedia.bookings.utils.SuggestionStrUtils
import com.squareup.phrase.Phrase
import io.reactivex.subjects.BehaviorSubject
import org.joda.time.LocalDate

class FlightToolbarViewModel(private val context: Context) {
    //input
    val refreshToolBar = BehaviorSubject.create<Boolean>()
    val isOutboundSearch = BehaviorSubject.create<Boolean>() // TODO - move this into flightSearchViewModel
    val setTitleOnly = BehaviorSubject.create<String>()
    val city = BehaviorSubject.create<String>()
    val travelers = BehaviorSubject.create<Int>()
    val date = BehaviorSubject.create<LocalDate>()

    //output
    val titleSubject = BehaviorSubject.create<String>()
    val subtitleSubject = BehaviorSubject.create<CharSequence>()
    val menuVisibilitySubject = BehaviorSubject.create<Boolean>()

    init {
        setTitleOnly.subscribe { title ->
            titleSubject.onNext(title)
            subtitleSubject.onNext("")
            menuVisibilitySubject.onNext(false)
        }

        ObservableOld.combineLatest(refreshToolBar, isOutboundSearch, city, travelers, date, { isResults, isOutboundSearch, cityBound, numTravelers, date ->
            val resultsTitle: String = SuggestionStrUtils.formatCityName(context.resources.getString(R.string.select_flight_to, cityBound))
            val overviewTitle: String = SuggestionStrUtils.formatCityName(context.resources.getString(R.string.flight_to_template, cityBound))
            val resultsOutInboundTitle: String = context.resources.getString(R.string.select_return_flight)
            val travelers = context.resources.getQuantityString(R.plurals.number_of_travelers_TEMPLATE, numTravelers, numTravelers)
            val subtitle = Phrase.from(context, R.string.flight_calendar_instructions_date_with_guests_TEMPLATE)
                    .put("startdate", DateFormatUtils.formatLocalDateToShortDayAndDate(date))
                    .put("guests", travelers)
                    .format()
                    .toString()
            titleSubject.onNext(if (isResults && !isOutboundSearch) resultsOutInboundTitle else if (isResults) resultsTitle else overviewTitle)
            subtitleSubject.onNext(subtitle)
            menuVisibilitySubject.onNext(isResults)
        }).subscribe()
    }
}