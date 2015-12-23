package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.PackageSearchResponse
import com.expedia.bookings.data.packages.FlightLeg
import com.expedia.bookings.data.packages.Hotel
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.services.PackageServices
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class BundleOverviewViewModel(val context: Context, val packageServices: PackageServices) {
    val searchParamsObservable = PublishSubject.create<PackageSearchParams>()

    // Outputs
    val hotelTextObservable = BehaviorSubject.create<String>()
    val destinationTextObservable = BehaviorSubject.create<String>()
    val arrivalTextObservable = BehaviorSubject.create<String>()
    val hotelResultsObservable = BehaviorSubject.create<List<Hotel>>()
    val flightResutlsObservable = BehaviorSubject.create<List<FlightLeg>>()

    init {
        searchParamsObservable.subscribe { params ->
            packageServices.packageSearch(params).subscribe(makeResultsObserver())
            hotelTextObservable.onNext(context.getString(R.string.hotels_in_TEMPLATE, params.destination.regionNames.shortName))
            destinationTextObservable.onNext(context.getString(R.string.flights_to_TEMPLATE, params.destination.regionNames.shortName))
            arrivalTextObservable.onNext(context.getString(R.string.flights_to_TEMPLATE, params.arrival.regionNames.shortName))
        }
    }

    fun makeResultsObserver(): Observer<PackageSearchResponse> {
        return object : Observer<PackageSearchResponse> {
            override fun onNext(response: PackageSearchResponse) {
                hotelResultsObservable.onNext(response.packageResult.hotelsPackage.hotels)
                flightResutlsObservable.onNext(response.packageResult.flightsPackage.flights)
                println("package success, Hotels:" + response.packageResult.hotelsPackage.hotels.size + "  Flights:" + response.packageResult.flightsPackage.flights.size)
            }

            override fun onCompleted() {
                println("package completed")
            }

            override fun onError(e: Throwable?) {
                println("package error: " + e?.message)
            }
        }
    }
}