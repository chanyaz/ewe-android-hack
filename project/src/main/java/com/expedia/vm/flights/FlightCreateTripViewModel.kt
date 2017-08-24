package com.expedia.vm.flights

import android.content.Context
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.TripBucketItemFlightV2
import com.expedia.bookings.data.flights.FlightCreateTripParams
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.utils.RetrofitUtils
import com.expedia.bookings.utils.Ui
import com.expedia.vm.BaseCreateTripViewModel
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import javax.inject.Inject

class FlightCreateTripViewModel(val context: Context) : BaseCreateTripViewModel() {

    lateinit var flightServices: FlightServices
        @Inject set

    val tripParams = BehaviorSubject.create<FlightCreateTripParams>()
    val showNoInternetRetryDialog = PublishSubject.create<Unit>()

    init {
        Ui.getApplication(context).flightComponent().inject(this)

        performCreateTrip.subscribe {
            showCreateTripDialogObservable.onNext(true)
            flightServices.createTrip(tripParams.value, makeCreateTripResponseObserver())
        }
    }

    fun makeCreateTripResponseObserver(): Observer<FlightCreateTripResponse> {
        return object : Observer<FlightCreateTripResponse> {
            override fun onNext(response: FlightCreateTripResponse) {
                showCreateTripDialogObservable.onNext(false)
                if (response.hasErrors() && !response.hasPriceChange()) {
                    val error = response.firstError
                    createTripErrorObservable.onNext(error)
                }
                else {
                    Db.getTripBucket().clearFlight()
                    Db.getTripBucket().add(TripBucketItemFlightV2(response))
                    createTripResponseObservable.onNext(response)
                }
            }

            override fun onError(e: Throwable) {
                showCreateTripDialogObservable.onNext(false)
                if (RetrofitUtils.isNetworkError(e)) {
                    FlightsV2Tracking.trackFlightCreateTripNoResponseError()
                    showNoInternetRetryDialog.onNext(Unit)
                }
            }

            override fun onCompleted() {
                // ignore
            }
        }
    }
}
