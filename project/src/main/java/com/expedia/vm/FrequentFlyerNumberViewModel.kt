package com.expedia.vm

import com.expedia.bookings.section.InvalidCharacterHelper
import com.expedia.vm.traveler.BaseTravelerValidatorViewModel

class FrequentFlyerNumberViewModel : BaseTravelerValidatorViewModel() {

    override val invalidCharacterMode = InvalidCharacterHelper.Mode.ALPHANUMERIC

//    init {
//        textSubject.subscribe { "12345" }
//    }

    override fun isValid(): Boolean {
        return true
    }
}

