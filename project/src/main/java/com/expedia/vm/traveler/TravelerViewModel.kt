package com.expedia.vm.traveler

import android.content.Context
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler
import com.expedia.util.endlessObserver
import rx.subjects.BehaviorSubject

open class TravelerViewModel(private val context: Context, val travelerIndex: Int) {
    var nameViewModel = TravelerNameViewModel()
    var phoneViewModel = TravelerPhoneViewModel()
    var tsaViewModel = TravelerTSAViewModel(context)
    var advancedOptionsViewModel = TravelerAdvancedOptionsViewModel()

    val passportCountrySubject = BehaviorSubject.create<String>()
    val showPassportCountryObservable = BehaviorSubject.create<Boolean>()

    val passportCountryObserver = endlessObserver<String> { countryCode ->
        getTraveler().primaryPassportCountry = countryCode
    }

    init {
        updateTraveler(getTraveler())
        showPassportCountryObservable.onNext(shouldShowPassportDropdown())
    }

    open fun updateTraveler(traveler: Traveler) {
        Db.getTravelers()[travelerIndex] = traveler
        nameViewModel.updateTravelerName(traveler.name)
        phoneViewModel.updatePhone(traveler.orCreatePrimaryPhoneNumber)
        tsaViewModel.updateTraveler(traveler)
        advancedOptionsViewModel.updateTraveler(traveler)
    }

    fun validate(): Boolean {
        val nameValid = nameViewModel.validate()
        val phoneValid = phoneViewModel.validate()
        val tsaValid = tsaViewModel.validate()

        val valid = nameValid && phoneValid && tsaValid
        return valid
    }

    open fun getTraveler(): Traveler {
        return Db.getTravelers()[travelerIndex];
    }

    fun shouldShowPassportDropdown(): Boolean {
        val flightOffer = Db.getTripBucket().`package`?.mPackageTripResponse?.packageDetails?.flight?.details?.offer   //holy shit
        return flightOffer != null && (flightOffer.isInternational || flightOffer.isPassportNeeded)
    }
}