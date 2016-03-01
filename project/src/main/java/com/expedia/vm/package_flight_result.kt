package com.expedia.vm

import android.content.Context
import android.text.format.DateUtils
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.packages.FlightLeg
import com.expedia.bookings.utils.JodaUtils
import com.expedia.bookings.utils.StrUtils
import rx.subjects.BehaviorSubject

class FlightResultsViewModel() {
    val flightResultsObservable = BehaviorSubject.create<List<FlightLeg>>()

    init {
        val isOutboundSearch = Db.getPackageParams()?.isOutboundSearch() ?: false
        flightResultsObservable.onNext(Db.getPackageResponse().packageResult.flightsPackage.flights.filter { it.outbound == isOutboundSearch && it.packageOfferModel != null }.sortedBy { it.packageOfferModel.price.packageTotalPrice.amount })
    }
}

class FlightToolbarViewModel(private val context: Context) {
    //input
    val refreshToolBar = BehaviorSubject.create<Boolean>()
    val setTitleOnly = BehaviorSubject.create<String>()

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

        refreshToolBar.subscribe { isResults ->
            // Flights Toolbar content - 6235
            var isOutboundSearch = Db.getPackageParams().isOutboundSearch()
            var cityBound: String = if (isOutboundSearch) Db.getPackageParams().destination.regionNames.shortName else Db.getPackageParams().origin.regionNames.shortName
            var resultsTitle: String = StrUtils.formatCityName(context.resources.getString(R.string.select_flight_to, cityBound))
            var overviewTitle: String = StrUtils.formatCityName(context.resources.getString(R.string.flight_to_template, cityBound))
            var resultsOutInboundTitle: String = context.resources.getString(R.string.select_return_flight)
            titleSubject.onNext(if (isResults && !isOutboundSearch) resultsOutInboundTitle else if (isResults) resultsTitle else overviewTitle)

            val numTravelers = Db.getPackageParams().guests()
            val travelers = context.resources.getQuantityString(R.plurals.number_of_travelers_TEMPLATE, numTravelers, numTravelers)
            val date = if (isOutboundSearch) Db.getPackageParams().checkIn else Db.getPackageParams().checkOut
            val subtitle: CharSequence = JodaUtils.formatLocalDate(context, date, DateUtils.FORMAT_SHOW_DATE + DateUtils.FORMAT_SHOW_YEAR + DateUtils.FORMAT_SHOW_WEEKDAY) + ", " + travelers
            subtitleSubject.onNext(subtitle)
            menuVisibilitySubject.onNext(isResults)
        }
    }
}
