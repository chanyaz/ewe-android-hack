package com.expedia.bookings.data.packages

import com.expedia.bookings.data.BaseCheckoutParams
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.Traveler
import org.joda.time.format.DateTimeFormat
import java.util.ArrayList
import java.util.HashMap

class PackageCheckoutParams(billingInfo: BillingInfo, travelers: ArrayList<Traveler>, tripId: String, val bedType: String, cvv: String, expectedTotalFare: String, expectedFareCurrencyCode: String, suppressFinalBooking: Boolean) : BaseCheckoutParams(billingInfo, travelers, cvv, expectedTotalFare, expectedFareCurrencyCode, suppressFinalBooking, tripId) {
    val dtf = DateTimeFormat.forPattern("MM-dd-yyyy")

    class Builder : BaseCheckoutParams.Builder() {
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
            return PackageCheckoutParams(billingInfo, travelers, tripId, bedType, cvv, expectedTotalFare, expectedFareCurrencyCode, suppressFinalBooking)
        }
    }

    override fun toQueryMap(): Map<String, Any> {
        val params = HashMap(super.toQueryMap())
        //HOTEL
        params.put("hotel.bedTypeId", bedType)
        params.put("hotel.primaryContactFullName", travelers[0].fullName)

        //TRAVELERS
        params.put("flight.mainFlightPassenger.email", travelers[0].email)
        travelers.forEachIndexed { i, traveler ->
            val key = if (i == 0) {
                "flight.mainFlightPassenger."
            } else {
                "flight.associatedFlightPassengers[" + (i - 1) + "]."
            }
            params.put(key + "firstName", travelers[i].firstName)
            if (!travelers[i].middleName.isNullOrEmpty()) {
                params.put(key + "middleName", travelers[i].middleName)
            }
            params.put(key + "lastName", travelers[i].lastName)
            params.put(key + "phoneCountryCode", travelers[i].phoneCountryCode)
            params.put(key + "phone", travelers[i].phoneNumber)
            params.put(key + "birthDate", dtf.print(travelers[i].birthDate))
            params.put(key + "gender", travelers[i].gender)
            params.put(key + "passengerCategory", travelers[i].passengerCategory)

            if (travelers[i].primaryPassportCountry != null) {
                params.put(key + "passportCountryCode", travelers[i].primaryPassportCountry)
            }
            if (travelers[i].assistance != null) {
                params.put(key + "specialAssistanceOption", travelers[i].assistance)
            }

            params.put(key + "seatPreference", travelers[i].seatPreference.name)
            if (traveler.redressNumber?.isNotEmpty() ?: false) {
                params.put(key + "TSARedressNumber", traveler.redressNumber)
            }
            if (traveler.knownTravelerNumber?.isNotEmpty() ?: false) {
                params.put(key + "knownTravelerNumber", traveler.knownTravelerNumber)
            }
        }

        //TRIP
        params.put("sendEmailConfirmation", true)

        return params
    }

    override fun toValidParamsMap(): Map<String, Any> {
        val params = HashMap(super.toValidParamsMap())

        //HOTEL
        params.put("hotel.bedTypeId", !bedType.isNullOrBlank())
        params.put("hotel.primaryContactFullName", !travelers[0].fullName.isNullOrBlank())

        //TRAVELERS
        params.put("flight.mainFlightPassenger.email", !travelers[0].email.isNullOrBlank())
        travelers.forEachIndexed { i, traveler ->
            val key = if (i == 0) {
                "flight.mainFlightPassenger."
            } else {
                "flight.associatedFlightPassengers[" + (i - 1) + "]."
            }
            params.put(key + "firstName", !travelers[i].firstName.isNullOrBlank())
            params.put(key + "middleName", !travelers[i].middleName.isNullOrBlank())
            params.put(key + "lastName", !travelers[i].lastName.isNullOrBlank())
            params.put(key + "phoneCountryCode", !travelers[i].phoneCountryCode.isNullOrBlank())
            params.put(key + "phone", !travelers[i].phoneNumber.isNullOrBlank())
            params.put(key + "birthDate", !travelers[i].birthDate?.toString().isNullOrBlank())
            params.put(key + "gender", travelers[i].gender == Traveler.Gender.FEMALE || travelers[i].gender == Traveler.Gender.MALE)
            params.put(key + "passengerCategory", !travelers[i].passengerCategory?.toString().isNullOrBlank())
            params.put(key + "passportCountryCode", travelers[i].primaryPassportCountry.isNullOrBlank())
            params.put(key + "specialAssistanceOption", travelers[i].assistance?.name.isNullOrBlank())
            params.put(key + "seatPreference", !travelers[i].seatPreference?.name.isNullOrBlank())
            params.put(key + "TSARedressNumber", !traveler.redressNumber.isNullOrBlank())
            params.put(key + "knownTravelerNumber", !traveler.knownTravelerNumber.isNullOrBlank())
        }

        return params
    }
}
