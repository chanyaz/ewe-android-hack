package com.expedia.vm.rail

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.TicketDeliveryOption
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.rail.requests.RailCheckoutParams
import com.expedia.bookings.data.CardFeeResponse
import android.text.Html
import android.text.Spanned
import com.expedia.bookings.services.RailServices
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.data.rail.responses.RailCheckoutResponse
import com.expedia.bookings.data.rail.responses.RailCreateTripResponse
import com.expedia.bookings.dialog.DialogFactory
import com.expedia.bookings.server.RailCardFeeServiceProvider
import com.expedia.bookings.utils.RetrofitUtils
import com.expedia.util.endlessObserver
import com.squareup.phrase.Phrase
import rx.Observable
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.properties.Delegates

class RailCheckoutViewModel(val context: Context) {
    lateinit var railServices: RailServices
        @Inject set

    lateinit var cardFeeServiceProvider: RailCardFeeServiceProvider
        @Inject set

    val sliderPurchaseTotalText = PublishSubject.create<CharSequence>()
    val checkoutParams = BehaviorSubject.create<RailCheckoutParams>()
    val builder = RailCheckoutParams.Builder()

    val bookingSuccessSubject: PublishSubject<Pair<RailCheckoutResponse, String>> = PublishSubject.create<Pair<RailCheckoutResponse, String>>()
    private var email: String by Delegates.notNull()

    var createTripId: String? = null

    val selectedCardFeeObservable = BehaviorSubject.create<Money>()
    val paymentTypeSelectedHasCardFee = PublishSubject.create<Boolean>()
    val cardFeeTextSubject = PublishSubject.create<Spanned>()
    val tripResponseObservable = BehaviorSubject.create<RailCreateTripResponse>()
    val cardFeeTripResponseSubject = PublishSubject.create<RailCreateTripResponse>()
    val displayCardFeesObservable = PublishSubject.create<Boolean>()
    val showingPaymentForm = PublishSubject.create<Boolean>()

    init {
        Ui.getApplication(context).railComponent().inject(this)

        checkoutParams.subscribe { params ->
            railServices.railCheckoutTrip(params, makeCheckoutResponseObserver())
        }

        cardFeeTripResponseSubject.subscribe(tripResponseObservable)
        setupCardFeeSubjects()
    }

    val createTripObserver = endlessObserver<RailCreateTripResponse> { createTripResponse ->
        val tripDetails = RailCheckoutParams.TripDetails(createTripResponse.tripId,
                createTripResponse.totalPrice.amount.toString(),
                createTripResponse.totalPrice.currencyCode,
                true)
        builder.tripDetails(tripDetails)
        createTripId = createTripResponse.tripId

        tripResponseObservable.onNext(createTripResponse)
    }

    val travelerCompleteObserver = endlessObserver<Traveler> { traveler ->
        val bookingTraveler = RailCheckoutParams.Traveler(traveler.firstName,
                traveler.lastName, traveler.phoneCountryCode, traveler.phoneNumber, traveler.email)
        builder.traveler(listOf(bookingTraveler))
        email = traveler.email
    }

    val ticketDeliveryCompleteObserver = endlessObserver<TicketDeliveryOption> { tdo ->
        val option = RailCheckoutParams.TicketDeliveryOption(tdo.deliveryOptionToken.name,
                tdo.deliveryAddress?.streetAddressString, null, tdo.deliveryAddress?.city,
                tdo.deliveryAddress?.postalCode, tdo.deliveryAddress?.countryCode)
        builder.ticketDeliveryOption(option)
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

    fun fetchCardFees(cardId: String, tdoToken: String) {
        val tripId = getTripId()
        if (tripId.isNotBlank() && cardId.length >= 6) {
            cardFeeServiceProvider.fetchCardFees(railServices, tripId, cardId, tdoToken, getCardFeesCallback())
        }
    }

    fun resetCardFees() {
        cardFeeServiceProvider.resetCardFees(railServices)
        paymentTypeSelectedHasCardFee.onNext(false)
        cardFeeTextSubject.onNext(Html.fromHtml(""))

        val newTripResponse = tripResponseObservable.value
        newTripResponse.selectedCardFees = null
        newTripResponse.totalPriceIncludingFees = null
        cardFeeTripResponseSubject.onNext(newTripResponse)
    }

    fun isValidForBooking(): Boolean {
        return builder.isValid()
    }

    private fun getTripId(): String {
        return if (createTripId != null) createTripId!! else ""
    }

    private fun setupCardFeeSubjects() {
        selectedCardFeeObservable.subscribe { selectedCardFee ->
            if (selectedCardFee != null && !selectedCardFee.isZero) {
                val cardFeeText = Phrase.from(context, R.string.rail_cc_processing_fee_TEMPLATE)
                        .put("card_fee", selectedCardFee.formattedPrice)
                        .format().toString()
                paymentTypeSelectedHasCardFee.onNext(true)
                cardFeeTextSubject.onNext(Html.fromHtml(cardFeeText))
            } else {
                paymentTypeSelectedHasCardFee.onNext(false)
                cardFeeTextSubject.onNext(Html.fromHtml(""))
            }
        }

        Observable.combineLatest(paymentTypeSelectedHasCardFee, showingPaymentForm, { haveCardFee, showingPaymentForm ->
            haveCardFee && showingPaymentForm
        }).subscribe { displayCardFeesObservable.onNext(it) }
    }


    private fun selectedPaymentHasCardFee(cardFee: Money, totalPriceInclFees: Money?) {
        // add card fee to trip response
        val newTripResponse = tripResponseObservable.value
        newTripResponse.selectedCardFees = cardFee
        newTripResponse.totalPriceIncludingFees = totalPriceInclFees
        cardFeeTripResponseSubject.onNext(newTripResponse)
        selectedCardFeeObservable.onNext(cardFee)
    }

    private fun getCardFeesCallback(): Observer<CardFeeResponse> {
        return object : Observer<CardFeeResponse> {
            override fun onNext(it: CardFeeResponse) {
                if (!it.hasErrors()) {
                    selectedPaymentHasCardFee(it.feePrice, it.tripTotalPrice)
                }
            }

            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
                //TODO error handling #9005
            }
        }
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
                    bookingSuccessSubject.onNext(Pair(response, email))
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