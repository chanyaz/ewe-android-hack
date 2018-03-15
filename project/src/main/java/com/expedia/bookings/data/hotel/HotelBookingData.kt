package com.expedia.bookings.data.hotel

import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.payment.PaymentSplits
import org.joda.time.LocalDate

data class HotelBookingData(
    val cvv: String?,
    val paymentSplits: PaymentSplits,
    val checkIn: LocalDate,
    val checkOut: LocalDate,
    val primaryTraveler: Traveler,
    val billingInfo: BillingInfo?,
    val isEmailOptedIn: Boolean
)
