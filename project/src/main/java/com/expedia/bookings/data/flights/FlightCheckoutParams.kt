package com.expedia.bookings.data.flights

import com.expedia.bookings.data.BaseCheckoutParams
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler
import org.joda.time.format.ISODateTimeFormat
import java.util.ArrayList
import java.util.HashMap

class FlightCheckoutParams(billingInfo: BillingInfo, travelers: ArrayList<Traveler>, cvv: String, expectedTotalFare: String, expectedFareCurrencyCode: String, suppressFinalBooking: Boolean, tripId: String, val tealeafTransactionId: String) : BaseCheckoutParams(billingInfo, travelers, cvv, expectedTotalFare, expectedFareCurrencyCode, suppressFinalBooking, tripId) {

    class Builder() : BaseCheckoutParams.Builder() {

        private var tealeafTransactionId: String? = null

        override fun build(): FlightCheckoutParams {
            val billingInfo = billingInfo ?: throw IllegalArgumentException()
            val travelers = if (travelers.isEmpty()) throw IllegalArgumentException() else {
                travelers
            }
            val tripId = tripId ?: throw IllegalArgumentException()
            val expectedTotalFare = expectedTotalFare ?: throw IllegalArgumentException()
            val expectedFareCurrencyCode = expectedFareCurrencyCode ?: throw IllegalArgumentException()
            val cvv = cvv ?: throw IllegalArgumentException()
            val tealeafTransactionId = tealeafTransactionId ?: throw IllegalArgumentException()
            return FlightCheckoutParams(billingInfo, travelers, cvv, expectedTotalFare, expectedFareCurrencyCode, suppressFinalBooking, tripId, tealeafTransactionId)
        }

        fun tealeafTransactionId(tealeafTransactionId: String): FlightCheckoutParams.Builder {
            this.tealeafTransactionId = tealeafTransactionId
            return this
        }
    }
    
    override fun toQueryMap(): Map<String, Any> {
        val params = HashMap(super.toQueryMap())
        val dtf = ISODateTimeFormat.date()

        params.put("tlPaymentsSubmitEvent", "1")
        params.put("tealeafTransactionId", this.tealeafTransactionId)
        params.put("validateWithChildren", true)

        //TRAVELERS
        travelers.forEachIndexed { i, traveler ->
            val isPrimaryTraveler = i == 0
            val prefix = if (isPrimaryTraveler) "mainFlightPassenger." else "associatedFlightPassengers[" + (i - 1) + "]."

            params.put(prefix + "firstName", travelers[i].firstName)
            if (travelers[i].middleName.isNotEmpty()) {
                params.put(prefix + "middleName", travelers[i].middleName)
            }
            params.put(prefix + "lastName", travelers[i].lastName)
            params.put(prefix + "phoneCountryCode", travelers[i].phoneCountryCode)
            params.put(prefix + "phone", travelers[i].phoneNumber)
            if (isPrimaryTraveler) {
                params.put(prefix + "email", billingInfo.email)
            }
            params.put(prefix + "birthDate", dtf.print(travelers[i].birthDate))
            params.put(prefix + "gender", travelers[i].gender)

            params.put(prefix + "passengerCategory", travelers[i].getPassengerCategory())
            if (travelers[i].primaryPassportCountry != null) {
                params.put(prefix + "passportCountryCode", travelers[i].primaryPassportCountry)
            }
            params.put(prefix + "specialAssistanceOption", travelers[i].assistance ?: Traveler.AssistanceType.NONE.name)
            params.put(prefix + "seatPreference", travelers[i].seatPreference.name)
            if (traveler.redressNumber.isNotEmpty()) {
                params.put(prefix + "TSARedressNumber", traveler.redressNumber)
            }
            if (traveler.hasTuid()) {
                params.put(prefix + "tuid", traveler.tuid.toString())
            }
        }

        return params
    }
}
