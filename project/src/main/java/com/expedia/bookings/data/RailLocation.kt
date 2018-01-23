package com.expedia.bookings.data

import com.expedia.bookings.data.rail.responses.RailTicketDeliveryOption
import com.expedia.bookings.widget.SpinnerAdapterWithHint
import kotlin.properties.Delegates

class RailLocation : Location() {
    var ticketDeliveryCountryCodes by Delegates.notNull<List<String>>()
    var tickerDeliveryOptions by Delegates.notNull<List<SpinnerAdapterWithHint.SpinnerItem>>()
    var ticketDeliveryOptionSelected: RailTicketDeliveryOption? = null
}
