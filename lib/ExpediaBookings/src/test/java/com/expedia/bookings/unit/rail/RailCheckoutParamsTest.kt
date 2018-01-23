package com.expedia.bookings.unit.rail

import com.expedia.bookings.data.rail.requests.RailCheckoutParams
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RailCheckoutParamsTest {
    val testParamBuilder = RailCheckoutParams.Builder()

    @Test
    fun testIsValidNullPayment() {
        testParamBuilder.traveler(RailCheckoutParamsMock.travelers())
        testParamBuilder.tripDetails(RailCheckoutParamsMock.tripDetails())

        assertFalse(testParamBuilder.isValid(), "Null PaymentInfo should not be valid")
    }

    @Test
    fun testIsValidEmptyPayment() {
        testParamBuilder.traveler(RailCheckoutParamsMock.travelers())
        testParamBuilder.tripDetails(RailCheckoutParamsMock.tripDetails())
        testParamBuilder.paymentInfo(RailCheckoutParams.PaymentInfo(emptyList()))

        assertFalse(testParamBuilder.isValid(), "If PaymentInfo.cardDetails is empty params should not be valid")
    }

    @Test
    fun testIsValidNoCVV() {
        testParamBuilder.traveler(RailCheckoutParamsMock.travelers())
        testParamBuilder.tripDetails(RailCheckoutParamsMock.tripDetails())
        testParamBuilder.paymentInfo(RailCheckoutParams.PaymentInfo(listOf(RailCheckoutParamsMock.railCardDetails(null))))

        assertFalse(testParamBuilder.isValid(), "If paymentInfo.cardDetails is missing a cvv the params should not be valid")
    }

    @Test
    fun testIsValidNoTravelers() {
        testParamBuilder.tripDetails(RailCheckoutParamsMock.tripDetails())
        testParamBuilder.paymentInfo(RailCheckoutParamsMock.paymentInfo())

        assertFalse(testParamBuilder.isValid(), "If travelers empty params should not be valid.")
    }

    @Test
    fun testIsValidNoTripDetails() {
        testParamBuilder.traveler(RailCheckoutParamsMock.travelers())
        testParamBuilder.paymentInfo(RailCheckoutParamsMock.paymentInfo())

        assertFalse(testParamBuilder.isValid(), "If tripDetails empty params should not be valid.")
    }

    @Test
    fun testIsValidTdoNotSet() {
        testParamBuilder.traveler(RailCheckoutParamsMock.travelers())
        testParamBuilder.tripDetails(RailCheckoutParamsMock.tripDetails())
        testParamBuilder.paymentInfo(RailCheckoutParamsMock.paymentInfo())

        assertFalse(testParamBuilder.isValid(), "If tdo not set, params should not be valid.")
    }

    @Test
    fun testIsValidTdoMail() {
        testParamBuilder.traveler(RailCheckoutParamsMock.travelers())
        testParamBuilder.tripDetails(RailCheckoutParamsMock.tripDetails())
        testParamBuilder.paymentInfo(RailCheckoutParamsMock.paymentInfo())
        // is valid with mail delivery
        testParamBuilder.ticketDeliveryOption(RailCheckoutParamsMock.railTicketDeliveryMailInfo())

        assertTrue(testParamBuilder.isValid())
    }

    @Test
    fun testIsValid() {
        testParamBuilder.traveler(RailCheckoutParamsMock.travelers())
        testParamBuilder.tripDetails(RailCheckoutParamsMock.tripDetails())
        testParamBuilder.paymentInfo(RailCheckoutParamsMock.paymentInfo())
        testParamBuilder.ticketDeliveryOption(RailCheckoutParamsMock.railTicketDeliveryStationInfo())

        assertTrue(testParamBuilder.isValid())
    }
}
