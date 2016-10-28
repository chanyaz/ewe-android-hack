package com.expedia.vm.rail

import android.content.Context
import android.text.Spanned
import android.text.SpannedString
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.CardFeeResponse
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.TicketDeliveryOption
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.rail.requests.RailCheckoutParams
import com.expedia.bookings.data.rail.responses.RailCheckoutResponse
import com.expedia.bookings.data.rail.responses.RailCreateTripResponse
import com.expedia.bookings.dialog.DialogFactory
import com.expedia.bookings.server.RailCardFeeServiceProvider
import com.expedia.bookings.services.RailServices
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.utils.RetrofitUtils
import com.expedia.bookings.utils.Ui
import com.expedia.util.endlessObserver
import com.squareup.phrase.Phrase
import rx.Observable
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
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
    val updatePricingSubject = PublishSubject.create<RailCreateTripResponse>()
    val cardFeeErrorObservable = PublishSubject.create<Unit>()

    private var currentTicketDeliveryToken: String = ""

    init {
        Ui.getApplication(context).railComponent().inject(this)

        checkoutParams.subscribe { params ->
            railServices.railCheckoutTrip(params, makeCheckoutResponseObserver())
        }

        cardFeeTripResponseSubject.subscribe(tripResponseObservable)
        setupCardFeeSubjects()
    }

    val createTripObserver = endlessObserver<RailCreateTripResponse> { createTripResponse ->
        //TODO update this on price change on checkout #7854
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
                billingInfo?.location?.city, billingInfo?.location?.stateCode ?: null,
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

    fun resetCardFees() {
        cardFeeServiceProvider.resetCardFees(railServices)
        paymentTypeSelectedHasCardFee.onNext(false)
        cardFeeTextSubject.onNext(SpannedString(""))

        val newTripResponse = tripResponseObservable.value
        newTripResponse.selectedCardFees = null

        updateTotalPriceWithTdoFees()
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
                cardFeeTextSubject.onNext(HtmlCompat.fromHtml(cardFeeText))
            } else {
                paymentTypeSelectedHasCardFee.onNext(false)
                cardFeeTextSubject.onNext(SpannedString(""))
            }
        }

        Observable.combineLatest(paymentTypeSelectedHasCardFee, showingPaymentForm, { haveCardFee, showingPaymentForm ->
            haveCardFee && showingPaymentForm
        }).subscribe { displayCardFeesObservable.onNext(it) }
    }

    fun fetchCardFees(cardId: String) {
        if (shouldCallCardFee(cardId)) {
            cardFeeServiceProvider.fetchCardFees(railServices, getTripId(), cardId, currentTicketDeliveryToken, getCardFeesCallback())
        }
    }

    fun updateTicketDeliveryToken(tdo: String) {
        this.currentTicketDeliveryToken = tdo
        updateTotalPriceWithTdoFees()
    }

    private fun shouldCallCardFee(cardId: String): Boolean {
        return getTripId().isNotBlank() && currentTicketDeliveryToken.isNotBlank() && cardId.length >= 6;
    }

    private fun updateCostBreakdownWithFees(cardFee: Money?, totalPriceInclFees: Money?) {
        // add credit card and tdo fees to trip response
        val newTripResponse = tripResponseObservable.value
        newTripResponse.selectedCardFees = cardFee
        newTripResponse.totalPriceIncludingFees = totalPriceInclFees
        newTripResponse.ticketDeliveryFees = newTripResponse.getTicketDeliveryFeeForOption(currentTicketDeliveryToken)

        cardFeeTripResponseSubject.onNext(newTripResponse)
        selectedCardFeeObservable.onNext(cardFee)
        updatePricingSubject.onNext(newTripResponse)
    }

    private fun getCardFeesCallback(): Observer<CardFeeResponse> {

        return object : Observer<CardFeeResponse> {
            override fun onNext(it: CardFeeResponse) {
                if (!it.hasErrors()) {
                    updateCostBreakdownWithFees(it.feePrice, it.tripTotalPrice)
                } else {
                    cardFeeErrorObservable.onNext(Unit)
                }
            }

            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
                cardFeeErrorObservable.onNext(Unit)
            }
        }
    }

    fun updateTotalPriceWithTdoFees() {
        /* To show the correct cost breakdown:
             1. when user selects a TDO with fees but has not entered CC
             2. when cardFee service fails
           We need to manually add the TDO fees to total
         */
        val response = tripResponseObservable.value
        val currencyCode = response.totalPrice.currencyCode
        val tdoFees = response.getTicketDeliveryFeeForOption(currentTicketDeliveryToken)
        var totalPriceInclFees = response.totalPrice
        if (tdoFees != null) {
            totalPriceInclFees = Money(response.totalPrice.amount.plus(tdoFees.amount), currencyCode)
            totalPriceInclFees.formattedPrice = Money.getFormattedMoneyFromAmountAndCurrencyCode(totalPriceInclFees.amount, currencyCode)
        }
        updateCostBreakdownWithFees(null, totalPriceInclFees)
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