package com.expedia.vm.traveler

import com.expedia.bookings.data.Phone
import com.expedia.bookings.utils.validation.TravelerValidator
import com.expedia.util.endlessObserver
import com.jakewharton.rxbinding.widget.TextViewAfterTextChangeEvent
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class TravelerPhoneViewModel(var phone: Phone) {
    val phoneNumberSubject = BehaviorSubject.create<String>()
    val phoneCountyCodeSubject = BehaviorSubject.create<String>(phone.countryCode)
    val phoneErrorSubject = PublishSubject.create<Boolean>()

    val countryNameObserver = endlessObserver<String> { countryName ->
        phone.countryName = countryName
    }

    val countryCodeObserver = endlessObserver<Int> { countryCode ->
        phone.countryCode = countryCode.toString()
    }

    val phoneNumberObserver = endlessObserver<TextViewAfterTextChangeEvent>() { phoneNumber ->
        phone.number = phoneNumber.editable().toString()
        phoneNumberSubject.onNext(phone.number)
    }

    init {
        phoneNumberSubject.onNext(if (phone.number.isNullOrEmpty()) "" else phone.number)
    }

    fun validate(): Boolean {
        val validPhone = TravelerValidator.isValidPhone(phone.number)
        phoneErrorSubject.onNext(!validPhone)
        return validPhone
    }
}