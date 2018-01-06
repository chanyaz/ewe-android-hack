package com.expedia.vm.traveler

import com.expedia.bookings.section.CommonSectionValidators
import com.mobiata.android.validation.ValidationError

class MiddleNameViewModel : BaseTravelerValidatorViewModel() {
    override fun isValid(): Boolean {
        return CommonSectionValidators.SUPPORTED_CHARACTER_VALIDATOR_NAMES_STRING.validate(getText()) == ValidationError.NO_ERROR
    }
}
