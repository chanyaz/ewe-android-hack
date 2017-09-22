package com.expedia.bookings.data.flights

import com.expedia.bookings.data.BaseCheckoutParams
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.utils.FlightV2Utils
import org.joda.time.format.ISODateTimeFormat
import java.util.ArrayList
import java.util.HashMap

class FlightCheckoutParams(billingInfo: BillingInfo, travelers: ArrayList<Traveler>, cvv: String, expectedTotalFare: String, expectedFareCurrencyCode: String, suppressFinalBooking: Boolean, tripId: String, val tealeafTransactionId: String, val flightLegs: List<FlightLeg>) : BaseCheckoutParams(billingInfo, travelers, cvv, expectedTotalFare, expectedFareCurrencyCode, suppressFinalBooking, tripId) {

    class Builder : BaseCheckoutParams.Builder() {

        private var tealeafTransactionId: String? = null
        private var flightLegs: List<FlightLeg>? = null

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
            val flightLegs = flightLegs ?: throw IllegalArgumentException()
            return FlightCheckoutParams(billingInfo, travelers, cvv, expectedTotalFare, expectedFareCurrencyCode, suppressFinalBooking, tripId, tealeafTransactionId, flightLegs)
        }

        fun tealeafTransactionId(tealeafTransactionId: String): FlightCheckoutParams.Builder {
            this.tealeafTransactionId = tealeafTransactionId
            return this
        }

        fun flightLeg(flightLegs: List<FlightLeg>?): FlightCheckoutParams.Builder {
            this.flightLegs = flightLegs
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

            if (AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightFrequentFlyerNumber) && flightLegs.isNotEmpty()) {
                val frequentFlyerCards = FlightV2Utils.getAirlineNames(flightLegs)
                frequentFlyerCards.forEachIndexed { index, frequentFlyerCard ->
                    val flyerMembership = traveler.frequentFlyerMemberships[frequentFlyerCard.airlineCode]
                    if (flyerMembership != null) {
                        val frequentFlyerPrefix = if (isPrimaryTraveler) {
                            "mainFlightPassenger.frequentFlyerDetails[" + index + "]."
                        } else {
                            "associatedFlightPassengers[" + (i - 1) + "].frequentFlyerDetails[" + index + "]."
                        }
                        params.put(frequentFlyerPrefix + "flightAirlineCode", flyerMembership.airlineCode)
                        params.put(frequentFlyerPrefix + "membershipNumber", flyerMembership.membershipNumber)
                        params.put(frequentFlyerPrefix + "frequentFlyerPlanCode", flyerMembership.frequentFlyerPlanID)
                        params.put(frequentFlyerPrefix + "frequentFlyerPlanAirlineCode", flyerMembership.planCode)
                    }
                }
            }
        }

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
            if (AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightFrequentFlyerNumber) && flightLegs.isNotEmpty()) {
                val frequentFlyerCards = FlightV2Utils.getAirlineNames(flightLegs)
                frequentFlyerCards.forEachIndexed { index, frequentFlyerCard ->
                    val flyerMembership = traveler.frequentFlyerMemberships[frequentFlyerCard.airlineCode]
                    if (flyerMembership != null) {
                        val frequentFlyerPrefix = if (isPrimaryTraveler) {
                            "mainFlightPassenger.frequentFlyerDetails[" + index + "]."
                        } else {
                            "associatedFlightPassengers[" + (i - 1) + "].frequentFlyerDetails[" + index + "]."
                        }
                        params.put(frequentFlyerPrefix + "flightAirlineCode", flyerMembership.airlineCode.isNullOrBlank())
                        params.put(frequentFlyerPrefix + "membershipNumber", flyerMembership.membershipNumber.isNullOrBlank())
                        params.put(frequentFlyerPrefix + "frequentFlyerPlanCode", flyerMembership.frequentFlyerPlanID.isNullOrBlank())
                        params.put(frequentFlyerPrefix + "frequentFlyerPlanAirlineCode", flyerMembership.planCode.isNullOrBlank())
                    }
                }
            }
        }
        return params
    }
}
