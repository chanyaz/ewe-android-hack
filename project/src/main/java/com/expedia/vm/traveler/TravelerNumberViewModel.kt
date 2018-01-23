package com.expedia.vm.traveler

import com.expedia.bookings.data.Traveler
import com.expedia.bookings.section.InvalidCharacterHelper
import kotlin.properties.Delegates

class TravelerNumberViewModel : BaseTravelerValidatorViewModel() {
    var traveler: Traveler by Delegates.notNull()

    override val invalidCharacterMode = InvalidCharacterHelper.Mode.ALPHANUMERIC

    init {
        textSubject.subscribe { traveler.knownTravelerNumber = it }
    }

    override fun isValid(): Boolean {
        return true
    }
}
