package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.rail.requests.RailCheckoutParams
import com.expedia.bookings.data.rail.responses.RailCreateTripResponse
import com.expedia.bookings.services.RailServices
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.rail.responses.RailCheckoutResponse
import com.expedia.bookings.dialog.DialogFactory
import com.expedia.bookings.utils.RetrofitUtils
import com.expedia.util.endlessObserver
import com.squareup.phrase.Phrase
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import javax.inject.Inject

class RailCheckoutViewModel(val context: Context) {
    lateinit var railServices: RailServices
        @Inject set

    val sliderPurchaseTotalText = PublishSubject.create<CharSequence>()

    val checkoutParams = BehaviorSubject.create<RailCheckoutParams>()
    val builder = RailCheckoutParams.Builder()

    val bookingSuccessSubject = PublishSubject.create<RailCheckoutResponse>()

    init {
        Ui.getApplication(context).railComponent().inject(this)

        checkoutParams.subscribe { params ->
            railServices.railCheckoutTrip(params, makeCheckoutResponseObserver())
        }
    }

    val createTripObserver = endlessObserver<RailCreateTripResponse> { createTripResponse ->
        val tripDetails = RailCheckoutParams.TripDetails(createTripResponse.tripId,
                createTripResponse.totalPrice.amount.toString(),
                createTripResponse.totalPrice.currencyCode,
                true)
        builder.tripDetails(tripDetails)
    }


    val travelerCompleteObserver = endlessObserver<Traveler> { traveler ->
        val bookingTraveler = RailCheckoutParams.Traveler(traveler.firstName,
                traveler.lastName, traveler.phoneCountryCode, traveler.phoneNumber, traveler.email)
        builder.traveler(listOf(bookingTraveler))
    }

    val clearTravelers = endlessObserver<Unit> {
        builder.clearTravelers()
    }

    val paymentCompleteObserver = endlessObserver<BillingInfo?> { billingInfo ->
        val cardDetails = RailCheckoutParams.CardDetails(billingInfo?.number.toString(),
                billingInfo?.expirationDate?.year.toString(), billingInfo?.expirationDate?.monthOfYear.toString(),
                billingInfo?.securityCode, billingInfo?.nameOnCard,
                billingInfo?.location?.streetAddressString, null,
                billingInfo?.location?.city, billingInfo?.location?.stateCode,
                billingInfo?.location?.postalCode, null, billingInfo?.location?.countryCode)
        val paymentInfo = RailCheckoutParams.PaymentInfo(listOf(cardDetails))
        builder.paymentInfo(paymentInfo)
    }

    val totalPriceObserver = endlessObserver<Money> { totalPrice ->
        val slideToPurchaseText = Phrase.from(context, R.string.your_card_will_be_charged_template)
                .put("dueamount", totalPrice.formattedMoneyFromAmountAndCurrencyCode)
                .format().toString()

        sliderPurchaseTotalText.onNext(slideToPurchaseText)
    }

    fun isValidForBooking() : Boolean {
        return builder.isValid();
    }

    private fun makeCheckoutResponseObserver(): Observer<RailCheckoutResponse> {
        return object : Observer<RailCheckoutResponse> {
            override fun onNext(response: RailCheckoutResponse) {
                if (response.hasErrors()) {
                    when (response.firstError.errorCode) {
                        ApiError.Code.INVALID_INPUT -> {
                            // TODO
                        }
                        ApiError.Code.PRICE_CHANGE -> {
                            //TODO
                        }
                        else -> {
                            // TODO checkoutErrorObservable.onNext(response.firstError)
                        }
                    }
                } else {
                     bookingSuccessSubject.onNext(response)
                }
            }

            override fun onError(e: Throwable) {
                if (RetrofitUtils.isNetworkError(e)) {
                    val retryFun = fun() {
                        // TODO retry
                    }
                    val cancelFun = fun() {
                        //TODO noNetworkObservable.onNext(Unit)
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