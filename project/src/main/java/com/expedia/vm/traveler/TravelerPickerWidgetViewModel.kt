package com.expedia.vm.traveler

import com.expedia.bookings.enums.TravelerCheckoutStatus
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class TravelerPickerWidgetViewModel {
    
    var passportRequired = BehaviorSubject.create<Boolean>(false)

    val selectedTravelerSubject = BehaviorSubject.create<TravelerSelectItemViewModel>()

    val refreshStatusObservable = PublishSubject.create<Unit>()

    val currentlySelectedTravelerStatusObservable = PublishSubject.create<TravelerCheckoutStatus>()
}