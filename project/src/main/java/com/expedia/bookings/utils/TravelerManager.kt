package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.data.AbstractFlightSearchParams
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.user.UserStateManager
import com.expedia.bookings.enums.PassengerCategory
import io.reactivex.subjects.PublishSubject

class TravelerManager(private val userStateManager: UserStateManager) {

    val travelersUpdated = PublishSubject.create<Unit>()

    fun updateDbTravelers(params: AbstractFlightSearchParams) {
        val travelers = Db.getTravelers()
        travelers.clear()
        for (i in 0..params.adults - 1) {
            val traveler = Traveler()
            traveler.passengerCategory = PassengerCategory.ADULT
            traveler.gender = Traveler.Gender.GENDER
            traveler.searchedAge = -1
            travelers.add(traveler)
        }
        for (child in params.children) {
            val traveler = Traveler()
            traveler.passengerCategory = getChildPassengerCategory(child, params)
            traveler.gender = Traveler.Gender.GENDER
            traveler.searchedAge = child
            travelers.add(traveler)
        }
        Db.setTravelers(travelers)
        if (userStateManager.isUserAuthenticated()) {
            onSignIn()
        }
        travelersUpdated.onNext(Unit)
    }

    fun updateRailTravelers() {
        val travelers = Db.getTravelers()
        travelers.clear()
        // Rail only collects Primary Traveler so don't worry about the details of the others.
        if (userStateManager.isUserAuthenticated()) {
            travelers.add(Db.getUser().primaryTraveler)
        } else {
            travelers.add(Traveler())
        }
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

    fun onSignIn() {
        if (userStateManager.isUserAuthenticated() && Db.getTravelers().isNotEmpty()) {
            val primaryTraveler = Db.getUser().primaryTraveler
            primaryTraveler.passengerCategory = Db.getTravelers()[0].passengerCategory
            Db.getTravelers()[0] = primaryTraveler
        }
    }
}
