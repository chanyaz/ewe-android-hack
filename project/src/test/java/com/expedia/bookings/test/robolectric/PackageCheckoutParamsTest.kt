package com.expedia.bookings.test.robolectric

import android.app.Activity
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.Location
import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.StoredCreditCard
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.packages.PackageCheckoutParams
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class PackageCheckoutParamsTest {
    var builder: PackageCheckoutParams.Builder by Delegates.notNull()

    var activity : Activity by Delegates.notNull()

    val expectedMainTravelerKey = "flight.mainFlightPassenger.firstName"
    val expectedAddTravelerKey1 = "flight.associatedFlightPassengers[0].firstName"
    val expectedAddTravelerKey2 = "flight.associatedFlightPassengers[1].firstName"
    val expectedStoredCardKey = "storedCreditCardId"

    val expectedSeatPreferenceKey = "flight.mainFlightPassenger.seatPreference"
    val expectedRedressKey = "flight.mainFlightPassenger.TSARedressNumber"

    @Before
    fun before() {
        builder = PackageCheckoutParams.Builder()
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
    }

    @Test
    fun testTravelerNameValuePair() {
        val travelers = arrayListOf(getTraveler(), getTraveler(), getTraveler())
        val builder = builder.billingInfo(getBillingInfo())
                .travelers(travelers)
                .cvv("123") as PackageCheckoutParams.Builder

        val params = builder.bedType("")
                .expectedFareCurrencyCode("")
                .expectedTotalFare("")
                .tripId("")
                .build()

        assertTrue(params.toQueryMap().containsKey(expectedMainTravelerKey))
        assertTrue(params.toQueryMap().containsKey(expectedAddTravelerKey1))
        assertTrue(params.toQueryMap().containsKey(expectedAddTravelerKey2))
        assertEquals("malcolm", params.toQueryMap().get(expectedMainTravelerKey))
        assertEquals("malcolm", params.toQueryMap().get(expectedAddTravelerKey1))
        assertEquals("malcolm", params.toQueryMap().get(expectedAddTravelerKey2))
    }


    @Test
    fun testRedressAndSeatingPreference() {
        val travelers = arrayListOf(getTraveler())
        val builder = builder.billingInfo(getBillingInfo())
                .travelers(travelers)
                .cvv("123") as PackageCheckoutParams.Builder

        val params = builder.bedType("")
                .expectedFareCurrencyCode("")
                .expectedTotalFare("")
                .tripId("")
                .build()

        assertEquals("WINDOW", params.toQueryMap().get(expectedSeatPreferenceKey))
        assertEquals("123456", params.toQueryMap().get(expectedRedressKey))
    }

    @Test
    fun testStoredCard() {
        val travelers = arrayListOf(getTraveler(), getTraveler(), getTraveler())
        val billing = getBillingInfo()
        billing.storedCard = getStoredCard()
        val builder = builder.billingInfo(billing)
                .travelers(travelers)
                .cvv("123") as PackageCheckoutParams.Builder

        val params = builder.bedType("")
                .expectedFareCurrencyCode("")
                .expectedTotalFare("")
                .tripId("")
                .build()

        assertTrue(params.toQueryMap().containsKey(expectedStoredCardKey))
        assertEquals("12345", params.toQueryMap().get(expectedStoredCardKey))
    }

    fun getBillingInfo(): BillingInfo {
        var info = BillingInfo()
        info.email = "qa-ehcc@mobiata.com"
        info.firstName = "JexperCC"
        info.lastName = "MobiataTestaverde"
        info.nameOnCard = info.firstName + " " + info.lastName
        info.setNumberAndDetectType("4111111111111111")
        info.securityCode = "111"
        info.telephone = "4155555555"
        info.telephoneCountryCode = "1"
        info.expirationDate = LocalDate.now()

        val location = Location()
        location.streetAddress = arrayListOf("123 street")
        location.city = "city"
        location.stateCode = "CA"
        location.countryCode = "US"
        location.postalCode = "12334"
        info.location = location

        return info
    }

    fun getTraveler(): Traveler {
        val traveler = Traveler()
        traveler.firstName = "malcolm"
        traveler.lastName = "nguyen"
        traveler.gender = Traveler.Gender.MALE
        traveler.phoneNumber = "9163355329"
        traveler.birthDate = LocalDate.now().minusYears(18)
        traveler.seatPreference = Traveler.SeatPreference.WINDOW
        traveler.redressNumber = "123456"
        return traveler
    }

    fun getStoredCard() : StoredCreditCard {
        val card = StoredCreditCard()
        card.id = "12345"
        card.cardNumber = "4111111111111111"
        card.type = PaymentType.CARD_VISA
        card.description = "Visa 4111"
        return card
    }
}
