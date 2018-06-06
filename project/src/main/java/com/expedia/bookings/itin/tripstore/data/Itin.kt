package com.expedia.bookings.itin.tripstore.data

import com.expedia.bookings.itin.tripstore.extensions.HasProducts
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
        override val hotels: List<ItinHotel>?,
        override val flights: List<ItinFlight>?,
        override val activities: List<ItinLx>?,
        override val cars: List<ItinCar>?,
        override val cruises: List<ItinCruise>?,
        override val rails: List<ItinRail>?,
        val packages: List<ItinPackage>?,
        val rewardList: List<Reward>?,
        val paymentDetails: PaymentDetails?,
        val paymentSummary: PaymentSummary?,
        val isGuest: Boolean,
        val isShared: Boolean,
        val customerSupport: CustomerSupport?
) : HasProducts

data class Reward(
        val totalPoints: String?,
        val basePoints: String?,
        val bonusPoints: List<PointsDetails>?,
        val logoUrl: String?,
        val viewStatementURL: String?,
        val programName: String?
)

data class PaymentSummary(
        val totalPaidLocalizedPrice: String?,
        val totalPaidTaxAndFeesLocalizedPrice: String?,
        val subTotalPaidLocalizedPrice: String?
)

data class PaymentDetails(
        val localizedNetPricePaidForThisBooking: String?,
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

data class PointsDetails(
        val m_pointValue: String?,
        val m_pointDescription: String?
)

data class ItinTime(
        val localizedFullDate: String?,
        val localizedShortTime: String?,
        val localizedMediumDate: String?
)

data class CustomerSupport(
        val customerSupportPhoneNumberDomestic: String?,
        val customerSupportURL: String?
)

interface ItinLOB
