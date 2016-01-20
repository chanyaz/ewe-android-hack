package com.expedia.bookings.data.hotels

import com.expedia.bookings.data.payment.CardDetails
import com.expedia.bookings.data.payment.MiscellaneousParams
import com.expedia.bookings.data.payment.PaymentInfo
import com.expedia.bookings.data.payment.Traveler
import kotlin.collections.listOf

public object HotelCheckoutParamsMock {
        @JvmStatic fun traveler()  = Traveler("Mock", "Travler", "+1", "111111", "mock@mobiata.com")

        @JvmStatic fun checkoutInfo() = HotelCheckoutInfo("checkin-date", "checkout-date")

        @JvmStatic fun paymentInfo(): PaymentInfo {
                val cardDetails = CardDetails(storedCreditCardId = "stored_card_id", cvv = "111", amountOnCard = "123.33", nameOnCard = "Test Card")
                val paymentInfo = PaymentInfo(listOf(cardDetails), null)
                return paymentInfo
        }

        @JvmStatic fun miscellaneousParams(tripId: String) = MiscellaneousParams(true, "tealeafHotel:" + tripId)
}