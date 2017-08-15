package com.expedia.vm

import com.expedia.bookings.section.InvalidCharacterHelper
import com.expedia.vm.traveler.BaseTravelerValidatorViewModel

class FrequentFlyerProgramViewModel : BaseTravelerValidatorViewModel() {

    override fun isValid(): Boolean {
        return true
    }
}
