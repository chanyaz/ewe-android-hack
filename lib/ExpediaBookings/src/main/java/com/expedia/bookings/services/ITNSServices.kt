package com.expedia.bookings.services

import com.expedia.bookings.data.Courier
import com.expedia.bookings.data.TNSFlight
import com.expedia.bookings.data.TNSUser
import rx.Subscription

interface ITNSServices {
    fun registerForFlights(user: TNSUser, courier: Courier, flights: List<TNSFlight>): Subscription
    fun deregisterForFlights(user: TNSUser, courier: Courier)
}