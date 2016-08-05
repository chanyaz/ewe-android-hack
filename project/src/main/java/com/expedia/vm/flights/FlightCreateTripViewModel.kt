package com.expedia.vm.flights

import android.content.Context
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.TripBucketItemFlightV2
import com.expedia.bookings.data.flights.FlightCreateTripParams
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.ValidFormOfPayment
import com.expedia.bookings.data.utils.getFee
import com.expedia.bookings.dialog.DialogFactory
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.utils.RetrofitUtils
import com.expedia.vm.BaseCreateTripViewModel
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class FlightCreateTripViewModel(val context: Context, val flightServices: FlightServices, val selectedCardFeeSubject: PublishSubject<ValidFormOfPayment>) : BaseCreateTripViewModel() {
    val tripParams = BehaviorSubject.create<FlightCreateTripParams>()

    init {
        performCreateTrip.subscribe {
            showCreateTripDialogObservable.onNext(true)
            flightServices.createTrip(tripParams.value).subscribe(makeCreateTripResponseObserver())
        }

        updateTripFeesOnCardSelection()
    }

    private fun updateTripFeesOnCardSelection() {
        selectedCardFeeSubject
                .filter { !it.getFee().isZero }
                .filter { it.getFee().compareTo(getCreateTripResponse().selectedCardFees) != 0 } // fee changed
                .subscribe { cardFee ->
                    // add card fee to trip response
                    val newTripResponse = getCreateTripResponse()
                    newTripResponse.selectedCardFees = cardFee.getFee()
                    tripResponseObservable.onNext(newTripResponse)
                }
    }

    private fun getCreateTripResponse(): FlightCreateTripResponse {
        return tripResponseObservable.value as FlightCreateTripResponse
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
                }
            }

            override fun onCompleted() {
                // ignore
            }
        }
    }
}
