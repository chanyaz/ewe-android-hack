package com.expedia.vm.traveler

import android.content.Context
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler

abstract class BaseTravelerEntryWidgetViewModel(val context: Context, val travelerIndex: Int) {
    var nameViewModel = TravelerNameViewModel(context)
    var phoneViewModel = TravelerPhoneViewModel(context)
    var emailViewModel = TravelerEmailViewModel(getTraveler(), context)

    abstract fun updateTraveler(traveler: Traveler)
    abstract fun validate(): Boolean

    fun getTraveler(): Traveler {
        return Db.getTravelers()[travelerIndex]
    }
}