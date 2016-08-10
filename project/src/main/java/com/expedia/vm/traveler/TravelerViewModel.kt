package com.expedia.vm.traveler

import android.content.Context
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.TravelerUtils
import com.expedia.util.endlessObserver
import rx.subjects.BehaviorSubject

open class TravelerViewModel(val context: Context, val travelerIndex: Int) {
    var nameViewModel = TravelerNameViewModel(context)
    var phoneViewModel = TravelerPhoneViewModel(context)
    var tsaViewModel = TravelerTSAViewModel(context)
    var advancedOptionsViewModel = TravelerAdvancedOptionsViewModel()

    val showPhoneNumberObservable = BehaviorSubject.create<Boolean>()
    val passportCountrySubject = BehaviorSubject.create<String>()
    val showPassportCountryObservable = BehaviorSubject.create<Boolean>()
    val passportValidSubject = BehaviorSubject.create<Boolean>()

    val passportCountryObserver = endlessObserver<String> { countryCode ->
        getTraveler().primaryPassportCountry = countryCode
    }

    init {
        updateTraveler(getTraveler())
        showPhoneNumberObservable.onNext(TravelerUtils.isMainTraveler(travelerIndex))
    }

    open fun updateTraveler(traveler: Traveler) {
        Db.getTravelers()[travelerIndex] = traveler
        nameViewModel.updateTravelerName(traveler.name)
        phoneViewModel.updatePhone(traveler.orCreatePrimaryPhoneNumber)
        tsaViewModel.updateTraveler(traveler)
        advancedOptionsViewModel.updateTraveler(traveler)
        passportCountrySubject.onNext(traveler.primaryPassportCountry)
    }

    fun validate(): Boolean {
        val nameValid = nameViewModel.validate()
        val phoneValid = !TravelerUtils.isMainTraveler(travelerIndex) || phoneViewModel.validate()
        val tsaValid = tsaViewModel.validate()
        val requiresPassport = showPassportCountryObservable.value ?: false
        val passportValid = !requiresPassport || (requiresPassport && Strings.isNotEmpty(getTraveler().primaryPassportCountry))
        passportValidSubject.onNext(passportValid)
        val valid = nameValid && phoneValid && tsaValid && passportValid
        return valid
    }

    open fun getTraveler(): Traveler {
        return Db.getTravelers()[travelerIndex]
    }
}
