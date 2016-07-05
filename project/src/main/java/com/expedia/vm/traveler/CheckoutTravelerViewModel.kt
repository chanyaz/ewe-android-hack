package com.expedia.vm.traveler

import com.expedia.bookings.data.AbstractFlightSearchParams
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler
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

    fun updateDbTravelers(params: AbstractFlightSearchParams) {
        val travelers = Db.getTravelers()
        travelers.clear()
        for (i in 0..params.adults - 1) {
            val traveler = Traveler()
            traveler.setPassengerCategory(PassengerCategory.ADULT)
            traveler.gender = Traveler.Gender.GENDER
            traveler.searchedAge = -1
            travelers.add(traveler)
        }
        for (child in params.children) {
            val traveler = Traveler()
            traveler.setPassengerCategory(getChildPassengerCategory(child, params))
            traveler.gender = Traveler.Gender.GENDER
            traveler.searchedAge = child
            travelers.add(traveler)
        }
        Db.setTravelers(travelers)
    }

    fun getChildPassengerCategory(childAge: Int, params: AbstractFlightSearchParams): PassengerCategory {
        if (childAge < 2) {
            if (params.infantSeatingInLap) {
                return PassengerCategory.INFANT_IN_LAP
            } else {
                return PassengerCategory.INFANT_IN_SEAT
            }
        } else if (childAge < 12) {
            return PassengerCategory.CHILD
        } else if (childAge < 18) {
            return PassengerCategory.ADULT_CHILD
        }
        throw IllegalArgumentException("\$childAge is not a valid child age")
    }
}
