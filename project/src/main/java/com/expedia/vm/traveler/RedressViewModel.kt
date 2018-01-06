package com.expedia.vm.traveler

import com.expedia.bookings.data.Traveler
import com.expedia.bookings.section.InvalidCharacterHelper
import kotlin.properties.Delegates

class RedressViewModel : BaseTravelerValidatorViewModel() {
    var traveler: Traveler by Delegates.notNull()

    override val invalidCharacterMode = InvalidCharacterHelper.Mode.ASCII

    init {
        textSubject.subscribe { traveler.redressNumber = it }
    }

    override fun isValid(): Boolean {
        return true
    }
}
