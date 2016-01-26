package com.expedia.vm

import android.app.Activity
import android.content.Context
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.packages.FlightLeg
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.PackageFlightUtils
import com.expedia.util.endlessObserver
import rx.Observer
import rx.subjects.BehaviorSubject

class FlightOverviewViewModel(val context: Context) {

    val selectedFlightLeg = BehaviorSubject.create<FlightLeg>()
    val flightTimeObserver = BehaviorSubject.create<String>()
    val flightAirlineObserver = BehaviorSubject.create<String>()
    val flightAirportsObserver = BehaviorSubject.create<String>()
    val flightDurationObserver = BehaviorSubject.create<String>()

    init {
        selectedFlightLeg.subscribe { selectedFlight ->
            flightTimeObserver.onNext(PackageFlightUtils.getFlightDepartureArivalTimeAndDays(context, selectedFlight))
            flightAirlineObserver.onNext(selectedFlight.carrierName)
            flightAirportsObserver.onNext(PackageFlightUtils.getAllAirports(selectedFlight))
            flightDurationObserver.onNext(PackageFlightUtils.getFlightDurationString(context, selectedFlight))
        }
    }

    val selectFlightClickObserver: Observer<Unit> = endlessObserver {
        val params = Db.getPackageParams();
        params.flightType = Constants.PACKAGE_FLIGHT_TYPE
        params.selectedLegId = selectedFlightLeg.value.departureLeg
        params.packagePIID = selectedFlightLeg.value.packageOfferModel.piid;
        val activity = (context as AppCompatActivity)
        activity.setResult(Activity.RESULT_OK)
        activity.finish()
    }
}
