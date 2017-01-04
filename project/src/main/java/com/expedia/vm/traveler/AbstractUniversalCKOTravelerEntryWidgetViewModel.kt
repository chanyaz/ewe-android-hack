package com.expedia.vm.traveler

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.User
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.bookings.utils.TravelerUtils
import rx.subjects.BehaviorSubject

abstract class AbstractUniversalCKOTravelerEntryWidgetViewModel(context: Context, val travelerIndex: Int) {

    var nameViewModel = TravelerNameViewModel(context)
    var phoneViewModel = TravelerPhoneViewModel(context)
    var emailViewModel = TravelerEmailViewModel(getTraveler(), context)
    val showPhoneNumberObservable = BehaviorSubject.create<Boolean>()
    val showEmailObservable = BehaviorSubject.create<Boolean>(!User.isLoggedIn(context) && travelerIndex == 0)
    var numberOfInvalidFields = BehaviorSubject.create<Int>()

    abstract fun getTraveler(): Traveler
    open fun updateTraveler(traveler: Traveler) {
        nameViewModel.updateTravelerName(traveler.name)
        phoneViewModel.updatePhone(traveler.orCreatePrimaryPhoneNumber)
        emailViewModel.updateEmail(traveler)
    }

    open fun validate(): Boolean {
        val nameValid = nameViewModel.validate()
        val phoneValid = !TravelerUtils.isMainTraveler(travelerIndex) || phoneViewModel.validate()
        val requiresEmail = showEmailObservable.value ?: false
        val emailValid = !requiresEmail || (requiresEmail && emailViewModel.validate())
        numberOfInvalidFields.onNext(nameViewModel.numberOfInvalidFields.value + AccessibilityUtil.getNumberOfInvalidFields(phoneValid, emailValid))
        return nameValid && emailValid && phoneValid
    }

}