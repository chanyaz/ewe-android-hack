package com.expedia.vm.traveler

import android.content.Context
import com.expedia.bookings.data.Phone
import com.expedia.util.endlessObserver
import rx.subjects.BehaviorSubject
import kotlin.properties.Delegates

open class TravelerPhoneViewModel(context: Context) {

    private var phone: Phone by Delegates.notNull()
    val phoneViewModel = PhoneViewModel(context)
    val phoneCountryCodeSubject = BehaviorSubject.create<String>()

    val countryNameObserver = endlessObserver<String> { countryName ->
        phone.countryName = countryName
    }

    val countryCodeObserver = endlessObserver<Int> { countryCode ->
        phone.countryCode = countryCode.toString()
    }

    init {
        phoneViewModel.textSubject.subscribe { phone.number = it }
    }

    fun updatePhone(phone: Phone) {
        this.phone = phone
        phoneCountryCodeSubject.onNext(phone.countryCode)
        phoneViewModel.textSubject.onNext(if (phone.number.isNullOrEmpty()) "" else phone.number)
    }

    open fun validate(): Boolean {
        val validPhone = phoneViewModel.validate()
        return validPhone
    }
}