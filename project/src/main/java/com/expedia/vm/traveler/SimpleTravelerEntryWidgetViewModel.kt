package com.expedia.vm.traveler

import android.content.Context
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.utils.TravelerUtils
import com.expedia.bookings.utils.Ui

class SimpleTravelerEntryWidgetViewModel(context: Context, travelerIndex: Int) : BaseTravelerEntryWidgetViewModel(context, travelerIndex) {
    private val userStateManager = Ui.getApplication(context).appComponent().userStateManager()

    init {
        updateTraveler(getTraveler())
    }

    override fun updateTraveler(traveler: Traveler) {
        Db.getTravelers()[travelerIndex] = traveler
        if (userStateManager.isUserAuthenticated()) {
            traveler.email = userStateManager.userSource.user?.primaryTraveler?.email
        }
        nameViewModel.updateTravelerName(traveler.name)
        phoneViewModel.updatePhone(traveler.orCreatePrimaryPhoneNumber)
    }

    override fun validate(): Boolean {
        val nameValid = nameViewModel.validate()
        val phoneValid = !TravelerUtils.isMainTraveler(travelerIndex) || phoneViewModel.validate()
        val emailValid = emailViewModel.validate()
        return nameValid && emailValid && phoneValid
    }
}