package com.expedia.vm.traveler

import android.content.Context
import com.expedia.bookings.data.Phone
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.widget.TelephoneSpinnerAdapter
import com.expedia.util.endlessObserver
import io.reactivex.subjects.BehaviorSubject
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
        phoneCountryCodeSubject.onNext(getCountryCode(phone))
        countryCodeObserver.onNext(getCountryCode(phone).toInt())
        phoneCountryNameSubject.onNext(getCountryName(phone))
        phoneViewModel.textSubject.onNext(if (phone.number.isNullOrEmpty()) "" else phone.number)
    }

    open fun validate(): Boolean {
        val hasError = phoneCountryCodeSubject.value.isNullOrBlank()
        phoneCountryCodeErrorSubject.onNext(hasError)
        val validPhone = phoneViewModel.validate()
        return validPhone && !hasError
    }

    open fun getCountryName(phone:Phone) : String {
        return if (phone.countryName.isNullOrEmpty()) {
            context.getString(PointOfSale.getPointOfSale().countryNameResId)
        } else {
            phone.countryName
        }
    }

    private fun getCountryCode(phone: Phone) : String {
        return if (phone.countryCode.isNullOrEmpty()) {
            val pointOfSaleCountryName = context.getString(PointOfSale.getPointOfSale().countryNameResId)
            val countryAdapter = TelephoneSpinnerAdapter(context)
            val pointOfSaleCountryCode = countryAdapter.getCountryCodeFromCountryName(pointOfSaleCountryName)
            pointOfSaleCountryCode.toString()
        } else {
            phone.countryCode
        }
    }

    fun getTravelerPhone() : Phone {
        return phone
    }
}