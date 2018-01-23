package com.expedia.vm.traveler

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.enums.TravelerCheckoutStatus
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.validation.TravelerValidator
import javax.inject.Inject

class RailTravelerSummaryViewModel(context: Context) : BaseSummaryViewModel(context) {
    lateinit var travelerValidator: TravelerValidator
        @Inject set

    override fun inject() {
        Ui.getApplication(context).travelerComponent().inject(this)
    }

    override fun getTitle(): String {
        val traveler = getFirstTraveler()
        if (traveler?.fullName.isNullOrEmpty()) {
            return resources.getString(R.string.checkout_enter_traveler_details)
        } else {
            return traveler!!.fullName
        }
    }

    override fun getSubtitle(): String {
        val traveler = getFirstTraveler()
        if (traveler == null || travelerStatusObserver.value != TravelerCheckoutStatus.COMPLETE) {
            return resources.getString(R.string.enter_missing_traveler_details)
        } else {
            return traveler.email!!
        }
    }

    override fun isTravelerEmpty(traveler: Traveler?): Boolean {
        if (traveler != null) {
            return travelerValidator.isTravelerEmpty(traveler)
        }
        return false
    }
}
