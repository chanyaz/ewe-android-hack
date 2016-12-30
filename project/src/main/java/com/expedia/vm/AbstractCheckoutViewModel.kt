package com.expedia.vm

import android.content.Context
import android.support.v4.content.ContextCompat
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.SpannedString
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.BaseApiResponse
import com.expedia.bookings.data.BaseCheckoutParams
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.CardFeeResponse
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.services.CardFeeService
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Strings
import com.squareup.phrase.Phrase
import rx.Observable
import rx.Observer
import rx.Scheduler
import rx.android.schedulers.AndroidSchedulers
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import rx.subscriptions.CompositeSubscription
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.properties.Delegates

abstract class AbstractCheckoutViewModel(val context: Context) {

    lateinit var paymentViewModel: PaymentViewModel
        @Inject set

    open val builder = BaseCheckoutParams.Builder()

    // Inputs
    val creditCardRequired = PublishSubject.create<Boolean>()
    val travelerCompleted = BehaviorSubject.create<List<Traveler>>()
    val clearTravelers = BehaviorSubject.create<Unit>()
    val paymentCompleted = BehaviorSubject.create<BillingInfo?>()
    val cvvCompleted = BehaviorSubject.create<String>()
    val createTripResponseObservable = BehaviorSubject.create<TripResponse?>()
    val checkoutParams = BehaviorSubject.create<BaseCheckoutParams>()
    val bookingSuccessResponse = PublishSubject.create<Pair<BaseApiResponse, String>>()

    var slideAllTheWayObservable = PublishSubject.create<Unit>()
    val showingPaymentWidgetSubject = PublishSubject.create<Boolean>()
    
    // Outputs
    val checkoutPriceChangeObservable = PublishSubject.create<TripResponse>()
    val noNetworkObservable = PublishSubject.create<Unit>()
    val depositPolicyText = PublishSubject.create<Spanned>()
    val legalText = BehaviorSubject.create<SpannableStringBuilder>()
    val sliderPurchaseTotalText = PublishSubject.create<CharSequence>()
    val accessiblePurchaseButtonContentDescription = PublishSubject.create<CharSequence>()
    val checkoutErrorObservable = PublishSubject.create<ApiError>()
    var email: String by Delegates.notNull()
    val slideToBookA11yActivateObservable = PublishSubject.create<Unit>()
    val cardFeeTripResponse  = PublishSubject.create<TripResponse>()
    val showCheckoutDialogObservable = PublishSubject.create<Boolean>()
    val cardFeeTextSubject = PublishSubject.create<Spanned>()
    val cardFeeWarningTextSubject = PublishSubject.create<Spanned>()
    val paymentTypeSelectedHasCardFee = PublishSubject.create<Boolean>()


    protected var compositeSubscription: CompositeSubscription? = null

    init {
        injectComponents()
        compositeSubscription = CompositeSubscription()
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
    }

    abstract fun injectComponents()
    abstract fun getTripId() : String

    open protected fun getScheduler(): Scheduler = AndroidSchedulers.mainThread()

    fun isValidForBooking() : Boolean {
        return builder.hasValidTravelerAndBillingInfo()
    }

    fun unsubscribeAll() {
        compositeSubscription?.unsubscribe()
    }
}
