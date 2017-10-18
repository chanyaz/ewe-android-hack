package com.expedia.vm.rail

import android.content.Context
import android.text.Spanned
import android.text.SpannedString
import com.expedia.bookings.ObservableOld
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.CardFeeResponse
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.TicketDeliveryOption
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.rail.requests.RailCheckoutParams
import com.expedia.bookings.data.rail.responses.RailCheckoutResponse
import com.expedia.bookings.data.rail.responses.RailCheckoutResponseWrapper
import com.expedia.bookings.data.rail.responses.RailCreateTripResponse
import com.expedia.bookings.server.RailCardFeeServiceProvider
import com.expedia.bookings.services.RailServices
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.tracking.RailTracking
import com.expedia.bookings.utils.RetrofitUtils
import com.expedia.bookings.utils.Ui
import com.expedia.util.Optional
import com.expedia.util.endlessObserver
import com.squareup.phrase.Phrase
import io.reactivex.Observer
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
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

    val bookingSuccessSubject = PublishSubject.create<Pair<RailCheckoutResponse, String>>()
    private var email: String by Delegates.notNull()

    var createTripId: String? = null

    val selectedCardFeeObservable = BehaviorSubject.create<Optional<Money>>()
    val paymentTypeSelectedHasCardFee = PublishSubject.create<Boolean>()
    val cardFeeTextSubject = PublishSubject.create<Spanned>()
    val tripResponseObservable = BehaviorSubject.create<RailCreateTripResponse>()
    val cardFeeTripResponseSubject = PublishSubject.create<RailCreateTripResponse>()
    val displayCardFeesObservable = PublishSubject.create<Boolean>()
    val showingPaymentForm = PublishSubject.create<Boolean>()
    val updatePricingSubject = PublishSubject.create<RailCreateTripResponse>()
    val cardFeeErrorObservable = PublishSubject.create<Unit>()

    val priceChangeObservable = PublishSubject.create<Pair<Money, Money>>()
    val showCheckoutDialogObservable = PublishSubject.create<Boolean>()
    val checkoutErrorObservable = PublishSubject.create<ApiError>()
    val showNoInternetRetryDialog = PublishSubject.create<Unit>()

    private var currentTicketDeliveryToken: String = ""

    init {
        Ui.getApplication(context).railComponent().inject(this)

        checkoutParams.subscribe { params ->
            showCheckoutDialogObservable.onNext(true)
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

    val paymentCompleteObserver = endlessObserver<Optional<BillingInfo>> { billingInfoOptional ->
        val billingInfo = billingInfoOptional.value
        val cardDetails = RailCheckoutParams.CardDetails(billingInfo?.number.toString(),
                billingInfo?.expirationDate?.year.toString(), billingInfo?.expirationDate?.monthOfYear.toString(),
                billingInfo?.securityCode, billingInfo?.nameOnCard,
                billingInfo?.location?.streetAddressString, null,
                billingInfo?.location?.city, billingInfo?.location?.postalCode, null,
                billingInfo?.location?.countryCode)
        cardDetails.state = billingInfo?.location?.stateCode ?: null
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

    fun trackPriceChange(newTotalPrice: Money, oldTotalPrice: Money) {
        var diffPercentage: Int = 0
        val priceDiff = newTotalPrice.amount.toInt() - oldTotalPrice.amount.toInt()
        if (priceDiff.toInt() != 0) {
            diffPercentage = (priceDiff * 100) / oldTotalPrice.amount.toInt()
        }
        RailTracking().trackPriceChange(diffPercentage)
    }

    private fun getTripId(): String {
        return if (createTripId != null) createTripId!! else ""
    }

    private fun setupCardFeeSubjects() {
        selectedCardFeeObservable.subscribe { selectedCardFeeOptional ->
            val selectedCardFee = selectedCardFeeOptional.value
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

        ObservableOld.combineLatest(paymentTypeSelectedHasCardFee, showingPaymentForm, { haveCardFee, showingPaymentForm ->
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
        return getTripId().isNotBlank() && currentTicketDeliveryToken.isNotBlank() && cardId.length >= 6
    }

    private fun updateCostBreakdownWithFees(cardFee: Money?, totalPriceInclFees: Money?) {
        // add credit card and tdo fees to trip response
        val newTripResponse = tripResponseObservable.value
        newTripResponse.selectedCardFees = cardFee
        newTripResponse.totalPriceIncludingFees = totalPriceInclFees
        newTripResponse.ticketDeliveryFees = newTripResponse.getTicketDeliveryFeeForOption(currentTicketDeliveryToken)

        cardFeeTripResponseSubject.onNext(newTripResponse)
        selectedCardFeeObservable.onNext(Optional(cardFee))
        updatePricingSubject.onNext(newTripResponse)
    }

    private fun getCardFeesCallback(): Observer<CardFeeResponse> {

        return object: DisposableObserver<CardFeeResponse>() {
            override fun onNext(it: CardFeeResponse) {
                if (!it.hasErrors()) {
                    updateCostBreakdownWithFees(it.feePrice, it.tripTotalPrice)
                } else {
                    cardFeeErrorObservable.onNext(Unit)
                    RailTracking().trackCardFeeUnknownError()
                }
            }

            override fun onComplete() {
            }

            override fun onError(e: Throwable) {
                cardFeeErrorObservable.onNext(Unit)
                RailTracking().trackCardFeeApiNoResponseError()
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

    private fun makeCheckoutResponseObserver(): Observer<RailCheckoutResponseWrapper> {
        return object : DisposableObserver<RailCheckoutResponseWrapper>() {
            override fun onNext(response: RailCheckoutResponseWrapper) {
                showCheckoutDialogObservable.onNext(false)
                if (response.checkoutResponse != null) {
                    handleCheckoutReturned(response.checkoutResponse)
                } else if (response.createTripResponse != null) {
                    handleNewCreateTripReturned(response.createTripResponse)
                } else {
                    checkoutErrorObservable.onNext(ApiError(ApiError.Code.UNKNOWN_ERROR))
                    RailTracking().trackCheckoutUnknownError()
                }
            }

            override fun onError(e: Throwable) {
                showCheckoutDialogObservable.onNext(false)
                if (RetrofitUtils.isNetworkError(e)) {
                    showNoInternetRetryDialog.onNext(Unit)
                } else {
                    checkoutErrorObservable.onNext(ApiError(ApiError.Code.UNKNOWN_ERROR))
                }
                RailTracking().trackCheckoutApiNoResponseError()
            }

            override fun onComplete() {
                // ignore
            }
        }
    }

    private fun handleCheckoutReturned(response: RailCheckoutResponse) {
        if (response.hasErrors()) {
            when (response.firstError.errorCode) {
                ApiError.Code.INVALID_INPUT -> {
                    checkoutErrorObservable.onNext(ApiError(ApiError.Code.INVALID_INPUT))
                    RailTracking().trackCheckoutInvalidInputError()
                }
                else -> {
                    checkoutErrorObservable.onNext(ApiError(ApiError.Code.RAIL_UNKNOWN_CKO_ERROR))
                    RailTracking().trackCheckoutUnknownError()
                }
            }
        } else {
            bookingSuccessSubject.onNext(Pair(response, email))
        }
    }

    private fun handleNewCreateTripReturned(response: RailCreateTripResponse) {
        if (response.hasErrors()) {
            when (response.firstError.errorCode) {
                ApiError.Code.PRICE_CHANGE -> {
                    val oldTotalPrice = tripResponseObservable.value.totalPrice
                    priceChangeObservable.onNext(Pair(response.totalPrice, oldTotalPrice))
                    createTripObserver.onNext(response)
                    updatePricingSubject.onNext(response)
                }
                ApiError.Code.INVALID_INPUT -> {
                    checkoutErrorObservable.onNext(ApiError(ApiError.Code.INVALID_INPUT))
                    RailTracking().trackCheckoutInvalidInputError()
                }
                else -> {
                    checkoutErrorObservable.onNext(ApiError(ApiError.Code.RAIL_UNKNOWN_CKO_ERROR))
                    RailTracking().trackCheckoutUnknownError()
                }
            }
        }
    }
}