package com.expedia.vm.flights

import android.content.Context
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.TripBucketItemFlightV2
import com.expedia.bookings.data.flights.FlightCreateTripParams
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.dialog.DialogFactory
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.tracking.FlightsV2Tracking
import com.expedia.bookings.utils.RetrofitUtils
import com.expedia.bookings.utils.Ui
import com.expedia.vm.BaseCreateTripViewModel
import rx.Observer
import rx.subjects.BehaviorSubject
import javax.inject.Inject

class FlightCreateTripViewModel(val context: Context) : BaseCreateTripViewModel() {

    lateinit var flightServices: FlightServices
        @Inject set

    val tripParams = BehaviorSubject.create<FlightCreateTripParams>()

    init {
        Ui.getApplication(context).flightComponent().inject(this)

        performCreateTrip.subscribe {
            showCreateTripDialogObservable.onNext(true)
            flightServices.createTrip(tripParams.value).subscribe(makeCreateTripResponseObserver())
        }
    }

    private fun makeCreateTripResponseObserver(): Observer<FlightCreateTripResponse> {
        return object : Observer<FlightCreateTripResponse> {
            override fun onNext(response: FlightCreateTripResponse) {
                showCreateTripDialogObservable.onNext(false)
                if (response.hasErrors() && !response.hasPriceChange()) {
                    val error = response.firstError
                    createTripErrorObservable.onNext(error)
                }
                else {
                    val hasPriceChange = response.details.oldOffer != null
                    if (hasPriceChange) {
                        priceChangeObservable.onNext(response)
                    }
                    Db.getTripBucket().clearFlight()
                    Db.getTripBucket().add(TripBucketItemFlightV2(response))
                    tripResponseObservable.onNext(response)
                }
            }

            override fun onError(e: Throwable) {
                if (RetrofitUtils.isNetworkError(e)) {
                    val retryFun = fun() {
                        flightServices.createTrip(tripParams.value).subscribe(makeCreateTripResponseObserver())
                    }
                    val cancelFun = fun() {
                        noNetworkObservable.onNext(Unit)
                    }
                    DialogFactory.showNoInternetRetryDialog(context, retryFun, cancelFun)
                    FlightsV2Tracking.trackFlightCreateTripNoResponseError()
                }
            }

            override fun onCompleted() {
                // ignore
            }
        }
    }
}
