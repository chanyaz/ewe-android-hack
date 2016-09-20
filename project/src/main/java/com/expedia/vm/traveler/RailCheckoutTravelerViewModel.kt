package com.expedia.vm.traveler

import android.content.Context
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.enums.TravelerCheckoutStatus
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.validation.TravelerValidator
import javax.inject.Inject

class RailCheckoutTravelerViewModel(context: Context) : BaseCheckoutTravelerViewModel() {
    lateinit var travelerValidator: TravelerValidator
        @Inject set

    init {
        Ui.getApplication(context).travelerComponent().inject(this)
    }

    fun refresh() {
        if (isTravelerEmpty(getTraveler(0))) {
            travelerCompletenessStatus.onNext(TravelerCheckoutStatus.CLEAN)
        } else {
            updateCompletionStatus()
        }
    }

    override fun isValidForBooking(traveler: Traveler, index: Int): Boolean {
        return travelerValidator.isValidForRailBooking(traveler)
    }

    override fun isTravelerEmpty(traveler: Traveler): Boolean {
        return travelerValidator.isTravelerEmpty(traveler)
    }
}