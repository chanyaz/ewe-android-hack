package com.expedia.vm

import android.content.Context
import android.text.format.DateUtils
import com.expedia.bookings.R
import com.expedia.bookings.utils.JodaUtils
import com.expedia.bookings.utils.StrUtils
import org.joda.time.LocalDate
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class FlightToolbarViewModel(private val context: Context) {
    //input
    val refreshToolBar = BehaviorSubject.create<Boolean>()
    val isOutboundSearch = BehaviorSubject.create<Boolean>()
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

        Observable.combineLatest(refreshToolBar, isOutboundSearch, city, travelers, date, { isResults, isOutboundSearch, cityBound, numTravelers, date ->
            var resultsTitle: String = StrUtils.formatCityName(context.resources.getString(R.string.select_flight_to, cityBound))
            var overviewTitle: String = StrUtils.formatCityName(context.resources.getString(R.string.flight_to_template, cityBound))
            var resultsOutInboundTitle: String = context.resources.getString(R.string.select_return_flight)
            titleSubject.onNext(if (isResults && !isOutboundSearch) resultsOutInboundTitle else if (isResults) resultsTitle else overviewTitle)
            val travelers = context.resources.getQuantityString(R.plurals.number_of_travelers_TEMPLATE, numTravelers, numTravelers)
            val subtitle: CharSequence = JodaUtils.formatLocalDate(context, date, DateUtils.FORMAT_SHOW_DATE + DateUtils.FORMAT_SHOW_YEAR + DateUtils.FORMAT_SHOW_WEEKDAY) + ", " + travelers
            subtitleSubject.onNext(subtitle)
            menuVisibilitySubject.onNext(isResults)
        }).subscribe()
    }
}