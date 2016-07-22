package com.expedia.vm

import android.content.Context
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.User
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.flights.FlightCheckoutParams
import com.expedia.bookings.data.flights.FlightCheckoutResponse
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.ValidFormOfPayment
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.utils.getFee
import com.expedia.bookings.data.utils.getPaymentType
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.utils.BookingSuppressionUtils
import com.expedia.bookings.utils.Strings
import com.squareup.phrase.Phrase
import rx.Observable
import rx.Observer
import rx.exceptions.OnErrorNotImplementedException
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class FlightCheckoutViewModel(context: Context, val flightServices: FlightServices, val selectedCardTypeSubject: PublishSubject<PaymentType?>) : BaseCheckoutViewModel(context) {

    override val builder = FlightCheckoutParams.Builder()

    // inputs
    val validFormsOfPaymentSubject = BehaviorSubject.create<List<ValidFormOfPayment>>()
    val selectedFlightChargesFees = PublishSubject.create<String>()
    val obFeeDetailsUrlSubject = PublishSubject.create<String>()

    // outputs
    val paymentTypeSelectedHasCardFee = PublishSubject.create<Boolean>()
    val cardFeeTextSubject = PublishSubject.create<Spanned>()
    val cardFeeWarningTextSubject = PublishSubject.create<Spanned>()
    val cardFeeForSelectedCard = PublishSubject.create<ValidFormOfPayment>()
    val showDebitCardsNotAcceptedSubject = BehaviorSubject.create<Boolean>()

    init {
        val pointOfSale = PointOfSale.getPointOfSale()

        legalText.onNext(SpannableStringBuilder(pointOfSale.stylizedFlightBookingStatement))

        tripResponseObservable.subscribe { it as FlightCreateTripResponse
            builder.tripId(it.newTrip.tripId)
            builder.expectedTotalFare(it.totalPrice.amount.toString())
            builder.expectedFareCurrencyCode(it.totalPrice.currency)
            builder.tealeafTransactionId(it.tealeafTransactionId)

            val totalPrice = Phrase.from(context, R.string.your_card_will_be_charged_template)
                    .put("dueamount", it.tripTotalPayableIncludingFeeIfZeroPayableByPoints().formattedMoneyFromAmountAndCurrencyCode)
                    .format()
            sliderPurchaseTotalText.onNext(totalPrice)
            paymentTypeSelectedHasCardFee.onNext(false)
        }

        priceChangeObservable.subscribe { it as FlightCheckoutResponse
            // TODO - update to totalPrice for subPub support
            val flightTripDetails = it.details
            if (flightTripDetails != null) {
                builder.expectedTotalFare(flightTripDetails.offer.totalFarePrice.amount.toString())
            }
        }

        checkoutParams.subscribe { params ->
            params as FlightCheckoutParams
            if (User.isLoggedIn(context)) {
                params.billingInfo.email = Db.getUser().primaryTraveler.email
            }
            builder.suppressFinalBooking(BookingSuppressionUtils.shouldSuppressFinalBooking(context, R.string.preference_suppress_flight_bookings))
            flightServices.checkout(params.toQueryMap()).subscribe(makeCheckoutResponseObserver())
            email = params.billingInfo.email
        }

        setupCardFeeSubjects()
        showDebitCardsNotAcceptedSubject.onNext(pointOfSale.doesNotAcceptDebitCardsForFlights())
    }

    private fun setupCardFeeSubjects() {
        Observable.combineLatest(obFeeDetailsUrlSubject, selectedFlightChargesFees, {obFeeDetailsUrl, selectedFlightChargesFees ->
            if (Strings.isNotEmpty(selectedFlightChargesFees)) {
                val airlineFeeWithLink =
                        Phrase.from(context, R.string.flights_fee_added_based_on_payment_TEMPLATE)
                                .put("airline_fee_url", obFeeDetailsUrl)
                                .format().toString()
                cardFeeWarningTextSubject.onNext(Html.fromHtml(airlineFeeWithLink))
            }
            else {
                cardFeeWarningTextSubject.onNext(null)
            }
        }).subscribe()

        Observable.combineLatest(
                validFormsOfPaymentSubject,
                selectedCardTypeSubject,
                { validPaymentForms, selectedCardType ->
                    val selectedCardFee = validPaymentForms.filter { it.getPaymentType() == selectedCardType }
                            .filter { !it.fee.isNullOrEmpty() }
                            .firstOrNull()

                    paymentTypeSelectedHasCardFee.onNext(false)
                    if (selectedCardFee != null) {
                        val feeNotZero = !selectedCardFee.getFee().isZero
                        if (feeNotZero) {
                            val cardFeeText = Phrase.from(context, R.string.airline_processing_fee_TEMPLATE)
                                    .put("card_fee", selectedCardFee.formattedFee)
                                    .format().toString()
                            val cardFeeWarningText = Phrase.from(context, R.string.flights_card_fee_warning_TEMPLATE)
                                    .put("card_fee", selectedCardFee.formattedFee)
                                    .format().toString()

                            paymentTypeSelectedHasCardFee.onNext(true)
                            cardFeeTextSubject.onNext(Html.fromHtml(cardFeeText))
                            cardFeeWarningTextSubject.onNext(Html.fromHtml(cardFeeWarningText))
                            cardFeeForSelectedCard.onNext(selectedCardFee)
                        }
                    }
                }).subscribe()
    }

    private fun makeCheckoutResponseObserver(): Observer<FlightCheckoutResponse> {
        return object : Observer<FlightCheckoutResponse> {
            override fun onNext(response: FlightCheckoutResponse) {
                if (response.hasErrors()) {
                    when (response.firstError.errorCode) {
                        ApiError.Code.INVALID_INPUT -> {
                            // TODO
                        }
                        ApiError.Code.PRICE_CHANGE -> {
                            priceChangeObservable.onNext(response)
                        }
                        else -> {
                            checkoutErrorObservable.onNext(ApiError(ApiError.Code.UNKNOWN_ERROR))
                        }
                    }
                } else {
                    Db.getTripBucket().flightV2.flightCheckoutResponse = response
                    bookingSuccessResponse.onNext(Pair(response, email))
                }
            }

            override fun onError(e: Throwable) {
                throw OnErrorNotImplementedException(e)
            }

            override fun onCompleted() {
                // ignore
            }
        }
    }
}
