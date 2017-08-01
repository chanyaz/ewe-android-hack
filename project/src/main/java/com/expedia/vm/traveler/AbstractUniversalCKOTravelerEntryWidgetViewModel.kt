package com.expedia.vm.traveler

import android.content.Context
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.user.UserStateManager
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.TravelerUtils
import com.expedia.bookings.utils.Ui
import io.reactivex.subjects.BehaviorSubject

abstract class AbstractUniversalCKOTravelerEntryWidgetViewModel(context: Context, val travelerIndex: Int) {

    var nameViewModel = TravelerNameViewModel(context)
    var phoneViewModel = TravelerPhoneViewModel(context)
    var emailViewModel = TravelerEmailViewModel(getTraveler(), context)
    val showPhoneNumberObservable = BehaviorSubject.create<Boolean>()
    val showEmailObservable = BehaviorSubject.create<Boolean>()
    var numberOfInvalidFields = BehaviorSubject.create<Int>()

    protected val userStateManager: UserStateManager

    init {
        userStateManager = Ui.getApplication(context).appComponent().userStateManager()
        showEmailObservable.onNext(!userStateManager.isUserAuthenticated() && travelerIndex == 0)
    }

    abstract fun getTraveler(): Traveler

    open fun updateTraveler(traveler: Traveler) {
        nameViewModel.updateTravelerName(traveler.name)
        phoneViewModel.updatePhone(traveler.orCreatePrimaryPhoneNumber)
        emailViewModel.updateEmail(traveler)
        Db.getWorkingTravelerManager().setWorkingTravelerAndBase(traveler)
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