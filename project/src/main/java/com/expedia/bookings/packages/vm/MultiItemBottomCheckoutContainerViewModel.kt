package com.expedia.bookings.packages.vm

import io.reactivex.subjects.PublishSubject

class MultiItemBottomCheckoutContainerViewModel {
    val resetPriceWidgetObservable = PublishSubject.create<Unit>()
    val checkoutButtonEnableObservable = PublishSubject.create<Boolean>()
}
