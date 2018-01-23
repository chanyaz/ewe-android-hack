package com.expedia.bookings.unit.rail

import com.expedia.bookings.data.rail.requests.RailCheckoutParams

object RailCheckoutParamsMock {
    @JvmStatic fun travelers() =
            listOf(RailCheckoutParams.Traveler("Mock", "Travler", "+1", "111111", "mock@mobiata.com"))

    @JvmStatic fun tripDetails() = RailCheckoutParams.TripDetails("Happy_Man", "12123.33", "USD", true)

    @JvmStatic fun paymentInfo(): RailCheckoutParams.PaymentInfo {
        val cardDetails = railCardDetails("123")
        val paymentInfo = RailCheckoutParams.PaymentInfo(listOf(cardDetails))
        return paymentInfo
    }

    @JvmStatic fun railTicketDeliveryStationInfo(): RailCheckoutParams.TicketDeliveryOption {
        return RailCheckoutParams.TicketDeliveryOption("PICK_UP_AT_TICKETING_OFFICE_NONE")
    }

    @JvmStatic fun railTicketDeliveryMailInfo(): RailCheckoutParams.TicketDeliveryOption {
        return RailCheckoutParams.TicketDeliveryOption(deliveryOptionToken = "SEND_BY_EXPRESS_POST_UK",
                deliveryAddressLine1 = "123 Seasme St",
                city = "New York",
                postalCode = "60567",
                country = "USA")
    }

    @JvmStatic fun railCardDetails(cvv: String?): RailCheckoutParams.CardDetails {
        val params = RailCheckoutParams.CardDetails(creditCardNumber = "444444444444444",
                expirationDateYear = "12", expirationDateMonth = "5", cvv = cvv, nameOnCard = "Test Card",
                address1 = "123 Seasme St", city = "New York", postalCode = "60567",
                currencyCode = "USD", country = "USA")
        params.state = "IL"
        return params
    }
}
