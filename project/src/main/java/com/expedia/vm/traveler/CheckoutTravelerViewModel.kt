package com.expedia.vm.traveler

import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.enums.PassengerCategory
import com.expedia.bookings.utils.validation.TravelerValidator

open class CheckoutTravelerViewModel() {

    open fun validateTravelersComplete(): Boolean {
        val travelerList = getTravelers()

        if (travelerList.isEmpty()) return false

        for (traveler in travelerList) {
            if (!TravelerValidator.isValidForPackageBooking(traveler)) {
                return false
            }
        }
        return true
    }

    open fun areTravelersEmpty() : Boolean {
        val travelerList = getTravelers()
        for (traveler in travelerList) {
            if (!TravelerValidator.isTravelerEmpty(traveler)) {
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
