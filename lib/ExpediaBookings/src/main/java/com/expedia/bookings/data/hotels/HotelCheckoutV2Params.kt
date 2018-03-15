package com.expedia.bookings.data.hotels

import com.expedia.bookings.data.payment.MiscellaneousParams
import com.expedia.bookings.data.payment.PaymentInfo
import com.expedia.bookings.data.payment.Traveler
import com.expedia.bookings.data.payment.TripDetails

class HotelCheckoutV2Params(
    val checkoutInfo: HotelCheckoutInfo,
    val traveler: Traveler,
    val tripDetails: TripDetails,
    val paymentInfo: PaymentInfo,
    val misc: MiscellaneousParams
) {

    class Builder {
        private var checkoutInfo: HotelCheckoutInfo? = null
        private var traveler: Traveler? = null
        private var tripDetails: TripDetails? = null
        private var paymentInfo: PaymentInfo? = null
        private var misc: MiscellaneousParams? = null

        fun checkoutInfo(checkoutInfo: HotelCheckoutInfo?): HotelCheckoutV2Params.Builder {
            this.checkoutInfo = checkoutInfo
            return this
        }

        fun traveler(traveler: Traveler?): HotelCheckoutV2Params.Builder {
            this.traveler = traveler
            return this
        }

        fun tripDetails(tripDetails: TripDetails?): HotelCheckoutV2Params.Builder {
            this.tripDetails = tripDetails
            return this
        }

        fun paymentInfo(paymentInfo: PaymentInfo?): HotelCheckoutV2Params.Builder {
            this.paymentInfo = paymentInfo
            return this
        }

        fun misc(misc: MiscellaneousParams?): HotelCheckoutV2Params.Builder {
            this.misc = misc
            return this
        }

        fun build(): HotelCheckoutV2Params {
            return HotelCheckoutV2Params(checkoutInfo ?: throw IllegalArgumentException(),
                    traveler ?: throw IllegalArgumentException(),
                    tripDetails ?: throw IllegalArgumentException(),
                    paymentInfo ?: throw IllegalArgumentException(),
                    misc ?: throw IllegalArgumentException())
        }
    }
}
