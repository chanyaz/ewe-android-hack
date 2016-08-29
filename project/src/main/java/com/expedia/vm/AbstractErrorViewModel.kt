package com.expedia.vm

import android.content.Context
import android.support.annotation.DrawableRes
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.utils.DateFormatUtils
import com.expedia.bookings.utils.StrUtils
import com.squareup.phrase.Phrase
import rx.Observable
import rx.Observer
import rx.Subscription
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import kotlin.properties.Delegates

abstract class AbstractErrorViewModel(protected val context: Context) {

    // Inputs
    val searchApiErrorObserver = PublishSubject.create<ApiError>()
    val checkoutApiErrorObserver = PublishSubject.create<ApiError>()
    val createTripErrorObserverable = PublishSubject.create<ApiError>()

    // Outputs
    val imageObservable = BehaviorSubject.create<Int>()
    val buttonOneTextObservable = BehaviorSubject.create<String>()
    val errorMessageObservable = BehaviorSubject.create<String>()
    val titleObservable = BehaviorSubject.create<String>()
    val subTitleObservable = BehaviorSubject.create<String>()
    val errorButtonClickedObservable = PublishSubject.create<Unit>()
    val clickBack = PublishSubject.create<Unit>()

    // handle different errors
    val defaultErrorObservable = BehaviorSubject.create<Unit>()
    val checkoutCardErrorObservable = BehaviorSubject.create<Unit>()
    val checkoutTravelerErrorObservable = BehaviorSubject.create<Unit>()
    val checkoutUnknownErrorObservable = BehaviorSubject.create<Unit>()
    val productKeyExpiryObservable = BehaviorSubject.create<Unit>()
    val checkoutAlreadyBookedObservable = BehaviorSubject.create<Unit>()
    val checkoutPaymentFailedObservable = BehaviorSubject.create<Unit>()
    val sessionTimeOutObservable = BehaviorSubject.create<Unit>()
    val soldOutObservable = BehaviorSubject.create<Unit>()
    val createTripUnknownErrorObservable = BehaviorSubject.create<Unit>()

    private var buttonActionSubscription: Subscription? = null

    init {
        checkoutApiErrorObserver.subscribe(checkoutApiErrorHandler())
        createTripErrorObserverable.subscribe(createTripErrorHandler())
        searchApiErrorObserver.subscribe(searchErrorHandler())
    }

    abstract protected fun searchErrorHandler(): Observer<ApiError>
    abstract protected fun createTripErrorHandler(): Observer<ApiError>
    abstract protected fun checkoutApiErrorHandler(): Observer<ApiError>

    protected fun subscribeActionToButtonPress(action: Observer<Unit>) {
        // Unsubscribe current button action
        buttonActionSubscription?.unsubscribe()
        buttonActionSubscription = errorButtonClickedObservable.subscribe(action)
    }

    protected fun makeDefaultError() {
        imageObservable.onNext(R.drawable.error_default)
        val message = Phrase.from(context, R.string.error_server_TEMPLATE)
                .put("brand", BuildConfig.brand)
                .format()
                .toString()
        errorMessageObservable.onNext(message)
        buttonOneTextObservable.onNext(context.getString(R.string.retry))
    }

    protected fun couldNotConnectToServerError() {
        imageObservable.onNext(defaultErrorDrawable())
        val message = Phrase.from(context, R.string.error_server_TEMPLATE)
                .put("brand", BuildConfig.brand)
                .format()
                .toString()
        errorMessageObservable.onNext(message)
        buttonOneTextObservable.onNext(context.getString(R.string.retry))
    }

    @DrawableRes
    protected fun defaultErrorDrawable(): Int {
        return R.drawable.error_default
    }
}
