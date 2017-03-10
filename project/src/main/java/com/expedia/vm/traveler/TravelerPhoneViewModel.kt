package com.expedia.vm.traveler

import android.content.Context
import com.expedia.bookings.data.Phone
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.util.endlessObserver
import rx.subjects.BehaviorSubject
import kotlin.properties.Delegates

open class TravelerPhoneViewModel(val context: Context) {

    private var phone: Phone by Delegates.notNull()
    val phoneViewModel = PhoneViewModel(context)
    val phoneCountryCodeSubject = BehaviorSubject.create<String>()
    val phoneCountryNameSubject = BehaviorSubject.create<String>()
    val phoneCountryCodeErrorSubject = BehaviorSubject.create<Boolean>()

    val countryNameObserver = endlessObserver<String> { countryName ->
        phone.countryName = countryName
        phoneCountryNameSubject.onNext(countryName)
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
        phoneCountryNameSubject.onNext(getCountryName(phone))
        phoneViewModel.textSubject.onNext(if (phone.number.isNullOrEmpty()) "" else phone.number)
    }

    open fun validate(): Boolean {
        val validCountryCode = phoneCountryCodeSubject.value.isNullOrBlank()
        phoneCountryCodeErrorSubject.onNext(validCountryCode)
        val validPhone = phoneViewModel.validate()
        return validPhone && validCountryCode
    }

    open fun getCountryName(phone:Phone) : String {
        return if (phone.countryName.isNullOrEmpty()) {
            context.getString(PointOfSale.getPointOfSale().countryNameResId)
        } else {
            phone.countryName
        }
    }
}