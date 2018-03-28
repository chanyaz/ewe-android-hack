package com.expedia.vm

import android.content.Context
import android.support.v4.content.ContextCompat
import android.text.SpannableStringBuilder
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.CardFeeResponse
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightCheckoutParams
import com.expedia.bookings.data.flights.FlightCheckoutResponse
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.extensions.safeSubscribeOptional
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.tracking.ApiCallFailing
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.utils.BookingSuppressionUtils
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.RetrofitUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Ui
import com.squareup.phrase.Phrase
import io.reactivex.Observer
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

open class FlightCheckoutViewModel(context: Context) : AbstractCardFeeEnabledCheckoutViewModel(context) {

    lateinit var flightServices: FlightServices
        @Inject set

    override val builder = FlightCheckoutParams.Builder()
    // outputs
    val showDebitCardsNotAcceptedSubject = BehaviorSubject.create<Boolean>()
    val showNoInternetRetryDialog = PublishSubject.create<Unit>()
    val isUserEvolableBucketed = AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppFlightsEvolable)

    init {
        val pointOfSale = PointOfSale.getPointOfSale()

        legalText.onNext(SpannableStringBuilder(getFlightBookingStatement(ContextCompat.getColor(context, R.color.flight_primary_color))))

        if (isUserEvolableBucketed) {
            builder.setFeatureOverrideFlag(Constants.FEATURE_EVOLABLE)
        }

        createTripResponseObservable.safeSubscribeOptional { createTripResponse ->
            createTripResponse as FlightCreateTripResponse
            builder.flightLeg(createTripResponse.details.legs)
            builder.tripId(createTripResponse.newTrip!!.tripId)
            builder.expectedTotalFare(createTripResponse.tripTotalPayableIncludingFeeIfZeroPayableByPoints().amount.toString())
            builder.expectedFareCurrencyCode(createTripResponse.details.offer.totalPrice.currency)
            builder.tealeafTransactionId(createTripResponse.tealeafTransactionId)
            builder.suppressFinalBooking(BookingSuppressionUtils.shouldSuppressFinalBooking(context, R.string.preference_suppress_flight_bookings))
        }

        checkoutPriceChangeObservable.subscribe { checkoutResponse ->
            checkoutResponse as FlightCheckoutResponse
            val flightTripDetails = checkoutResponse.details
            builder.expectedTotalFare(flightTripDetails.offer.totalPrice.amount.toString())
        }

        checkoutParams.subscribe { params ->
            params as FlightCheckoutParams
            showCheckoutDialogObservable.onNext(true)
            flightServices.checkout(params.toQueryMap(), params.featureOverride, makeCheckoutResponseObserver())
            email = params.travelers.first().email
        }

        showDebitCardsNotAcceptedSubject.onNext(pointOfSale.doesNotAcceptDebitCardsForFlights())
    }

    override fun injectComponents() {
        Ui.getApplication(context).flightComponent().inject(this)
    }

    override fun getCardFeesCallback(): Observer<CardFeeResponse> {
        return object : DisposableObserver<CardFeeResponse>() {
            override fun onNext(cardFeeResponse: CardFeeResponse) {
                if (!cardFeeResponse.hasErrors()) {
                    // add card fee to trip response
                    val cardFee = cardFeeResponse.feePrice
                    val totalPriceInclFees = cardFeeResponse.tripTotalPrice
                    cardFeeFlexStatus.onNext(cardFeeResponse.flexStatus)
                    val response = createTripResponseObservable.value?.value
                    if (response != null) {
                        val newTripResponse = response as FlightCreateTripResponse
                        newTripResponse.selectedCardFees = cardFee
                        newTripResponse.totalPriceIncludingFees = totalPriceInclFees
                        cardFeeTripResponse.onNext(newTripResponse)
                        selectedCardFeeObservable.onNext(cardFee)
                    }
                }
            }

            override fun onComplete() {
            }

            override fun onError(e: Throwable) {
            }
        }
    }

    override fun resetCardFees() {
        val response = createTripResponseObservable.value?.value
        if (response != null) {
            val newTripResponse = response as FlightCreateTripResponse
            newTripResponse.selectedCardFees = null
            newTripResponse.totalPriceIncludingFees = null
            cardFeeTripResponse.onNext(newTripResponse)
        }
    }

    override fun getTripId(): String {
        if (createTripResponseObservable.value?.value != null) {
            val flightCreateTripResponse = createTripResponseObservable.value.value as FlightCreateTripResponse
            val tripId = flightCreateTripResponse.newTrip!!.tripId!!
            return tripId
        }
        return ""
    }

    fun makeCheckoutResponseObserver(): Observer<FlightCheckoutResponse> {
        return object : DisposableObserver<FlightCheckoutResponse>() {
            override fun onNext(response: FlightCheckoutResponse) {
                showCheckoutDialogObservable.onNext(false)
                if (response.hasErrors()) {
                    when (response.firstError.getErrorCode()) {
                        ApiError.Code.PRICE_CHANGE -> {
                            checkoutPriceChangeObservable.onNext(response)
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
                showCheckoutDialogObservable.onNext(false)
                if (RetrofitUtils.isNetworkError(e)) {
                    showNoInternetRetryDialog.onNext(Unit)
                    FlightsV2Tracking.trackFlightShoppingError(ApiCallFailing.FlightCheckout(Constants.NO_INTERNET_ERROR_CODE))
                }
            }

            override fun onComplete() {
                // ignore
            }
        }
    }

    private fun getFlightBookingStatement(color: Int): CharSequence {
        val flightBookingStatement = Phrase.from(context, R.string.flight_booking_statement_TEMPLATE)
                .put("website_url", PointOfSale.getPointOfSale().websiteUrl)
                .format().toString()
        return StrUtils.getSpannableTextByColor(flightBookingStatement, color, false)
    }
}
