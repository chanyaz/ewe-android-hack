package com.expedia.vm

import com.expedia.bookings.section.InvalidCharacterHelper
import com.expedia.vm.traveler.BaseTravelerValidatorViewModel

class FrequentFlyerProgramViewModel : BaseTravelerValidatorViewModel() {

    override val invalidCharacterMode = InvalidCharacterHelper.Mode.ANY

    override fun isValid(): Boolean {
        return true
    }
}
