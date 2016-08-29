package com.expedia.vm.traveler

import android.content.Context
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.User
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.TravelerUtils
import rx.subjects.BehaviorSubject

class FlightTravelerViewModel(context: Context, travelerIndex: Int, passportRequired: Boolean) : BaseTravelerViewModel(context, travelerIndex) {
    var tsaViewModel = TravelerTSAViewModel(context)
    var advancedOptionsViewModel = TravelerAdvancedOptionsViewModel()

    val showPhoneNumberObservable = BehaviorSubject.create<Boolean>()
    val passportCountrySubject = BehaviorSubject.create<String>()
    val showPassportCountryObservable = BehaviorSubject.create<Boolean>(passportRequired)
    val showEmailObservable = BehaviorSubject.create<Boolean>(!User.isLoggedIn(context) && travelerIndex == 0)
    val passportValidSubject = BehaviorSubject.create<Boolean>()
    val passportCountryObserver = BehaviorSubject.create<String>()

    init {
        updateTraveler(getTraveler())
        passportCountryObserver.subscribe { countryCode ->
            getTraveler().primaryPassportCountry = countryCode
        }
        showPhoneNumberObservable.onNext(TravelerUtils.isMainTraveler(travelerIndex))
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
        return valid
    }
}