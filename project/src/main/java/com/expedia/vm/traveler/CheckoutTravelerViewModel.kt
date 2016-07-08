package com.expedia.vm.traveler

import com.expedia.bookings.data.AbstractFlightSearchParams
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.enums.PassengerCategory
import android.content.Context
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.validation.TravelerValidator
import javax.inject.Inject

open class CheckoutTravelerViewModel(private val context: Context) {
    lateinit var travelerValidator: TravelerValidator
        @Inject set

    init {
        Ui.getApplication(context).travelerComponent().inject(this)
    }

    open fun validateTravelersComplete(): Boolean {
        val travelerList = getTravelers()

        if (travelerList.isEmpty()) return false

        for (traveler in travelerList) {
            if (!travelerValidator.isValidForPackageBooking(traveler)) {
                return false
            }
        }
        return true
    }

    open fun areTravelersEmpty() : Boolean {
        val travelerList = getTravelers()
        for (traveler in travelerList) {
            if (!travelerValidator.isTravelerEmpty(traveler)) {
                return false
            }
        }
        return true
    }

    open fun getTravelers() : List<Traveler> {
        return Db.getTravelers();
    }

    open fun getTraveler(index: Int) : Traveler {
        val travelerList = Db.getTravelers()
        return travelerList[index]
    }
}
