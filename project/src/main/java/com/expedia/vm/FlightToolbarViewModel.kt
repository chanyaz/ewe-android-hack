package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.utils.DateFormatUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.SuggestionStrUtils
import com.squareup.phrase.Phrase
import org.joda.time.LocalDate
import rx.Observable
import rx.subjects.BehaviorSubject

class FlightToolbarViewModel(private val context: Context) {
    //input
    val refreshToolBar = BehaviorSubject.create<Boolean>()
    val isOutboundSearch = BehaviorSubject.create<Boolean>() // TODO - move this into flightSearchViewModel
    val setTitleOnly = BehaviorSubject.create<String>()
    val city = BehaviorSubject.create<String>()
    val country = BehaviorSubject.create<String>()
    val airport = BehaviorSubject.create<String>()
    val travelers = BehaviorSubject.create<Int>()
    val date = BehaviorSubject.create<LocalDate>()
    val lob = BehaviorSubject.create<LineOfBusiness>()

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

        Observable.combineLatest(refreshToolBar, isOutboundSearch, city, country, airport, lob, travelers, date, { isResults, isOutboundSearch, cityBound, country, airportCode, lob, numTravelers, date ->
            if (lob == LineOfBusiness.FLIGHTS_V2) {
                titleSubject.onNext(getFlightTitle(isResults, isOutboundSearch, cityBound))
            } else if (lob == LineOfBusiness.PACKAGES) {
                titleSubject.onNext(getPackageTitle(isOutboundSearch, cityBound, country, airportCode))
            }
            subtitleSubject.onNext(getSubtitle(date, numTravelers))
            menuVisibilitySubject.onNext(isResults)
        }).subscribe()
    }

    fun getFlightTitle(isResults: Boolean, isOutboundSearch: Boolean, city: String): String {
        val resultsTitle: String = SuggestionStrUtils.formatCityName(context.resources.getString(R.string.select_flight_to, city))
        val overviewTitle: String = SuggestionStrUtils.formatCityName(context.resources.getString(R.string.flight_to_template, city))
        val resultsOutInboundTitle: String = context.resources.getString(R.string.select_return_flight)
        return if (isResults && !isOutboundSearch) resultsOutInboundTitle else if (isResults) resultsTitle else overviewTitle
    }

    fun getPackageTitle(isOutboundSearch: Boolean, city: String, country: String, airportCode: String): String {
        val resultsTitle: String = Phrase.from(context, R.string.package_outbound_flight_toolbar_title_TEMPLATE)
                .put("cityname", StrUtils.formatCityName(HtmlCompat.stripHtml(city)))
                .put("country", country)
                .put("airportcode", airportCode)
                .format()
                .toString()
        val resultsInboundTitle: String = Phrase.from(context, R.string.package_inbound_flight_toolbar_title_TEMPLATE)
                .put("cityname", StrUtils.formatCityName(HtmlCompat.stripHtml(city)))
                .put("country", country)
                .put("airportcode", airportCode)
                .format()
                .toString()
        return if (!isOutboundSearch) resultsInboundTitle else resultsTitle
    }

    fun getSubtitle(date: LocalDate, numTravelers: Int): String {
        val travelers = context.resources.getQuantityString(R.plurals.number_of_travelers_TEMPLATE, numTravelers, numTravelers)
        val subtitle = Phrase.from(context, R.string.flight_calendar_instructions_date_with_guests_TEMPLATE)
                .put("startdate", DateFormatUtils.formatLocalDateToShortDayAndDate(date))
                .put("guests", travelers)
                .format()
                .toString()
        return subtitle
    }
}