package com.expedia.bookings.data

import com.expedia.bookings.data.rail.responses.RailCreateTripResponse

// open class for testing
open class TicketDeliveryOption(
    open val deliveryOptionToken: RailCreateTripResponse.RailTicketDeliveryOptionToken,
    open val deliveryAddress: Location? = null
)
