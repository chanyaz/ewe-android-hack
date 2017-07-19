package com.expedia.bookings.data.flights

import com.expedia.bookings.data.BaseCheckoutParams
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.Traveler
import org.joda.time.format.ISODateTimeFormat
import java.util.ArrayList
import java.util.HashMap

class FlightCheckoutParams(billingInfo: BillingInfo, travelers: ArrayList<Traveler>, cvv: String, expectedTotalFare: String, expectedFareCurrencyCode: String, suppressFinalBooking: Boolean, tripId: String, val tealeafTransactionId: String, val flightAirlineCodeValue: String?, val frequentFlyerPlanAirlineCodeValue: String?, val frequentFlyerPlanCodeValue: String?, val membershipNumberValue: String?) : BaseCheckoutParams(billingInfo, travelers, cvv, expectedTotalFare, expectedFareCurrencyCode, suppressFinalBooking, tripId) {

    class Builder() : BaseCheckoutParams.Builder() {

        private var tealeafTransactionId: String? = null
        var flightAirlineCodeValue: String? = null
        var frequentFlyerPlanAirlineCodeValue: String? = null
        var frequentFlyerPlanCodeValue: String? = null
        var membershipNumberValue: String? = null

        fun flightAirlineCodeValue(flightAirlineCodeValue: String?): BaseCheckoutParams.Builder {
            this.flightAirlineCodeValue = flightAirlineCodeValue
            return this
        }

        fun frequentFlyerPlanAirlineCodeValue(frequentFlyerPlanAirlineCodeValue: String?): BaseCheckoutParams.Builder {
            this.frequentFlyerPlanAirlineCodeValue = frequentFlyerPlanAirlineCodeValue
            return this
        }

        fun frequentFlyerPlanCodeValue(frequentFlyerPlanCodeValue: String?): BaseCheckoutParams.Builder {
            this.frequentFlyerPlanCodeValue = frequentFlyerPlanCodeValue
            return this
        }

        fun membershipNumberValue(membershipNumberValue: String?): BaseCheckoutParams.Builder {
            this.membershipNumberValue = membershipNumberValue
            return this
        }

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
            return FlightCheckoutParams(billingInfo, travelers, cvv, expectedTotalFare, expectedFareCurrencyCode, suppressFinalBooking, tripId, tealeafTransactionId, flightAirlineCodeValue, frequentFlyerPlanAirlineCodeValue, frequentFlyerPlanCodeValue, membershipNumberValue)
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
            if (!travelers[i].middleName.isNullOrEmpty()) {
                params.put(prefix + "middleName", travelers[i].middleName)
            }
            params.put(prefix + "lastName", travelers[i].lastName)
            params.put(prefix + "phoneCountryCode", travelers[i].phoneCountryCode)
            params.put(prefix + "phone", travelers[i].phoneNumber)
            if (isPrimaryTraveler) {
                params.put(prefix + "email", travelers[i].email)
            }
            params.put(prefix + "birthDate", dtf.print(travelers[i].birthDate))
            params.put(prefix + "gender", travelers[i].gender)

            params.put(prefix + "passengerCategory", travelers[i].passengerCategory)
            if (travelers[i].primaryPassportCountry != null) {
                params.put(prefix + "passportCountryCode", travelers[i].primaryPassportCountry)
            }
            params.put(prefix + "specialAssistanceOption", travelers[i].assistance ?: Traveler.AssistanceType.NONE.name)
            params.put(prefix + "seatPreference", travelers[i].seatPreference.name)
            if (traveler.redressNumber?.isNotEmpty() ?: false) {
                params.put(prefix + "TSARedressNumber", traveler.redressNumber)
            }
            if (traveler.knownTravelerNumber?.isNotEmpty() ?: false) {
                params.put(prefix + "knownTravelerNumber", traveler.knownTravelerNumber)
            }
            if (traveler.hasTuid()) {
                params.put(prefix + "tuid", traveler.tuid.toString())
            }
        }

        val freqFlyerPrefix = "frequentFlyerDetails[0]."
        val flightAirlineCode = freqFlyerPrefix + "flightAirlineCode"
        val frequentFlyerPlanAirlineCode = freqFlyerPrefix + "frequentFlyerPlanAirlineCode"
        val frequentFlyerPlanCode = freqFlyerPrefix + "frequentFlyerPlanCode"
        val membershipNumber = freqFlyerPrefix + "membershipNumber"
        params.put(flightAirlineCode, flightAirlineCodeValue)
        params.put(frequentFlyerPlanAirlineCode, frequentFlyerPlanAirlineCodeValue)
        params.put(frequentFlyerPlanCode, frequentFlyerPlanCodeValue)
        params.put(membershipNumber, membershipNumberValue)

        return params
    }

    override fun toValidParamsMap(): Map<String, Any> {
        val params = HashMap(super.toValidParamsMap())
        params.put("validateWithChildren", true)

        travelers.forEachIndexed { i, traveler ->
            val isPrimaryTraveler = i == 0
            val prefix = if (isPrimaryTraveler) "mainFlightPassenger." else "associatedFlightPassengers[" + (i - 1) + "]."

            params.put(prefix + "firstName", !travelers[i].firstName.isNullOrBlank())
            params.put(prefix + "middleName", !travelers[i].middleName.isNullOrBlank())
            params.put(prefix + "lastName", !travelers[i].lastName.isNullOrBlank())
            params.put(prefix + "phoneCountryCode", !travelers[i].phoneCountryCode.isNullOrBlank())
            params.put(prefix + "phone", !travelers[i].phoneNumber.isNullOrBlank())
            params.put(prefix + "email", !travelers[i].email.isNullOrBlank())
            params.put(prefix + "birthDate", travelers[i].birthDate != null)
            params.put(prefix + "gender", travelers[i].gender == Traveler.Gender.FEMALE || travelers[i].gender == Traveler.Gender.MALE)
            params.put(prefix + "passengerCategory", travelers[i].passengerCategory != null)
            params.put(prefix + "passportCountryCode", !travelers[i].primaryPassportCountry.isNullOrBlank())
            params.put(prefix + "specialAssistanceOption", (travelers[i].assistance ?: true))
            params.put(prefix + "seatPreference", !travelers[i].seatPreference.name.isNullOrBlank())
            params.put(prefix + "TSARedressNumber", !traveler.redressNumber.isNullOrBlank())
            params.put(prefix + "knownTravelerNumber", !traveler.knownTravelerNumber.isNullOrBlank())
        }

        return params
    }
}