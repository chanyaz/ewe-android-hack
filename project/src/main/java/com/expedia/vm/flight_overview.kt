package com.expedia.vm

import android.app.Activity
import android.content.Context
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.packages.FlightLeg
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.PackageFlightUtils
import com.expedia.util.endlessObserver
import com.squareup.phrase.Phrase
import rx.Observer
import rx.subjects.BehaviorSubject

class FlightOverviewViewModel(val context: Context) {

    val selectedFlightLeg = BehaviorSubject.create<FlightLeg>()
    val flightTimeObserver = BehaviorSubject.create<String>()
    val flightAirlineObserver = BehaviorSubject.create<String>()
    val flightAirportsObserver = BehaviorSubject.create<String>()
    val flightDurationObserver = BehaviorSubject.create<String>()
    val bundlePriceObserver = BehaviorSubject.create<String>()

    init {
        selectedFlightLeg.subscribe { selectedFlight ->
            flightTimeObserver.onNext(PackageFlightUtils.getFlightDepartureArivalTimeAndDays(context, selectedFlight))
            flightAirlineObserver.onNext(selectedFlight.carrierName)
            flightAirportsObserver.onNext(PackageFlightUtils.getAllAirports(selectedFlight))
            flightDurationObserver.onNext(PackageFlightUtils.getFlightDurationString(context, selectedFlight))
            var perPersonPrice = Phrase.from(context.resources.getString(R.string.package_flight_overview_per_person_TEMPLATE))
                                .put("money", selectedFlight.packageOfferModel.price.packageTotalPriceFormatted)
                                .format().toString()
            bundlePriceObserver.onNext(perPersonPrice)
        }
    }

    val selectFlightClickObserver: Observer<Unit> = endlessObserver {
        val params = Db.getPackageParams()
        val flight = selectedFlightLeg.value
        params.flightType = Constants.PACKAGE_FLIGHT_TYPE
        params.selectedLegId = flight.departureLeg
        params.packagePIID = flight.packageOfferModel.piid
        if (flight.outbound) Db.setPackageSelectedOutboundFlight(flight) else Db.setPackageSelectedInboundFlight(flight)
        val activity = (context as AppCompatActivity)
        activity.setResult(Activity.RESULT_OK)
        activity.finish()
    }
}
