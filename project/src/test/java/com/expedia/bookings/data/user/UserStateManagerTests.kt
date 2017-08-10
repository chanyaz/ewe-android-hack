package com.expedia.bookings.data.user

import com.expedia.bookings.data.LoyaltyMembershipTier
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.UserAccountRefresher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
class UserStateManagerTests {

    @Test
    fun notSignedInUserHasNoLoyaltyTier() {
        val userStateManager = UserStateManager(RuntimeEnvironment.application)
        val expectedTier = LoyaltyMembershipTier.NONE

        assertFalse(userStateManager.isUserAuthenticated())
        assertEquals(expectedTier, userStateManager.getCurrentUserLoyaltyTier())
    }

    @Test
    fun userWithNoLoyaltyInfoHasNoLoyaltyTier() {
        val userStateManager = UserStateManager(RuntimeEnvironment.application)
        val expectedTier = LoyaltyMembershipTier.NONE

        givenSignedInAsUser(getNonRewardsMember())

        assertTrue(userStateManager.isUserAuthenticated())
        assertEquals(expectedTier, userStateManager.getCurrentUserLoyaltyTier())
    }

    @Test
    fun userWithLoyaltyInfoHasCorrectLoyaltyTier() {
        val userStateManager = UserStateManager(RuntimeEnvironment.application)

        givenSignedInAsUser(getBaseTierRewardsMember())
        assertTrue(userStateManager.isUserAuthenticated())
        assertEquals(LoyaltyMembershipTier.BASE, userStateManager.getCurrentUserLoyaltyTier())

        givenSignedInAsUser(getMiddleTierRewardsMember())
        assertTrue(userStateManager.isUserAuthenticated())
        assertEquals(LoyaltyMembershipTier.MIDDLE, userStateManager.getCurrentUserLoyaltyTier())

        givenSignedInAsUser(getTopTierRewardsMember())
        assertTrue(userStateManager.isUserAuthenticated())
        assertEquals(LoyaltyMembershipTier.TOP, userStateManager.getCurrentUserLoyaltyTier())
    }

    @Test
    fun testUserStateSanityCallsOnUserAccountRefreshedWhenUserLoggedIn() {
        val userStateManager = UserStateManager(RuntimeEnvironment.application)
        var onUserAccountRefreshCalled = false

        class TestListener: UserAccountRefresher.IUserAccountRefreshListener {
            override fun onUserAccountRefreshed() {
                onUserAccountRefreshCalled = true
            }
        }

        givenSignedInAsUser(getBaseTierRewardsMember())
        userStateManager.ensureUserStateSanity(TestListener())

        assertTrue(onUserAccountRefreshCalled)
    }

    @Test
    fun testUserDeletedAtSystemLevelSignedOutWhenLoggedInAccountChanges() {
        val userStateManager = UserStateManager(RuntimeEnvironment.application)

        givenSignedInAsDiskOnlyUser(getBaseTierRewardsMember())
        assertTrue(User.isLoggedInOnDisk(RuntimeEnvironment.application))

        userStateManager.onLoginAccountsChanged()

        assertFalse(User.isLoggedInOnDisk(RuntimeEnvironment.application))
    }

    private fun givenSignedInAsUser(user: User) {
        UserLoginTestUtil.setupUserAndMockLogin(user)
    }

    private fun givenSignedInAsDiskOnlyUser(user: User) {
        UserLoginTestUtil.setupUserAndMockDiskOnlyLogin(user)
    }

    private fun getNonRewardsMember(): User {
        val user = User()
        val traveler = Traveler()

        traveler.firstName = "No"
        traveler.middleName = "Rewards"
        traveler.lastName = "ForMe"
        traveler.email = "norewards@mobiata.com"
        user.primaryTraveler = traveler

        return user
    }

    private fun getBaseTierRewardsMember(): User {
        val user = User()
        val traveler = Traveler()
        val loyaltyInfo = UserLoyaltyMembershipInformation()

        traveler.firstName = "Base"
        traveler.middleName = "Tier"
        traveler.lastName = "Rewards"
        traveler.email = "basetier@mobiata.com"
        user.primaryTraveler = traveler

        loyaltyInfo.isLoyaltyMembershipActive = true
        loyaltyInfo.loyaltyMembershipTier = LoyaltyMembershipTier.BASE
        loyaltyInfo.loyaltyPointsAvailable = 1802.0
        user.loyaltyMembershipInformation = loyaltyInfo

        return user
    }

    private fun getMiddleTierRewardsMember(): User {
        val user = User()
        val traveler = Traveler()
        val loyaltyInfo = UserLoyaltyMembershipInformation()

        traveler.firstName = "Middle"
        traveler.middleName = "Tier"
        traveler.lastName = "Rewards"
        traveler.email = "middletier@mobiata.com"
        user.primaryTraveler = traveler

        loyaltyInfo.isLoyaltyMembershipActive = true
        loyaltyInfo.loyaltyMembershipTier = LoyaltyMembershipTier.MIDDLE
        loyaltyInfo.loyaltyPointsAvailable = 22996.0
        loyaltyInfo.loyaltyPointsPending = 965.0
        loyaltyInfo.isAllowedToShopWithPoints = true
        loyaltyInfo.loyaltyMonetaryValue = UserLoyaltyMembershipInformation.LoyaltyMonetaryValue(Money("3285.14", "USD"))
        loyaltyInfo.loyaltyMonetaryValue.setApiFormattedPrice("$3,285.14")
        user.loyaltyMembershipInformation = loyaltyInfo

        return user
    }

    private fun getTopTierRewardsMember(): User {
        val user = User()
        val traveler = Traveler()
        val loyaltyInfo = UserLoyaltyMembershipInformation()

        traveler.firstName = "Top"
        traveler.middleName = "Tier"
        traveler.lastName = "Rewards"
        traveler.email = "toptier@mobiata.com"
        user.primaryTraveler = traveler

        loyaltyInfo.isLoyaltyMembershipActive = true
        loyaltyInfo.loyaltyMembershipTier = LoyaltyMembershipTier.TOP
        loyaltyInfo.loyaltyPointsAvailable = 54206.0
        loyaltyInfo.loyaltyPointsPending = 5601.0
        loyaltyInfo.isAllowedToShopWithPoints = true
        loyaltyInfo.loyaltyMonetaryValue = UserLoyaltyMembershipInformation.LoyaltyMonetaryValue(Money("7743.41", "USD"))
        loyaltyInfo.loyaltyMonetaryValue.setApiFormattedPrice("$7,743.41")
        user.loyaltyMembershipInformation = loyaltyInfo

        return user
    }

}