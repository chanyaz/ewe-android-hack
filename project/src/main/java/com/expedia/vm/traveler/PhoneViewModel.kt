package com.expedia.vm.traveler

import android.content.Context
import com.expedia.bookings.section.CommonSectionValidators
import com.expedia.bookings.section.InvalidCharacterHelper
import com.mobiata.android.validation.ValidationError

class PhoneViewModel(context: Context) : BaseTravelerValidatorViewModel() {

    override val invalidCharacterMode = InvalidCharacterHelper.Mode.ASCII

    override fun isValid(): Boolean {
        return CommonSectionValidators.TELEPHONE_NUMBER_VALIDATOR_STRING.validate(getText()) == ValidationError.NO_ERROR
    }
}