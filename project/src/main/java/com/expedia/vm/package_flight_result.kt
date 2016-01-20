package com.expedia.vm

import android.content.Context
import android.text.format.DateUtils
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.packages.FlightLeg
import com.expedia.bookings.utils.JodaUtils
import rx.subjects.BehaviorSubject
import kotlin.collections.filter

public class FlightResultsViewModel() {
    val flightResultsObservable = BehaviorSubject.create<List<FlightLeg>>()

    init {
        val isOutboundSearch = Db.getPackageParams().isOutboundSearch()
        flightResultsObservable.onNext(Db.getPackageResponse().packageResult.flightsPackage.flights.filter { it.outbound == isOutboundSearch && it.packageOfferModel != null})
    }
}

public class FlightToolbarViewModel(private val context: Context) {
    val titleSubject = BehaviorSubject.create<String>()
    val subtitleSubject = BehaviorSubject.create<CharSequence>()

    init {
        val isOutboundSearch = Db.getPackageParams().isOutboundSearch()

        var title : String = if (isOutboundSearch) Db.getPackageParams().destination.regionNames.shortName else Db.getPackageParams().origin.regionNames.shortName
        title = context.resources.getString(R.string.select_flight_to, title)
        titleSubject.onNext(title)
        val numTravelers = Db.getPackageParams().guests()
        val travelers = context.resources.getQuantityString(R.plurals.number_of_travelers_TEMPLATE, numTravelers, numTravelers)
        val date = if (isOutboundSearch) Db.getPackageParams().checkIn else Db.getPackageParams().checkOut
        val subtitle : CharSequence = JodaUtils.formatLocalDate(context, date, DateUtils.FORMAT_SHOW_DATE + DateUtils.FORMAT_SHOW_YEAR + DateUtils.FORMAT_SHOW_WEEKDAY) + ", " + travelers
        subtitleSubject.onNext(subtitle)
    }
}
