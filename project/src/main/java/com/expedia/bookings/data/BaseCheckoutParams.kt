package com.expedia.bookings.data

import java.util.ArrayList
import java.util.HashMap

open class BaseCheckoutParams(val billingInfo: BillingInfo, val travelers: ArrayList<Traveler>,
                              val cvv: String, val expectedTotalFare: String, val expectedFareCurrencyCode: String,
                              val suppressFinalBooking: Boolean, val tripId: String) {

    open class Builder {
        protected var billingInfo: BillingInfo? = null
        protected var travelers: ArrayList<Traveler> = ArrayList()
        protected var cvv: String? = null
        protected var tripId: String? = null
        protected var expectedTotalFare: String? = null
        protected var expectedFareCurrencyCode: String? = null
        protected var suppressFinalBooking = true

        fun tripId(tripId: String?): Builder {
            this.tripId = tripId
            return this
        }

        fun expectedTotalFare(expectedTotalFare: String?): Builder {
            this.expectedTotalFare = expectedTotalFare
            return this
        }

        fun expectedFareCurrencyCode(expectedFareCurrencyCode: String?): Builder {
            this.expectedFareCurrencyCode = expectedFareCurrencyCode
            return this
        }

        fun suppressFinalBooking(suppress: Boolean): Builder {
            this.suppressFinalBooking = suppress
            return this
        }

        fun billingInfo(billingInfo: BillingInfo?): Builder {
            this.billingInfo = billingInfo
            return this
        }

        fun travelers(travelers: List<Traveler>): Builder {
            this.travelers.clear()
            this.travelers.addAll(travelers)
            return this
        }

        fun clearTravelers(): Builder {
            this.travelers.clear()
            return this
        }

        fun cvv(cvv: String?): Builder {
            this.cvv = cvv
            return this
        }

        open fun build(): BaseCheckoutParams {
            val billingInfo = billingInfo ?: throw IllegalArgumentException()
            val travelers = if (travelers.isEmpty()) throw IllegalArgumentException() else {
                travelers
            }
            val cvv = cvv
            if (cvv == null || cvv.isEmpty()) {
                throw IllegalArgumentException()
            }
            val tripId = tripId ?: throw IllegalArgumentException()
            val expectedTotalFare = expectedTotalFare ?: throw IllegalArgumentException()
            val expectedFareCurrencyCode = expectedFareCurrencyCode ?: throw IllegalArgumentException()
            return BaseCheckoutParams(billingInfo, travelers, cvv, expectedTotalFare, expectedFareCurrencyCode, suppressFinalBooking, tripId)
        }

        fun hasValidTravelerAndBillingInfo(): Boolean {
            return travelers.isNotEmpty() && billingInfo != null
        }

        fun hasValidParams(): Boolean {
            return travelers.isNotEmpty() &&
                    billingInfo != null &&
                    !cvv.isNullOrEmpty() &&
                    expectedTotalFare != null &&
                    expectedFareCurrencyCode != null
        }

        open fun hasValidCheckoutParams(): Boolean {
            return hasValidTravelers() && hasValidBilling()
        }

        private fun hasValidTravelers(): Boolean {
            var validTravelers = false
            if (travelers.isNotEmpty()) {
                travelers.forEachIndexed { i, traveler ->
                    validTravelers = traveler.hasName() &&
                            traveler.birthDate?.monthOfYear != null &&
                            traveler.birthDate?.year != null &&
                            traveler.gender == Traveler.Gender.FEMALE || traveler.gender == Traveler.Gender.MALE &&
                            (i != 0 || (!traveler.email.isNullOrBlank() &&
                                    !traveler.primaryPhoneNumber.countryCode.isNullOrBlank() &&
                                    !traveler.primaryPhoneNumber.number.isNullOrBlank()))

                    if (!validTravelers) return false
                }
            }
            return validTravelers
        }

        private fun hasValidBilling(): Boolean {
            return if (billingInfo?.hasStoredCard() ?: false) {
                !billingInfo?.storedCard?.id.isNullOrBlank() &&
                !billingInfo?.storedCard?.nameOnCard.isNullOrBlank() &&
                !(billingInfo?.storedCard?.isExpired ?: true)
            } else {
                !billingInfo?.number.isNullOrBlank() &&
                billingInfo?.expirationDate?.monthOfYear != null &&
                billingInfo?.expirationDate?.year != null &&
                !billingInfo?.location?.countryCode.isNullOrBlank() &&
                !billingInfo?.nameOnCard.isNullOrBlank()
            }
        }

        fun hasValidCVV(): Boolean {
            return !cvv.isNullOrEmpty()
        }
    }

    open fun toQueryMap(): Map<String, Any> {
        val params = HashMap<String, Any>()

        params.put("cvv", cvv)
        params.put("tripId", tripId)
        params.put("expectedTotalFare", expectedTotalFare)
        params.put("expectedFareCurrencyCode", expectedFareCurrencyCode)
        params.put("suppressFinalBooking", suppressFinalBooking)
        params.put("storeCreditCardInUserProfile", billingInfo.saveCardToExpediaAccount)

        //BILLING
        val hasStoredCard = billingInfo.hasStoredCard()
        if (hasStoredCard) {
            val storedCard = billingInfo.storedCard
            params.put("storedCreditCardId", storedCard.id)
            params.put("nameOnCard", storedCard.nameOnCard)
        } else {
            params.put("nameOnCard", billingInfo.nameOnCard)
            params.put("creditCardNumber", billingInfo.number)
            params.put("expirationDateYear", billingInfo.expirationDate.year)
            params.put("expirationDateMonth", billingInfo.expirationDate.monthOfYear)

            if (!billingInfo.location.streetAddressLine1.isNullOrEmpty()) {
                params.put("streetAddress", billingInfo.location.streetAddressLine1)
            }
            if (!billingInfo.location.streetAddressLine2.isNullOrEmpty()) {
                params.put("streetAddress2", billingInfo.location.streetAddressLine2)
            }

            if (!billingInfo.location.city.isNullOrEmpty()) {
                params.put("city", billingInfo.location.city)
            }
            if (!billingInfo.location.stateCode.isNullOrEmpty()) {
                params.put("state", billingInfo.location.stateCode)
            }

            if (!billingInfo.location.postalCode.isNullOrEmpty()) {
                params.put("postalCode", billingInfo.location.postalCode)
            }

            params.put("country", billingInfo.location.countryCode)
        }

        return params
    }

    open fun toValidParamsMap(): Map<String, Any> {
        val params = HashMap<String, Any>()

        params.put("cvv", !cvv.isNullOrBlank())
        params.put("tripId", !tripId.isNullOrBlank())
        params.put("expectedTotalFare", !expectedTotalFare.isNullOrBlank())
        params.put("expectedFareCurrencyCode", !expectedFareCurrencyCode.isNullOrBlank())
        params.put("suppressFinalBooking", suppressFinalBooking)
        params.put("storeCreditCardInUserProfile", billingInfo.saveCardToExpediaAccount)

        //BILLING
        val hasStoredCard = billingInfo.hasStoredCard()
        if (hasStoredCard) {
            val storedCard = billingInfo.storedCard
            params.put("storedCreditCardId", !storedCard?.id.isNullOrBlank())
            params.put("nameOnCard", !storedCard?.nameOnCard.isNullOrBlank())
            params.put("creditCardIsExpired", storedCard.isExpired)
        } else {
            params.put("nameOnCard", !billingInfo.nameOnCard.isNullOrBlank())
            params.put("creditCardNumber", !billingInfo.number.isNullOrBlank())
            params.put("expirationDateYear", billingInfo.expirationDate?.year != null)
            params.put("expirationDateMonth", billingInfo.expirationDate?.monthOfYear != null)
        }

        params.put("streetAddress", !billingInfo.location?.streetAddressLine1.isNullOrBlank())
        params.put("streetAddress2", !billingInfo.location?.streetAddressLine2.isNullOrBlank())
        params.put("city", !billingInfo.location?.city.isNullOrBlank())
        params.put("state", !billingInfo.location?.stateCode.isNullOrBlank())
        params.put("postalCode", !billingInfo.location?.postalCode.isNullOrBlank())
        params.put("country", !billingInfo.location?.countryCode.isNullOrBlank())

        return params
    }
}
