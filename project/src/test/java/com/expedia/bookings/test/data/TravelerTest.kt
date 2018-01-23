package com.expedia.bookings.test.data

import com.expedia.bookings.data.Traveler
import com.expedia.bookings.enums.PassengerCategory
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.joda.time.LocalDate
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class TravelerTest {
    var testTraveler = Traveler()

    val infantBirthDate = LocalDate.now().minusYears(1)
    val adultBirthDate = LocalDate.now().minusYears(19)
    val tomorrow = LocalDate.now().plusDays(1)

    @Test
    fun testGetPassengerCategoryAdult() {
        testTraveler.birthDate = adultBirthDate

        assertEquals(PassengerCategory.ADULT, testTraveler.getPassengerCategory(tomorrow, false))
    }

    @Test
    fun testGetPassengerCategoryPreDefined() {
        testTraveler.birthDate = adultBirthDate
        testTraveler.passengerCategory = PassengerCategory.CHILD

        assertEquals(PassengerCategory.CHILD, testTraveler.getPassengerCategory(tomorrow, false),
                "Error: If passenger category has been explicitly set the getter should not change it based on birthdate." +
                        "This logic is used to verify birthdate matches preset categories.")
    }

    @Test
    fun testGetPassengerCategoryInfantInLap() {
        testTraveler.birthDate = infantBirthDate
        assertEquals(PassengerCategory.INFANT_IN_LAP, testTraveler.getPassengerCategory(tomorrow, true))
    }

    @Test
    fun testGetPassengerCategoryInfantInSeat() {
        testTraveler.birthDate = infantBirthDate
        assertEquals(PassengerCategory.INFANT_IN_SEAT, testTraveler.getPassengerCategory(tomorrow, false))
    }

    @Test
    fun testGetSeatPreferenceDefault() {
        testTraveler.seatPreference = Traveler.SeatPreference.ANY
        assertEquals(Traveler.SeatPreference.WINDOW, testTraveler.seatPreference)
    }
}
