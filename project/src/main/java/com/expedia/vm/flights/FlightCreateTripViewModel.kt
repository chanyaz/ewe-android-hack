package com.expedia.vm.flights

import android.content.Context
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
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
import rx.Subscription
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.ArrayList

class FlightCreateTripViewModel(val context: Context, val flightServices: FlightServices, val selectedCardFeeSubject: PublishSubject<ValidFormOfPayment?>) : BaseCreateTripViewModel() {
    val tripParams = BehaviorSubject.create<FlightCreateTripParams>()
    val cardFeeSubscriptions = ArrayList<Subscription>()

    init {
        performCreateTrip.subscribe {
            showCreateTripDialogObservable.onNext(true)
            flightServices.createTrip(tripParams.value).subscribe(makeCreateTripResponseObserver())
        }
    }

    private fun updateTripFeesOnCardSelection() {
        cardFeeSubscriptions.forEach {
            it.unsubscribe()
        }
        cardFeeSubscriptions.clear()

        cardFeeSubscriptions.add(selectedCardFeeSubject
                .filter { it != null && it.getFee().compareTo(getCreateTripResponse().selectedCardFees) != 0 } // selected card has a fee and fee has changed
                .subscribe { cardFee ->
                    // add card fee to trip response
                    val newTripResponse = getCreateTripResponse()
                    newTripResponse.selectedCardFees = cardFee!!.getFee()
                    tripResponseObservable.onNext(newTripResponse)
                })
        cardFeeSubscriptions.add(selectedCardFeeSubject
                .filter { it == null && getCreateTripResponse().selectedCardFees != null && !getCreateTripResponse().selectedCardFees.isZero } // selected card has no fees
                .subscribe {
                    val newTripResponse = getCreateTripResponse()
                    val zeroFees = Money(0, newTripResponse.totalPrice.currency)
                    newTripResponse.selectedCardFees = zeroFees
                    tripResponseObservable.onNext(newTripResponse)
                })
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
                    updateTripFeesOnCardSelection()
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
