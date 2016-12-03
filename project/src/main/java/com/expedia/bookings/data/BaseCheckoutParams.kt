package com.expedia.bookings.data

import java.util.ArrayList
import java.util.HashMap

open class BaseCheckoutParams(val billingInfo: BillingInfo, val travelers: ArrayList<Traveler>,
                              val cvv: String, val expectedTotalFare: String, val expectedFareCurrencyCode: String,
                              val suppressFinalBooking: Boolean, val tripId: String) {

    open class Builder() {
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
            val cvv = cvv ?: throw IllegalArgumentException()
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
    }

    open fun toQueryMap(): Map<String, Any> {
        val params = HashMap<String, Any>()

        params.put("cvv", cvv)
        params.put("tripId", tripId)
        params.put("expectedTotalFare", expectedTotalFare)
        params.put("expectedFareCurrencyCode", expectedFareCurrencyCode)
        params.put("suppressFinalBooking", true)

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

            params.put("streetAddress", billingInfo.location.streetAddressLine1)
            if (!billingInfo.location.streetAddressLine2.isNullOrEmpty()) {
                params.put("streetAddress2", billingInfo.location.streetAddressLine2)
            }
            params.put("city", billingInfo.location.city)
            params.put("state", billingInfo.location.stateCode)
            params.put("country", billingInfo.location.countryCode)
            params.put("postalCode", billingInfo.location.postalCode)
        }

        return params
    }
}