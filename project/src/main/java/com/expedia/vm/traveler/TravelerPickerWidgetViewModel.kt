package com.expedia.vm.traveler

import com.expedia.bookings.enums.TravelerCheckoutStatus
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class TravelerPickerWidgetViewModel {
    
    var passportRequired = BehaviorSubject.createDefault<Boolean>(false)

    val selectedTravelerSubject = BehaviorSubject.create<TravelerSelectItemViewModel>()

    val refreshStatusObservable = PublishSubject.create<Unit>()

    val currentlySelectedTravelerStatusObservable = PublishSubject.create<TravelerCheckoutStatus>()
}