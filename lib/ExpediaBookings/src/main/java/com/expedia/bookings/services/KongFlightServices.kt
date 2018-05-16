package com.expedia.bookings.services

import com.expedia.bookings.data.flights.FlightCreateTripParams
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.extensions.subscribeObserver
import io.reactivex.Observer
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import okhttp3.Interceptor
import okhttp3.OkHttpClient

class KongFlightServices(endpoint: String, okHttpClient: OkHttpClient, interceptors: List<Interceptor>, observeOn: Scheduler,
                         subscribeOn: Scheduler) : FlightServices(endpoint, okHttpClient, interceptors, observeOn, subscribeOn) {
    override fun createTrip(params: FlightCreateTripParams, observer: Observer<FlightCreateTripResponse>): Disposable {
        createTripRequestSubscription?.dispose()
        val createTripRequestSubscription = flightApi.createTrip(params.flexEnabled, params.queryParamsForNewCreateTrip(), params.featureOverride, params.fareFamilyCode, params.fareFamilyTotalPrice, params.childTravelerAge)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribeObserver(observer)
        this.createTripRequestSubscription = createTripRequestSubscription
        return createTripRequestSubscription
    }
}
