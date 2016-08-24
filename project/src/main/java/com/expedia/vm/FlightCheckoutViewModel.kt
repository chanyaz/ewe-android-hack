package com.expedia.vm

import android.content.Context
import android.support.v4.content.ContextCompat
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.CardFeeResponse
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.flights.FlightCheckoutParams
import com.expedia.bookings.data.flights.FlightCheckoutResponse
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.dialog.DialogFactory
import com.expedia.bookings.services.CardFeeService
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.utils.BookingSuppressionUtils
import com.expedia.bookings.utils.RetrofitUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.Ui
import com.mobiata.android.Log
import com.squareup.phrase.Phrase
import rx.Observable
import rx.Observer
import rx.Scheduler
import rx.android.schedulers.AndroidSchedulers
import rx.exceptions.OnErrorNotImplementedException
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

open class FlightCheckoutViewModel(context: Context) : BaseCheckoutViewModel(context) {

    lateinit var flightServices: FlightServices
        @Inject set

    lateinit var cardFeeService: CardFeeService
        @Inject set

    lateinit var paymentViewModel: PaymentViewModel
        @Inject set

    override val builder = FlightCheckoutParams.Builder()

    val selectedCardFeeObservable = PublishSubject.create<Money>()

    // inputs
    val selectedFlightChargesFees = PublishSubject.create<String>()
    val obFeeDetailsUrlSubject = PublishSubject.create<String>()

    // outputs
    val paymentTypeSelectedHasCardFee = PublishSubject.create<Boolean>()
    val cardFeeTextSubject = PublishSubject.create<Spanned>()
    val cardFeeWarningTextSubject = PublishSubject.create<Spanned>()
    val showDebitCardsNotAcceptedSubject = BehaviorSubject.create<Boolean>()
    val receivedCheckoutResponse = PublishSubject.create<Unit>()

    init {
        Ui.getApplication(context).flightComponent().inject(this)

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

        setupCardFeeSubjects()
        showDebitCardsNotAcceptedSubject.onNext(pointOfSale.doesNotAcceptDebitCardsForFlights())

        paymentViewModel.cardBIN
                .debounce(2, TimeUnit.SECONDS, getScheduler())
                .subscribe(customerEnteredPaymentObserver())
        paymentViewModel.storedPaymentInstrumentId.subscribe(customerEnteredPaymentObserver())
    }

    open protected fun getScheduler(): Scheduler = AndroidSchedulers.mainThread()

    private fun customerEnteredPaymentObserver(): Observer<String> {
        return object : Observer<String> {
            override fun onNext(cardId: String) {
                val flightCreateTripResponse = tripResponseObservable.value as FlightCreateTripResponse
                val tripId = flightCreateTripResponse.newTrip.tripId!!
                cardFeeService.getCardFees(tripId, cardId, getCardFeesCallback())
            }

            override fun onError(e: Throwable?) {
                throw OnErrorNotImplementedException(e)
            }
            override fun onCompleted() {}
        }
    }

    private fun getCardFeesCallback(): Observer<CardFeeResponse> {
        return object: Observer<CardFeeResponse> {
            override fun onNext(it: CardFeeResponse) {
                if (!it.hasErrors()) {
                    selectedPaymentHasCardFee(it.feePrice, it.tripTotalPrice)
                }
            }

            override fun onCompleted() {}
            override fun onError(e: Throwable?) {
                Log.e("Card fee service call failed", e)
            }
        }
    }

    private fun selectedPaymentHasCardFee(cardFee: Money, totalPriceInclFees: Money?) {
        // add card fee to trip response
        val newTripResponse = getCurrentCreateTripResponse()
        newTripResponse.selectedCardFees = cardFee
        newTripResponse.totalPriceIncludingFees = totalPriceInclFees
        tripResponseObservable.onNext(newTripResponse)
        selectedCardFeeObservable.onNext(cardFee)
    }

    private fun getCurrentCreateTripResponse(): FlightCreateTripResponse {
        return tripResponseObservable.value as FlightCreateTripResponse
    }

    private fun setupCardFeeSubjects() {
        Observable.combineLatest(obFeeDetailsUrlSubject, selectedFlightChargesFees, {obFeeDetailsUrl, selectedFlightChargesFees ->
            if (Strings.isNotEmpty(selectedFlightChargesFees)) {
                val airlineFeeWithLink =
                        Phrase.from(context, R.string.flights_fee_added_based_on_payment_TEMPLATE)
                                .put("airline_fee_url", obFeeDetailsUrl)
                                .format().toString()
                cardFeeWarningTextSubject.onNext(StrUtils.getSpannableTextByColor(airlineFeeWithLink, ContextCompat.getColor(context, R.color.flight_primary_color), true))
            }
            else {
                cardFeeWarningTextSubject.onNext(null)
            }
        }).subscribe()

        selectedCardFeeObservable
                .debounce(1, TimeUnit.SECONDS, getScheduler()) // subscribe on ui thread as we're affecting ui elements
                .subscribe {
                    selectedCardFee ->
                    if (selectedCardFee != null && !selectedCardFee.isZero) {
                        val cardFeeText = Phrase.from(context, R.string.airline_processing_fee_TEMPLATE)
                                .put("card_fee", selectedCardFee.formattedPrice)
                                .format().toString()
                        val cardFeeWarningText = Phrase.from(context, R.string.flights_card_fee_warning_TEMPLATE)
                                .put("card_fee", selectedCardFee.formattedPrice)
                                .format().toString()
                        paymentTypeSelectedHasCardFee.onNext(true)
                        cardFeeTextSubject.onNext(Html.fromHtml(cardFeeText))
                        cardFeeWarningTextSubject.onNext(Html.fromHtml(cardFeeWarningText))
                    } else {
                        paymentTypeSelectedHasCardFee.onNext(false)
                        cardFeeTextSubject.onNext(Html.fromHtml(""))
                        cardFeeWarningTextSubject.onNext(Html.fromHtml(""))
                    }
                }
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
                }
            }

            override fun onCompleted() {
                // ignore
            }
        }
    }
}
