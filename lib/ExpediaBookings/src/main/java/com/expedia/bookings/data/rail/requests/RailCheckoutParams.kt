package com.expedia.bookings.data.rail.requests

import com.expedia.bookings.data.rail.responses.RailCreateTripResponse

class RailCheckoutParams(val travelers: List<Traveler>,
                         val tripDetails: TripDetails,
                         val paymentInfo: PaymentInfo,
                         val ticketDeliveryOption: TicketDeliveryOption) {
    var messageInfo: MessageInfo? = null

    class Builder {
        private var travelers: List<Traveler> = emptyList()
        private var tripDetails: TripDetails? = null
        private var paymentInfo: PaymentInfo? = null
        private var ticketDeliveryOption: TicketDeliveryOption? = null

        fun traveler(travelers: List<Traveler>): Builder {
            this.travelers = travelers
            return this
        }

        fun clearTravelers(): Builder {
            this.travelers = emptyList()
            return this
        }

        fun tripDetails(tripDetails: TripDetails?): Builder {
            this.tripDetails = tripDetails
            return this
        }

        fun paymentInfo(paymentInfo: PaymentInfo?): Builder {
            this.paymentInfo = paymentInfo
            return this
        }

        fun ticketDeliveryOption(deliveryOption: TicketDeliveryOption?): Builder {
            this.ticketDeliveryOption = deliveryOption
            return this
        }

        fun build(): RailCheckoutParams {
            if (!isValid()) {
                throw IllegalArgumentException()
            }
            return RailCheckoutParams(
                    travelers,
                    tripDetails!!,
                    paymentInfo!!,
                    ticketDeliveryOption!!)
        }

        fun isValid(): Boolean {
            return travelers.isNotEmpty() && paymentInfo != null && paymentInfo!!.cards.isNotEmpty() && tripDetails != null
                    && !paymentInfo!!.cards[0].cvv.isNullOrEmpty() && isValidTDO()
        }

        private fun isValidTDO(): Boolean {
            var isValid = false
            if (ticketDeliveryOption != null) {
                if (!ticketDeliveryOption!!.deliveryOptionToken.equals(RailCreateTripResponse.RailTicketDeliveryOptionToken.PICK_UP_AT_TICKETING_OFFICE_NONE.name)) {
                    // TDO is deliver by mail
                    isValid = !ticketDeliveryOption!!.deliveryAddressLine1.isNullOrEmpty()
                            && !ticketDeliveryOption!!.city.isNullOrEmpty() && !ticketDeliveryOption!!.country.isNullOrEmpty()
                            && !ticketDeliveryOption!!.postalCode.isNullOrEmpty()
                } else {
                    // TDO is pick up at station
                    isValid = true
                }
            }
            return isValid
        }
    }

    // variable names 1-1 mapping to the format the api expects
    data class TripDetails(
            val tripId: String,
            val expectedTotalFare: String,
            val expectedFareCurrencyCode: String,
            val sendConfirmationEmail: Boolean)

    // variable names 1-1 mapping to the format the api expects
    data class Traveler(
            val firstName: String,
            val lastName: String,
            val phoneCountryCode: String,
            val phone: String,
            val email: String)

    data class PaymentInfo(val cards: List<CardDetails>)

    // variable names 1-1 mapping to the format the api expects
    data class CardDetails (
            val creditCardNumber: String? = null,
            val expirationDateYear: String? = null,
            val expirationDateMonth: String? = null,
            val cvv: String?,
            val nameOnCard: String?,
            val address1: String? = null,
            val address2: String? = null,
            val city: String? = null,
            val postalCode: String? = null,
            val currencyCode: String? = null,
            val country: String? = null) {
        var state: String? = null
            set(value) {
                field = if (value.isNullOrBlank()) {
                    null
                } else {
                    value
                }
        }
    }

    data class TicketDeliveryOption(
            val deliveryOptionToken: String,
            val deliveryAddressLine1: String? = null,
            val deliveryAddressLine2: String? = null,
            val city: String? = null,
            val postalCode: String? = null,
            val country: String? = null)
}
