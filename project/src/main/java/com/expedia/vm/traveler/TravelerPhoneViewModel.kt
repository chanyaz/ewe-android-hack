package com.expedia.vm.traveler

import android.content.Context
import com.expedia.bookings.data.Phone
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.validation.TravelerValidator
import com.expedia.util.endlessObserver
import com.jakewharton.rxbinding.widget.TextViewAfterTextChangeEvent
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import javax.inject.Inject
import kotlin.properties.Delegates

open class TravelerPhoneViewModel(context: Context) {
    lateinit var travelerValidator: TravelerValidator
        @Inject set

    private var phone: Phone by Delegates.notNull()

    val phoneNumberSubject = BehaviorSubject.create<String>()
    val phoneCountyCodeSubject = BehaviorSubject.create<String>()
    val phoneErrorSubject = PublishSubject.create<Boolean>()

    val countryNameObserver = endlessObserver<String> { countryName ->
        phone.countryName = countryName
    }

    val countryCodeObserver = endlessObserver<Int> { countryCode ->
        phone.countryCode = countryCode.toString()
    }

    val phoneNumberObserver = endlessObserver<String>() { phone.number = it }

    init {
        Ui.getApplication(context).travelerComponent().inject(this)
    }

    fun updatePhone(phone: Phone) {
        this.phone = phone
        phoneCountyCodeSubject.onNext(phone.countryCode)
        phoneNumberSubject.onNext(if (phone.number.isNullOrEmpty()) "" else phone.number)
    }

    open fun validate(): Boolean {
        val validPhone = travelerValidator.isValidPhone(phone.number)
        phoneErrorSubject.onNext(!validPhone)
        return validPhone
    }
}