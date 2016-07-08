package com.expedia.bookings.utils

import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.enums.PassengerCategory
import org.joda.time.LocalDate
import org.junit.Assert
import org.junit.Test
import kotlin.test.assertEquals

class TravelerManagerTest {
    val travelerManager = TravelerManager()

    @Test
    fun testGetChildPassengerCategoryInfant() {
        val infantInSeat = travelerManager.getChildPassengerCategory(1, getPackageParams())
        assertEquals(PassengerCategory.INFANT_IN_SEAT, infantInSeat)
    }

    @Test
    fun testGetChildPassengerCategoryChild() {
        val child = travelerManager.getChildPassengerCategory(10, getPackageParams())
        assertEquals(PassengerCategory.CHILD, child)
    }

    @Test
    fun testGetChildPassengerCategoryAdultChild() {
        val adultChild = travelerManager.getChildPassengerCategory(17, getPackageParams())
        assertEquals(PassengerCategory.ADULT_CHILD, adultChild)
    }

    @Test
    fun testGetChildPassengerCategoryInfantInLap() {
        val params = getPackageParams()
        params.infantSeatingInLap = true
        val infantInLap = travelerManager.getChildPassengerCategory(1, params)
        assertEquals(PassengerCategory.INFANT_IN_LAP, infantInLap)
    }

    @Test
    fun testGetChildPassengerCategoryInvalid() {
        try {
            travelerManager.getChildPassengerCategory(18, getPackageParams())
            Assert.fail("This has to throw exception")
        } catch (e: IllegalArgumentException) {
            //if childAge must be less than 18
        }
    }

    private fun getPackageParams() : PackageSearchParams {
        // Can't mock PackageSearchParams because it's a 'data' class. So we have to build one.... #KotlinOP
        val packageParams = PackageSearchParams.Builder(26, 329)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .origin(SuggestionV4())
                .destination(SuggestionV4())
                .build() as PackageSearchParams
        return packageParams
    }
}