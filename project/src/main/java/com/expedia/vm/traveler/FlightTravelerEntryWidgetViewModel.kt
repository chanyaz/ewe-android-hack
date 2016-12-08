package com.expedia.vm.traveler

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.User
import com.expedia.bookings.enums.TravelerCheckoutStatus
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.TravelerUtils
import rx.subjects.BehaviorSubject

class FlightTravelerEntryWidgetViewModel(context: Context, travelerIndex: Int, val showPassportCountryObservable: BehaviorSubject<Boolean>, travelerCheckoutStatus: TravelerCheckoutStatus) : BaseTravelerEntryWidgetViewModel(context, travelerIndex) {
    var tsaViewModel = TravelerTSAViewModel(getTraveler(), context)
    var advancedOptionsViewModel = TravelerAdvancedOptionsViewModel(context)

    val showPhoneNumberObservable = BehaviorSubject.create<Boolean>()
    val passportCountrySubject = BehaviorSubject.create<String>()
    val showEmailObservable = BehaviorSubject.create<Boolean>(!User.isLoggedIn(context) && travelerIndex == 0)
    val passportValidSubject = BehaviorSubject.create<Boolean>()
    val passportCountryObserver = BehaviorSubject.create<String>()
    var numberOfInvalidFields = BehaviorSubject.create<Int>()
    val newCheckoutIsEnabled = BehaviorSubject.create<Boolean>(false)

    init {
        updateTraveler(getTraveler())
        passportCountryObserver.subscribe { countryCode ->
            getTraveler().primaryPassportCountry = countryCode
        }
        showPhoneNumberObservable.onNext(TravelerUtils.isMainTraveler(travelerIndex))
        if (travelerCheckoutStatus != TravelerCheckoutStatus.CLEAN) {
            validate()
        }

        newCheckoutIsEnabled.onNext(FeatureToggleUtil.isFeatureEnabled(context, R.string.preference_enable_new_checkout_forms_behavior))
    }

    override fun updateTraveler(traveler: Traveler) {
        Db.getTravelers()[travelerIndex] = traveler
        if (User.isLoggedIn(context)) {
            traveler.email = Db.getUser().primaryTraveler.email
        }
        nameViewModel.updateTravelerName(traveler.name)
        phoneViewModel.updatePhone(traveler.orCreatePrimaryPhoneNumber)
        emailViewModel.updateEmail(traveler)
        tsaViewModel.updateTraveler(traveler)
        advancedOptionsViewModel.updateTraveler(traveler)
        passportCountrySubject.onNext(traveler.primaryPassportCountry)
    }

    override fun validate(): Boolean {
        val nameValid = nameViewModel.validate()
        val phoneValid = !TravelerUtils.isMainTraveler(travelerIndex) || phoneViewModel.validate()
        val tsaValid = tsaViewModel.validate()
        val requiresPassport = showPassportCountryObservable.value ?: false
        val passportValid = !requiresPassport || (requiresPassport && Strings.isNotEmpty(getTraveler().primaryPassportCountry))
        passportValidSubject.onNext(passportValid)
        val requiresEmail = showEmailObservable.value ?: false
        val emailValid = !requiresEmail || (requiresEmail && emailViewModel.validate())
        val valid = nameValid && emailValid && phoneValid && tsaValid && passportValid
        numberOfInvalidFields.onNext(nameViewModel.numberOfInvalidFields.value + AccessibilityUtil.getNumberOfInvalidFields(phoneValid, emailValid, tsaValid, passportValid))
        return valid
    }
}