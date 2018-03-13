package com.expedia.bookings.itin.common

import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.data.trips.Trip
import com.expedia.bookings.data.trips.TripUtils
import com.expedia.bookings.data.user.UserStateManager
import java.util.ArrayList

class ItinLaunchScreenHelper {

    companion object ItinLaunchScreenHelper {

        @JvmStatic
        fun showActiveItinLaunchScreenCard(userStateManager: UserStateManager): Boolean {
            return customerHasTripsInNextTwoWeeks() && isUserLoggedIn(userStateManager)
        }

        private fun customerHasTripsInNextTwoWeeks(): Boolean {
            val customersTrips = getCustomerTrips()
            val includeSharedItins = false
            return TripUtils.customerHasTripsInNextTwoWeeks(customersTrips, includeSharedItins)
        }

        private fun getCustomerTrips(): List<Trip> {
            return ArrayList(ItineraryManager.getInstance().trips)
        }

        private fun isUserLoggedIn(userStateManager: UserStateManager): Boolean {
            return userStateManager.isUserAuthenticated()
        }
    }
}
