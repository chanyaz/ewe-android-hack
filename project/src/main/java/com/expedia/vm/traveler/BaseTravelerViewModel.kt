package com.expedia.vm.traveler

import android.content.Context
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.User
import com.expedia.bookings.utils.TravelerUtils

abstract class BaseTravelerViewModel(val context: Context, val travelerIndex: Int) {
    var nameViewModel = TravelerNameViewModel(context)
    var phoneViewModel = TravelerPhoneViewModel(context)
    var emailViewModel = TravelerEmailViewModel(context)

    abstract fun updateTraveler(traveler: Traveler)
    abstract fun validate(): Boolean

    fun getTraveler(): Traveler {
        return Db.getTravelers()[travelerIndex]
    }
}