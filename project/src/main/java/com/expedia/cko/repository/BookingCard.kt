package com.expedia.cko.repository

import com.expedia.bookings.widget.TextView

interface BookingCard {

    val type: BookingType

    val headerText: TextView

}

class BookingType {

}
