package com.expedia.vm.traveler

import android.content.Context
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.section.CommonSectionValidators
import com.mobiata.android.validation.ValidationError

open class TravelerEmailViewModel(var traveler: Traveler, val context: Context) : BaseTravelerValidatorViewModel() {

    init {
        textSubject.subscribe { email -> traveler.email = email }
        textSubject.onNext(if (traveler.email.isNullOrEmpty()) "" else traveler.email)
        updateEmail(traveler)
    }

    fun updateEmail(traveler: Traveler) {
        this.traveler = traveler
        textSubject.onNext(if (traveler.email.isNullOrEmpty()) "" else traveler.email)
    }

    override fun isValid(): Boolean {
        return CommonSectionValidators.EMAIL_STRING_VALIDATIOR_STRICT.validate(getText()) == ValidationError.NO_ERROR
    }
}