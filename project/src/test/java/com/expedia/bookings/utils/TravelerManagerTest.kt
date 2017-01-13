package com.expedia.bookings.utils

import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.User
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.enums.PassengerCategory
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.vm.test.traveler.MockTravelerProvider
import com.mobiata.android.util.SettingUtils
import org.joda.time.LocalDate
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
class TravelerManagerTest {
    val travelerManager = TravelerManager()
    val mockTravelerProvider = MockTravelerProvider()


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

    @Test
    fun testOnSignInWhenUserLoggedIn() {
        val testUser = User();
        val testSavedPrimaryTraveler = Traveler();
        val travelerWithCategory = Traveler();

        testSavedPrimaryTraveler.passengerCategory = null
        travelerWithCategory.passengerCategory = PassengerCategory.ADULT

        testUser.primaryTraveler = testSavedPrimaryTraveler

        testUser.primaryTraveler.firstName = mockTravelerProvider.testFirstName

        UserLoginTestUtil.setupUserAndMockLogin(testUser)

        mockTravelerProvider.updateDBWithMockTravelers(1, travelerWithCategory)
        travelerManager.onSignIn(RuntimeEnvironment.application)
        assertEquals(PassengerCategory.ADULT, Db.getTravelers()[0].passengerCategory,
                "Expected Primary Traveler to inherit the PassengerCategory from Db")
        assertEquals(mockTravelerProvider.testFirstName, Db.getTravelers()[0].firstName,
                "Expected Db to inherit primary traveler attributes minus PC")
    }

    @Test
    fun testOnSignInWhenUserNotLoggedIn() {
        val traveler = Traveler();

        traveler.passengerCategory = PassengerCategory.ADULT
        traveler.firstName = mockTravelerProvider.testFirstName

        mockTravelerProvider.updateDBWithMockTravelers(1, traveler)
        travelerManager.onSignIn(RuntimeEnvironment.application)
        assertEquals(PassengerCategory.ADULT, Db.getTravelers()[0].passengerCategory,
                "Not Signed In, nothing about traveler should change")
        assertEquals(mockTravelerProvider.testFirstName, Db.getTravelers()[0].firstName,
                "Not Signed In, nothing about traveler should change")
    }

    @Test
    fun testUpdateTravelersWhenUserNotLoggedIn() {
        SettingUtils.save(RuntimeEnvironment.application  , R.string.preference_enable_rail_checkout_login, true)
        travelerManager.updateRailTravelers(RuntimeEnvironment.application)
        assertTrue(Db.getTravelers().size == 1)
        assertNull(Db.getTravelers()[0].lastName)
    }

    @Test
    fun testUpdateTravelersWhenUserLoggedIn() {
        SettingUtils.save(RuntimeEnvironment.application  , R.string.preference_enable_rail_checkout_login, true)

        val testUser = User();
        testUser.primaryTraveler = Traveler()
        testUser.primaryTraveler.firstName = mockTravelerProvider.testFirstName

        UserLoginTestUtil.setupUserAndMockLogin(testUser)

        travelerManager.updateRailTravelers(RuntimeEnvironment.application)
        assertTrue(Db.getTravelers().size == 1)
        assertEquals(mockTravelerProvider.testFirstName, Db.getTravelers()[0].firstName)
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