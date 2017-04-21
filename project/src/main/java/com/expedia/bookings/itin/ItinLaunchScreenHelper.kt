package com.expedia.bookings.itin

import android.content.Context
import com.expedia.bookings.data.User
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.data.trips.Trip
import com.expedia.bookings.data.trips.TripUtils
import java.util.ArrayList

class ItinLaunchScreenHelper {

    companion object ItinLaunchScreenHelper {

        @JvmStatic
        fun showActiveItinLaunchScreenCard(context: Context): Boolean {
            return customerHasTripsInNextTwoWeeks() && isUserLoggedIn(context)
        }

        @JvmStatic
        fun showGuestItinLaunchScreenCard(context: Context): Boolean {
            return !isUserLoggedIn(context) && getCustomerTrips().isEmpty()
        }

        private fun customerHasTripsInNextTwoWeeks(): Boolean {
            val customersTrips = getCustomerTrips()
            val includeSharedItins = false
            return TripUtils.customerHasTripsInNextTwoWeeks(customersTrips, includeSharedItins)
        }

        private fun getCustomerTrips(): List<Trip> {
            return ArrayList(ItineraryManager.getInstance().trips)
        }

        private fun isUserLoggedIn(context: Context): Boolean {
            return User.isLoggedIn(context)
        }
    }
}
