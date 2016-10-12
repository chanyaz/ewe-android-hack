package com.expedia.vm.traveler

import android.content.Context
import com.expedia.bookings.section.CommonSectionValidators
import com.mobiata.android.validation.ValidationError

class MiddleNameViewModel(context: Context) : BaseTravelerValidatorViewModel() {
    override fun isValid(): Boolean {
        if (getText() == null) {
            return true
        }
        return CommonSectionValidators.SUPPORTED_CHARACTER_VALIDATOR_NAMES_STRING.validate(getText()) == ValidationError.NO_ERROR
    }
}