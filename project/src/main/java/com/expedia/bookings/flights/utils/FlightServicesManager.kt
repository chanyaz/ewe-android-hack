package com.expedia.bookings.flights.utils

import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.flights.FlightSearchResponse
import com.expedia.bookings.data.flights.FlightSearchResponse.FlightSearchType
import com.expedia.bookings.extensions.subscribeObserver
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.utils.RetrofitUtils
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.PublishSubject

class FlightServicesManager(val flightServices: FlightServices) {
    val resultsReceivedDateTimeObservable = PublishSubject.create<Unit>()

    fun doFlightSearch(params: FlightSearchParams, type: FlightSearchType, successHandler: PublishSubject<Pair<FlightSearchType, FlightSearchResponse>>,
                       errorHandler: PublishSubject<Pair<FlightSearchType, ApiError>>): Disposable {

        return flightServices.flightSearch(params, resultsReceivedDateTimeObservable).subscribeObserver(
                object : DisposableObserver<FlightSearchResponse>() {
                    override fun onNext(response: FlightSearchResponse) {
                        if (response.hasErrors()) {
                            errorHandler.onNext(Pair(type, response.firstError))
                        } else if (response.offers.isEmpty() || response.legs.isEmpty()) {
                            errorHandler.onNext(Pair(type, ApiError(ApiError.Code.FLIGHT_SEARCH_NO_RESULTS)))
                        } else {
                            successHandler.onNext(Pair(type, response))
                        }
                    }
                    override fun onError(e: Throwable) {
                        if (RetrofitUtils.isNetworkError(e)) {
                            errorHandler.onNext(Pair(type, ApiError(ApiError.Code.NO_INTERNET)))
                        }
                    }
                    override fun onComplete() {
                    }
                })
    }
}
