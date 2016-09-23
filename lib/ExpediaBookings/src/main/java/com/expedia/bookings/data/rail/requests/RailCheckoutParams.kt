package com.expedia.bookings.data.rail.requests

class RailCheckoutParams(val travelers: List<Traveler>,
                         val tripDetails: TripDetails,
                         val paymentInfo: PaymentInfo,
                         val ticketDeliveryOption: TicketDeliveryOption) {

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

        fun ticketDeliveryOption(deliveryOptionToken: String): Builder {
            this.ticketDeliveryOption = TicketDeliveryOption(deliveryOptionToken)
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
                    && !paymentInfo!!.cards[0]?.cvv.isNullOrEmpty() && ticketDeliveryOption != null
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
            val state: String? = null,
            val postalCode: String? = null,
            val currencyCode: String? = null,
            val country:String? = null)

    class TicketDeliveryOption(val deliveryOptionToken: String)
}