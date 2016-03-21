package com.expedia.vm.traveler

import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.enums.PassengerCategory
import com.expedia.bookings.utils.validation.TravelerValidator

open class CheckoutTravelerViewModel() {

    //TODO the search screen should just udpate the travelers for us but right now it doesn't and we need to wait
    // for new search so this 'workaround' exists
    open fun refreshTravelerList(params: PackageSearchParams) {
        val travelerList = Db.getTravelers()
        if (travelerList.isNotEmpty()) {
            travelerList.clear()
        }
        /*
        TODO: this
        val passengers = Db.getTripBucket().getFlight().getFlightTrip().getPassengers()
		val travelerListGenerator = TravelerListGenerator(passengers, travelers)
		val newTravelerList = travelerListGenerator.generateTravelerList()
		Db.setTravelers(newTravelerList)
         */
        for (i in 1..params.adults) {
            val traveler = Traveler()
            traveler.setPassengerCategory(PassengerCategory.ADULT)
            travelerList.add(traveler)
        }
        for (child in params.children) {
            val traveler = Traveler()
            traveler.setPassengerCategory(PassengerCategory.CHILD)
            travelerList.add(traveler)
        }
        Db.setTravelers(travelerList)
    }

    open fun validateTravelersComplete(): Boolean {
        val travelerList = getTravelers()
            for (traveler in travelerList) {
            if (!TravelerValidator.isValidForPackageBooking(traveler)) {
                return false
            }
        }
        return true
    }

    open fun getTravelers(): List<Traveler> {
        return Db.getTravelers();
    }

    open fun getTraveler(index: Int): Traveler {
        val travelerList = Db.getTravelers()
        return travelerList[index]
    }
}
