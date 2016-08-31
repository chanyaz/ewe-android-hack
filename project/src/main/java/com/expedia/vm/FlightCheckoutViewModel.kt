package com.expedia.vm

import android.content.Context
import android.support.v4.content.ContextCompat
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.flights.FlightCheckoutParams
import com.expedia.bookings.data.flights.FlightCheckoutResponse
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.dialog.DialogFactory
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.tracking.FlightsV2Tracking
import com.expedia.bookings.utils.BookingSuppressionUtils
import com.expedia.bookings.utils.RetrofitUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.Ui
import com.squareup.phrase.Phrase
import rx.Observable
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

open class FlightCheckoutViewModel(context: Context) : BaseCheckoutViewModel(context) {
    lateinit var flightServices: FlightServices
        @Inject set

    override val builder = FlightCheckoutParams.Builder()
    // outputs
    val showDebitCardsNotAcceptedSubject = BehaviorSubject.create<Boolean>()
    val receivedCheckoutResponse = PublishSubject.create<Unit>()

    override fun injectComponents() {
        Ui.getApplication(context).flightComponent().inject(this)
    }

    init {
        val pointOfSale = PointOfSale.getPointOfSale()

        legalText.onNext(SpannableStringBuilder(pointOfSale.getColorizedFlightBookingStatement(ContextCompat.getColor(context, R.color.flight_primary_color))))

        tripResponseObservable.subscribe { it as FlightCreateTripResponse
            builder.tripId(it.newTrip.tripId)
            builder.expectedTotalFare(it.tripTotalPayableIncludingFeeIfZeroPayableByPoints().amount.toString())
            builder.expectedFareCurrencyCode(it.totalPrice.currency)
            builder.tealeafTransactionId(it.tealeafTransactionId)
            builder.suppressFinalBooking(BookingSuppressionUtils.shouldSuppressFinalBooking(context, R.string.preference_suppress_flight_bookings))
            val totalPrice = Phrase.from(context, R.string.your_card_will_be_charged_template)
                    .put("dueamount", it.tripTotalPayableIncludingFeeIfZeroPayableByPoints().formattedMoneyFromAmountAndCurrencyCode)
                    .format()
            sliderPurchaseTotalText.onNext(totalPrice)
            paymentTypeSelectedHasCardFee.onNext(false)
        }

        priceChangeObservable.subscribe { it as FlightCheckoutResponse
            // TODO - update to totalPrice for subPub support when api is fixed to return totalPrice field from a priceChange response
            val flightTripDetails = it.details
            if (flightTripDetails != null) {
                builder.expectedTotalFare(flightTripDetails.offer.totalFarePrice.amount.toString())
            }
        }

        checkoutParams.subscribe { params -> params as FlightCheckoutParams
            flightServices.checkout(params.toQueryMap()).subscribe(makeCheckoutResponseObserver())
            email = params.travelers.first().email
        }

        showDebitCardsNotAcceptedSubject.onNext(pointOfSale.doesNotAcceptDebitCardsForFlights())
    }

    override fun selectedPaymentHasCardFee(cardFee: Money, totalPriceInclFees: Money?) {
        // add card fee to trip response
        val newTripResponse = tripResponseObservable.value as FlightCreateTripResponse
        newTripResponse.selectedCardFees = cardFee
        newTripResponse.totalPriceIncludingFees = totalPriceInclFees
        tripResponseObservable.onNext(newTripResponse)
        selectedCardFeeObservable.onNext(cardFee)
    }

    override fun getTripId(): String {
        val flightCreateTripResponse = tripResponseObservable.value as FlightCreateTripResponse
        val tripId = flightCreateTripResponse.newTrip.tripId!!
        return tripId
    }

    private fun makeCheckoutResponseObserver(): Observer<FlightCheckoutResponse> {
        return object : Observer<FlightCheckoutResponse> {
            override fun onNext(response: FlightCheckoutResponse) {
                receivedCheckoutResponse.onNext(Unit)
                if (response.hasErrors()) {
                    when (response.firstError.errorCode) {
                        ApiError.Code.INVALID_INPUT -> {
                            // TODO
                        }
                        ApiError.Code.PRICE_CHANGE -> {
                            priceChangeObservable.onNext(response)
                        }
                        else -> {
                            checkoutErrorObservable.onNext(response.firstError)
                        }
                    }
                } else {
                    Db.getTripBucket().flightV2.flightCheckoutResponse = response
                    bookingSuccessResponse.onNext(Pair(response, email))
                }
            }

            override fun onError(e: Throwable) {
                if (RetrofitUtils.isNetworkError(e)) {
                    val retryFun = fun() {
                        flightServices.checkout(checkoutParams.value.toQueryMap()).subscribe(makeCheckoutResponseObserver())
                    }
                    val cancelFun = fun() {
                        noNetworkObservable.onNext(Unit)
                    }
                    DialogFactory.showNoInternetRetryDialog(context, retryFun, cancelFun)
                    FlightsV2Tracking.trackFlightCheckoutAPINoResponseError()
                }
            }

            override fun onCompleted() {
                // ignore
            }
        }
    }
}
