package com.expedia.bookings.data.packages

import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.Traveler
import java.util.ArrayList
import java.util.HashMap

data class PackageCheckoutParams(val billingInfo: BillingInfo, val travelers: ArrayList<Traveler>, val tripId: String, val expectedTotalFare: String, val expectedFareCurrencyCode: String, val bedType: String, val cvv: String) {

    class Builder() {
        private var billingInfo: BillingInfo? = null
        private var travelers: ArrayList<Traveler> = ArrayList()
        private var tripId: String? = null
        private var expectedTotalFare: String? = null
        private var expectedFareCurrencyCode: String? = null
        private var cvv: String? = null
        private var bedType: String? = null

        fun billingInfo(billingInfo: BillingInfo?): PackageCheckoutParams.Builder {
            this.billingInfo = billingInfo
            return this
        }

        fun travelers(travelers: ArrayList<Traveler>): PackageCheckoutParams.Builder {
            this.travelers.addAll(travelers)
            return this
        }

        fun tripId(tripId: String?): PackageCheckoutParams.Builder {
            this.tripId = tripId
            return this
        }

        fun expectedTotalFare(expectedTotalFare: String?): PackageCheckoutParams.Builder {
            this.expectedTotalFare = expectedTotalFare
            return this
        }

        fun expectedFareCurrencyCode(expectedFareCurrencyCode: String?): PackageCheckoutParams.Builder {
            this.expectedFareCurrencyCode = expectedFareCurrencyCode
            return this
        }

        fun cvv(cvv: String?): PackageCheckoutParams.Builder {
            this.cvv = cvv
            return this
        }

        fun bedType(bedType: String?): PackageCheckoutParams.Builder {
            this.bedType = bedType
            return this
        }

        fun build(): PackageCheckoutParams {
            val billingInfo = billingInfo ?: throw IllegalArgumentException()
            val travelers = if (travelers.isEmpty()) throw IllegalArgumentException() else {
                travelers
            }
            val tripId = tripId ?: throw IllegalArgumentException()
            val bedType = bedType ?: throw IllegalArgumentException()
            val expectedTotalFare = expectedTotalFare ?: throw IllegalArgumentException()
            val expectedFareCurrencyCode = expectedFareCurrencyCode ?: throw IllegalArgumentException()
            val cvv = cvv ?: throw IllegalArgumentException()
            return PackageCheckoutParams(billingInfo, travelers, tripId, expectedTotalFare, expectedFareCurrencyCode, bedType, cvv)
        }

        fun hasValidParams(): Boolean {
            return travelers.isNotEmpty() &&
                    billingInfo != null &&
                    !cvv.isNullOrEmpty() &&
                    tripId != null &&
                    expectedTotalFare != null &&
                    expectedFareCurrencyCode != null
        }
    }

    fun toQueryMap(): Map<String, Any> {
        val params = HashMap<String, Any>()
        //HOTEL
        params.put("hotel.bedTypeId", bedType)
        params.put("hotel.primaryContactFullName", travelers[0].fullName)

        //TRAVELERS
        params.put("flight.mainFlightPassenger.firstName", travelers[0].firstName)
        params.put("flight.mainFlightPassenger.lastName", travelers[0].lastName)
        params.put("flight.mainFlightPassenger.phoneCountryCode", travelers[0].phoneCountryCode)
        params.put("flight.mainFlightPassenger.phone", travelers[0].phoneNumber)
        //TODO: Get the actual birthday, current traveler form doesn't ask for it
        params.put("flight.mainFlightPassenger.birthDate", "09-06-1989")
        params.put("flight.mainFlightPassenger.gender", travelers[0].gender)
        params.put("flight.mainFlightPassenger.passportCountryCode", travelers[0].primaryPassportCountry)
        params.put("flight.mainFlightPassenger.specialAssistanceOption", travelers[0].assistance)
        params.put("flight.mainFlightPassenger.email", billingInfo.email)

        //TODO: Support other travelers

        //TRIP
        params.put("tripId", tripId)
        params.put("expectedTotalFare", expectedTotalFare)
        params.put("expectedFareCurrencyCode", expectedFareCurrencyCode)
        params.put("sendEmailConfirmation", true)

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

            params.put("streetAddress", billingInfo.location.streetAddress)
            params.put("city", billingInfo.location.city)
            params.put("state", billingInfo.location.stateCode)
            params.put("country", billingInfo.location.countryCode)
            params.put("postalCode", billingInfo.location.postalCode)
        }
        params.put("cvv", cvv)

        //TODO: Toggle this under dev settings
        params.put("suppressFinalBooking", true)

        return params
    }

}