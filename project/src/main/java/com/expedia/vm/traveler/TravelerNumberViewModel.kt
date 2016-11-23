package com.expedia.vm.traveler

import android.content.Context
import com.expedia.bookings.data.Traveler
import kotlin.properties.Delegates

class TravelerNumberViewModel(context: Context) : BaseTravelerValidatorViewModel() {
    var traveler: Traveler by Delegates.notNull()

    init {
        textSubject.subscribe { traveler.redressNumber = it }
    }

    override fun isValid(): Boolean {
        return true
    }
}