package com.expedia.vm

import android.content.Context
import android.support.v4.content.ContextCompat
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.BaseApiResponse
import com.expedia.bookings.data.BaseCheckoutParams
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.CardFeeResponse
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.services.CardFeeService
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Strings
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
import kotlin.properties.Delegates

abstract  class BaseCheckoutViewModel(val context: Context) {
    //nullable for hotels/cars/lx which wont implement card fees
    var cardFeeService: CardFeeService? = null
        @Inject set

    lateinit var paymentViewModel: PaymentViewModel
        @Inject set

    open val builder = BaseCheckoutParams.Builder()
    val selectedCardFeeObservable = PublishSubject.create<Money>()
    val paymentTypeSelectedHasCardFee = PublishSubject.create<Boolean>()
    val cardFeeTextSubject = PublishSubject.create<Spanned>()
    val cardFeeWarningTextSubject = PublishSubject.create<Spanned>()
    val selectedFlightChargesFees = PublishSubject.create<String>()
    val obFeeDetailsUrlSubject = PublishSubject.create<String>()

    // Inputs
    val creditCardRequired = PublishSubject.create<Boolean>()
    val travelerCompleted = BehaviorSubject.create<List<Traveler>>()
    val clearTravelers = BehaviorSubject.create<Unit>()
    val paymentCompleted = BehaviorSubject.create<BillingInfo?>()
    val cvvCompleted = BehaviorSubject.create<String>()
    val tripResponseObservable = BehaviorSubject.create<TripResponse>()
    val checkoutParams = BehaviorSubject.create<BaseCheckoutParams>()
    val bookingSuccessResponse = PublishSubject.create<Pair<BaseApiResponse, String>>()

    var slideAllTheWayObservable = PublishSubject.create<Unit>()
    var checkoutTranslationObserver = PublishSubject.create<Float>()
    val showingPaymentWidgetSubject = PublishSubject.create<Boolean>()
    
    // Outputs
    val priceChangeObservable = PublishSubject.create<TripResponse>()
    val noNetworkObservable = PublishSubject.create<Unit>()
    val depositPolicyText = PublishSubject.create<Spanned>()
    val legalText = BehaviorSubject.create<SpannableStringBuilder>()
    val sliderPurchaseTotalText = PublishSubject.create<CharSequence>()
    val accessiblePurchaseButtonContentDescription = PublishSubject.create<CharSequence>()
    val checkoutErrorObservable = PublishSubject.create<ApiError>()
    var email: String by Delegates.notNull()
    val slideToBookA11yActivateObservable = PublishSubject.create<Unit>()

    init {
        injectComponents()
        clearTravelers.subscribe {
            builder.clearTravelers()
        }

        travelerCompleted.subscribe {
            builder.travelers(it)
        }

        paymentCompleted.subscribe { billingInfo ->
            builder.billingInfo(billingInfo)
            builder.cvv(billingInfo?.securityCode)
        }

        cvvCompleted.subscribe {
            builder.cvv(it)
            if (builder.hasValidParams()) {
                checkoutParams.onNext(builder.build())
            }
        }

        paymentViewModel.cardBIN
                .debounce(2, TimeUnit.SECONDS, getScheduler())
                .subscribe(customerEnteredPaymentObserver())
        paymentViewModel.storedPaymentInstrumentId.subscribe(customerEnteredPaymentObserver())

        setupCardFeeSubjects()
    }

    abstract fun injectComponents()

    open protected fun getScheduler(): Scheduler = AndroidSchedulers.mainThread()

    private fun customerEnteredPaymentObserver(): Observer<String> {
        return object : Observer<String> {
            override fun onNext(cardId: String) {
                cardFeeService?.getCardFees(getTripId(), cardId, getCardFeesCallback())
            }

            override fun onError(e: Throwable?) {
                throw OnErrorNotImplementedException(e)
            }
            override fun onCompleted() {}
        }
    }

    abstract fun getTripId() : String

    private fun getCardFeesCallback(): Observer<CardFeeResponse> {
        return object: Observer<CardFeeResponse> {
            override fun onNext(it: CardFeeResponse) {
                if (!it.hasErrors()) {
                    selectedPaymentHasCardFee(it.feePrice, it.tripTotalPrice)
                }
            }

            override fun onCompleted() {}
            override fun onError(e: Throwable?) {}
        }
    }

    abstract fun selectedPaymentHasCardFee(cardFee: Money, totalPriceInclFees: Money?)

    fun isValid() : Boolean {
        return builder.hasValidTravelerAndBillingInfo();
    }

    private fun setupCardFeeSubjects() {
        Observable.combineLatest(obFeeDetailsUrlSubject, selectedFlightChargesFees, { obFeeDetailsUrl, selectedFlightChargesFees ->
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

}