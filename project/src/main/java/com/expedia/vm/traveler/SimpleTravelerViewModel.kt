package com.expedia.vm.traveler

import android.content.Context
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.User
import com.expedia.bookings.utils.TravelerUtils

class SimpleTravelerViewModel(context: Context, travelerIndex: Int) : BaseTravelerViewModel(context, travelerIndex) {
    init {
        updateTraveler(getTraveler())
    }

    override fun updateTraveler(traveler: Traveler) {
        Db.getTravelers()[travelerIndex] = traveler
        if (User.isLoggedIn(context)) {
            traveler.email = Db.getUser().primaryTraveler.email
        }
        nameViewModel.updateTravelerName(traveler.name)
        phoneViewModel.updatePhone(traveler.orCreatePrimaryPhoneNumber)
        emailViewModel.updateEmail(traveler)
    }

    override fun validate(): Boolean {
        val nameValid = nameViewModel.validate()
        val phoneValid = !TravelerUtils.isMainTraveler(travelerIndex) || phoneViewModel.validate()
        val emailValid = emailViewModel.validate()
        return nameValid && emailValid && phoneValid
    }
}