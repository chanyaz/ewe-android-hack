package com.expedia.vm.traveler

import android.content.Context
import com.expedia.bookings.section.CommonSectionValidators
import com.mobiata.android.validation.ValidationError

class FirstNameViewModel(context: Context) : BaseTravelerValidatorViewModel() {
    override fun isValid(): Boolean {
        return CommonSectionValidators.NON_EMPTY_VALIDATOR.validate(getText()) == ValidationError.NO_ERROR
                && hasAllValidChars(getText())
    }
}