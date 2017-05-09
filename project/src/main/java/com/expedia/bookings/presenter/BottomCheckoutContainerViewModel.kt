package com.expedia.bookings.presenter

import com.expedia.bookings.data.TripResponse
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class BottomCheckoutContainerViewModel() {

    val sliderPurchaseTotalText = BehaviorSubject.create<CharSequence>()
    val accessiblePurchaseButtonContentDescription = PublishSubject.create<CharSequence>()
    val noNetworkObservable = PublishSubject.create<Unit>()
    val checkoutPriceChangeObservable = PublishSubject.create<TripResponse>()
    val slideAllTheWayObservable = PublishSubject.create<Unit>()

}