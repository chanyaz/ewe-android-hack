package com.expedia.bookings.services

import com.expedia.bookings.data.flights.FlightCreateTripParams
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.flights.FlightSearchResponse
import com.expedia.bookings.extensions.subscribeObserver
import io.reactivex.Observer
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
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

    override fun flightSearch(params: FlightSearchParams, observer: Observer<FlightSearchResponse>,
                              resultsResponseReceivedObservable: PublishSubject<Unit>?): Disposable {
        searchRequestSubscription?.dispose()
        searchRequestSubscription = doKongFlightSearch(params, observer, resultsResponseReceivedObservable, FlightSearchResponse.FlightSearchType.NORMAL)
        return searchRequestSubscription as Disposable
    }

    override fun greedyFlightSearch(params: FlightSearchParams, observer: Observer<FlightSearchResponse>,
                                resultsResponseReceivedObservable: PublishSubject<Unit>?): Disposable {
        greedySearchRequestSubscription?.dispose()
        greedySearchRequestSubscription = doKongFlightSearch(params, observer, resultsResponseReceivedObservable, FlightSearchResponse.FlightSearchType.GREEDY)
        return greedySearchRequestSubscription as Disposable
    }

    private fun doKongFlightSearch(params: FlightSearchParams, observer: Observer<FlightSearchResponse>, resultsResponseReceivedObservable: PublishSubject<Unit>? = null, searchType: FlightSearchResponse.FlightSearchType): Disposable {
        return flightApi.kongFlightSearch(params.toQueryMapForKong())
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .doOnNext { resultsResponseReceivedObservable?.onNext(Unit) }
                .doOnNext { response ->
                    response.searchType = searchType
                    processSearchResponse(response)
                }
                .subscribeObserver(observer)
    }
}
