package com.expedia.bookings.presenter

import android.content.Context
import com.expedia.bookings.data.TripResponse
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class BottomCheckoutContainerViewModel(context: Context) {

    val sliderPurchaseTotalText = BehaviorSubject.create<CharSequence>()
    val accessiblePurchaseButtonContentDescription = PublishSubject.create<CharSequence>()
    val noNetworkObservable = PublishSubject.create<Unit>()
    val animateInSlideToPurchaseObservable = PublishSubject.create<Boolean>()
    val checkoutPriceChangeObservable = PublishSubject.create<TripResponse>()
    val accessiblePurchaseButtonClicked = PublishSubject.create<Unit>()

}