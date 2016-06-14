package com.expedia.bookings.data.packages

import com.expedia.bookings.data.BaseCheckoutParams
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.Traveler
import org.joda.time.format.DateTimeFormat
import java.util.ArrayList
import java.util.HashMap

class PackageCheckoutParams(billingInfo: BillingInfo, travelers: ArrayList<Traveler>, tripId: String, val bedType: String, cvv: String, expectedTotalFare: String, expectedFareCurrencyCode: String, suppressFinalBooking: Boolean) : BaseCheckoutParams(billingInfo, travelers, cvv, expectedTotalFare, expectedFareCurrencyCode, suppressFinalBooking, tripId) {
    val dtf = DateTimeFormat.forPattern("MM-dd-yyyy");

    class Builder() : BaseCheckoutParams.Builder() {
        private var bedType: String? = null

        fun bedType(bedType: String?): PackageCheckoutParams.Builder {
            this.bedType = bedType
            return this
        }

        override fun build(): PackageCheckoutParams {
            val billingInfo = billingInfo ?: throw IllegalArgumentException()
            val travelers = if (travelers.isEmpty()) throw IllegalArgumentException() else {
                travelers
            }
            val bedType = bedType ?: throw IllegalArgumentException()
            val tripId = tripId ?: throw IllegalArgumentException()
            val expectedTotalFare = expectedTotalFare ?: throw IllegalArgumentException()
            val expectedFareCurrencyCode = expectedFareCurrencyCode ?: throw IllegalArgumentException()
            val cvv = cvv ?: throw IllegalArgumentException()
            return PackageCheckoutParams(billingInfo, travelers, tripId, expectedTotalFare, expectedFareCurrencyCode, bedType, cvv, suppressFinalBooking)
        }
    }

    override fun toQueryMap(): Map<String, Any> {
        val params = HashMap(super.toQueryMap())
        //HOTEL
        params.put("hotel.bedTypeId", bedType)
        params.put("hotel.primaryContactFullName", travelers[0].fullName)

        //TRAVELERS
        params.put("flight.mainFlightPassenger.email", billingInfo.email)
        travelers.forEachIndexed { i, traveler ->
            var key = if (i == 0) {
                "flight.mainFlightPassenger."
            } else {
                "flight.associatedFlightPassengers[" + (i - 1) + "]."
            }
            params.put(key+"firstName", travelers[i].firstName)
            params.put(key+"lastName", travelers[i].lastName)
            params.put(key+"phoneCountryCode", travelers[i].phoneCountryCode)
            params.put(key+"phone", travelers[i].phoneNumber)
            params.put(key+"birthDate", dtf.print(travelers[i].birthDate))
            params.put(key+"gender", travelers[i].gender)
            if (travelers[i].primaryPassportCountry != null) {
                params.put(key+"passportCountryCode", travelers[i].primaryPassportCountry)
            }
            if (travelers[i].assistance != null) {
                params.put(key + "specialAssistanceOption", travelers[i].assistance)
            }
        }

        //TRIP
        params.put("sendEmailConfirmation", true)

        return params
    }
}
