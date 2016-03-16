package com.expedia.vm.traveler

import com.expedia.bookings.data.Phone
import com.expedia.bookings.utils.validation.TravelerValidator
import com.expedia.util.endlessObserver
import com.jakewharton.rxbinding.widget.TextViewAfterTextChangeEvent
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import kotlin.properties.Delegates

class TravelerPhoneViewModel() {
    private var phone: Phone by Delegates.notNull()

    val phoneNumberSubject = BehaviorSubject.create<String>()
    val phoneCountyCodeSubject = BehaviorSubject.create<String>()
    val phoneErrorSubject = PublishSubject.create<Boolean>()

    val countryNameObserver = endlessObserver<String> { countryName ->
        phone.countryName = countryName
    }

    val countryCodeObserver = endlessObserver<Int> { countryCode ->
        phone.countryCode = countryCode.toString()
        phoneCountyCodeSubject.onNext(phone.countryCode)
    }

    val phoneNumberObserver = endlessObserver<TextViewAfterTextChangeEvent>() { phoneNumber ->
        phone.number = phoneNumber.editable().toString()
        phoneNumberSubject.onNext(phone.number)
    }

    fun updatePhone(phone: Phone) {
        this.phone = phone
        phoneCountyCodeSubject.onNext(phone.countryCode)
        phoneNumberSubject.onNext(if (phone.number.isNullOrEmpty()) "" else phone.number)
    }

    fun validate(): Boolean {
        val validPhone = TravelerValidator.isValidPhone(phone.number)
        phoneErrorSubject.onNext(!validPhone)
        return validPhone
    }
}