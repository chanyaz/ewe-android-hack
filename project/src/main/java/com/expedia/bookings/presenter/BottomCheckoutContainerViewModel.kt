package com.expedia.bookings.presenter

import android.content.Context
import com.expedia.bookings.data.TripResponse
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class BottomCheckoutContainerViewModel(context: Context) {

    val sliderPurchaseTotalText = BehaviorSubject.create<CharSequence>()
    val noNetworkObservable = PublishSubject.create<Unit>()
    val checkoutPriceChangeObservable = PublishSubject.create<TripResponse>()
    val slideAllTheWayObservable = PublishSubject.create<Unit>()
    val cvvToCheckoutObservable = PublishSubject.create<Unit>()
    val setSTPLayoutFocusObservable = PublishSubject.create<Boolean>()
    val resetSliderObservable = PublishSubject.create<Unit>()
    val setSTPLayoutVisibilityObservable = PublishSubject.create<Boolean>()
    val toggleBundleTotalDrawableObservable = PublishSubject.create<Boolean>()
    val resetPriceWidgetObservable = PublishSubject.create<Unit>()
    val checkoutButtonEnableObservable = PublishSubject.create<Boolean>()
}