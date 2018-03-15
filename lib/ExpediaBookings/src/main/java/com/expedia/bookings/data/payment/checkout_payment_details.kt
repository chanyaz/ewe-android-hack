package com.expedia.bookings.data.payment

data class Traveler(
    val firstName: String,
    val lastName: String,
    val phoneCountryCode: String,
    val phone: String,
    val email: String,
    val expediaEmailOptIn: Boolean
)

data class TripDetails(
    val tripId: String,
    val expectedTotalFare: String,
    val expectedFareCurrencyCode: String,
    val sendEmailConfirmation: Boolean
)

data class CardDetails(
    val postalCode: String? = null,
    val storedCreditCardId: String? = null,
    val creditCardNumber: String? = null,
    val expirationDateYear: String? = null,
    val expirationDateMonth: String? = null,
    val cvv: String?,
    val nameOnCard: String,
    val amountOnCard: String?,
    val storeCreditCardInUserProfile: Boolean = false,
    val currencyCode: String? = null,
    val country: String? = null
)

data class RewardDetails(
    val membershipId: String? = null,
    val paymentInstrumentId: String? = null,
    val programName: ProgramName,
    val amountToChargeInRealCurrency: Float,
    val amountToChargeInVirtualCurrency: Float,
    val rateId: String,
    val currencyCode: String
)

data class PaymentInfo(
    val cards: List<CardDetails> ?,
    val rewards: List<RewardDetails> ?
)

data class MiscellaneousParams(
    val suppressFinalBooking: Boolean,
    val teaLeafTransactionId: String,
    val clientId: String
)
