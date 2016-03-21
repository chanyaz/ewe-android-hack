package com.expedia.vm.traveler

import com.expedia.bookings.data.Phone
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.section.CommonSectionValidators
import com.expedia.util.endlessObserver
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class PhoneEntryViewModel(var traveler: Traveler) {
    val phoneSubject = BehaviorSubject.create<Phone>()
    val phoneErrorSubject = PublishSubject.create<Int>()

    init {
        val phone = traveler.primaryPhoneNumber
        if (phone != null) {
            phoneSubject.onNext(phone)
        }
    }

    val countryNameObserver = endlessObserver<String> { countryName ->
        traveler.phoneCountryName = countryName
    }

    val countryCodeObserver = endlessObserver<Int> { countryCode ->
        traveler.phoneCountryCode = countryCode.toString()
    }

    val phoneNumberObserver = endlessObserver<String> { phoneNumber ->
        traveler.phoneNumber = phoneNumber
    }

    fun validate(): Boolean {
        val invalidPhoneError = CommonSectionValidators.TELEPHONE_NUMBER_VALIDATOR_STRING.validate(traveler.phoneNumber)
        if (invalidPhoneError != 0) {
            phoneErrorSubject.onNext(invalidPhoneError)
            return false
        }
        return true
    }
}