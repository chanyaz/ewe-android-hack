package com.expedia.vm.traveler

import android.content.Context
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.enums.TravelerCheckoutStatus
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.TravelerUtils
import com.expedia.util.Optional
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class FlightTravelerEntryWidgetViewModel(val context: Context, travelerIndex: Int, val showPassportCountryObservable: BehaviorSubject<Boolean>, travelerCheckoutStatus: TravelerCheckoutStatus) : AbstractUniversalCKOTravelerEntryWidgetViewModel(context, travelerIndex) {
    var tsaViewModel = TravelerTSAViewModel(getTraveler(), context)
    var advancedOptionsViewModel = TravelerAdvancedOptionsViewModel(context)
    val passportCountrySubject = BehaviorSubject.create<Optional<String>>()
    val passportValidSubject = BehaviorSubject.create<Boolean>()
    val passportCountryObserver = BehaviorSubject.create<String>()
    val additionalNumberOfInvalidFields = PublishSubject.create<Int>()
    val flightLegsObservable = PublishSubject.create<List<FlightLeg>>()
    val frequentFlyerPlans = PublishSubject.create<FlightCreateTripResponse.FrequentFlyerPlans>()
    var frequentFlyerAdapterViewModel = FrequentFlyerAdapterViewModel(getTraveler())

    init {
        updateTraveler(getTraveler())
        if (travelerCheckoutStatus != TravelerCheckoutStatus.CLEAN) {
            validate()
        }
        passportCountryObserver.subscribe { countryCode ->
            getTraveler().primaryPassportCountry = countryCode
        }
        showPhoneNumberObservable.onNext(TravelerUtils.isMainTraveler(travelerIndex))
        additionalNumberOfInvalidFields.subscribe { newNumberOfInvalidFields ->
            numberOfInvalidFields.onNext(numberOfInvalidFields.value + newNumberOfInvalidFields)
        }

        frequentFlyerAdapterViewModel = FrequentFlyerAdapterViewModel(getTraveler())
        flightLegsObservable.subscribe(frequentFlyerAdapterViewModel.flightLegsObservable)
        frequentFlyerPlans.subscribe(frequentFlyerAdapterViewModel.frequentFlyerPlans)
    }

    override fun getTraveler(): Traveler {
        return if (Db.sharedInstance.travelers.isNotEmpty()) {
            Db.sharedInstance.travelers[travelerIndex]
        } else {
            Traveler()
        }
    }

    override fun validate(): Boolean {
        val genderValid = tsaViewModel.genderViewModel.validate()
        val birthDateValid = tsaViewModel.dateOfBirthViewModel.validate()
        val requiresPassport = showPassportCountryObservable.value ?: false
        val passportValid = !requiresPassport || (requiresPassport && Strings.isNotEmpty(getTraveler().primaryPassportCountry))
        passportValidSubject.onNext(passportValid)

        val valid = super.validate() && birthDateValid && genderValid && passportValid

        additionalNumberOfInvalidFields.onNext(AccessibilityUtil.getNumberOfInvalidFields(genderValid, birthDateValid, passportValid))
        return valid
    }

    override fun updateTraveler(traveler: Traveler) {
        super.updateTraveler(traveler)
        tsaViewModel.updateTraveler(traveler)
        advancedOptionsViewModel.updateTraveler(traveler)
        passportCountrySubject.onNext(Optional(traveler.primaryPassportCountry))
        frequentFlyerAdapterViewModel?.updateTravelerObservable?.onNext(traveler)
    }
}
