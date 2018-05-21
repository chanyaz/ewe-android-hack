package com.expedia.vm

import android.content.Context
import android.support.annotation.DrawableRes
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.extensions.subscribeObserver
import com.squareup.phrase.Phrase
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

abstract class BaseErrorViewModel(protected var context: Context) {

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

    private var buttonActionSubscription: Disposable? = null

    protected fun subscribeActionToButtonPress(action: Observer<Unit>) {
        // Unsubscribe current button action
        buttonActionSubscription?.dispose()
        buttonActionSubscription = errorButtonClickedObservable.subscribeObserver(action)
    }

    open fun makeDefaultError() {
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
        errorMessageObservable.onNext(context.getString(R.string.package_error_server))
        buttonOneTextObservable.onNext(context.getString(R.string.retry))
    }

    @DrawableRes
    protected fun defaultErrorDrawable(): Int {
        return R.drawable.error_default
    }

    fun getButtonActionSubscription(): Disposable? {
        return buttonActionSubscription
    }
}
