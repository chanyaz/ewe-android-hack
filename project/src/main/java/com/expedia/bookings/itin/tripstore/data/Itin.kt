package com.expedia.bookings.itin.tripstore.data

import com.google.gson.annotations.SerializedName

data class Itin(
        val tripId: String?,
        val webDetailsURL: String?,
        val itineraryReceiptURL: String?,
        val tripNumber: String?,
        val title: String?,
        val startTime: Time?,
        val endTime: Time?,
        val orderNumber: String?,
        val bookingStatus: String?,
        val hotels: List<ItinHotel>?,
        val flights: List<ItinFlight>?,
        val activities: List<ItinLx>?,
        val cars: List<ItinCar>?,
        val cruises: List<ItinCruise>?,
        val rails: List<ItinRail>?,
        val packages: List<ItinPackage>?,
        val rewardList: List<Reward>?,
        val paymentDetails: PaymentDetails?
)

data class Reward(
        val totalPoints: String?,
        val basePoints: String?
)

data class PaymentDetails(
        val priceByFormOfPayment: PriceByFormOfPayment?
)

data class PriceByFormOfPayment(
        @SerializedName("Points")
        val points: Points?,
        @SerializedName("CreditCard")
        val creditCard: CreditCard?
)

data class Points(
        val localizedPaidPrice: String?
)

data class CreditCard(
        val paidLocalizedPrice: String?
)
