package com.expedia.bookings.utils

import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.user.UserLoyaltyMembershipInformation
import com.expedia.bookings.data.user.UserStateManager
import com.expedia.bookings.test.PointOfSaleTestConfiguration
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.model.UserLoginStateChangedModel
import com.expedia.util.LoyaltyUtil
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
class LoyaltyUtilTest {
    val context = RuntimeEnvironment.application
    private lateinit var userStateManager: UserStateManager

    @Test
    fun testSWPDisabledNotSignedIn() {
        givenUserIsNotSignedIn()
        PointOfSaleTestConfiguration.configurePointOfSale(context, "MockSharedData/pos_swp_enabled_config.json")
        Assert.assertTrue(PointOfSale.getPointOfSale().isSWPEnabledForHotels)

        Assert.assertFalse(LoyaltyUtil.isShopWithPointsAvailable(userStateManager))
    }

    @Test
    fun testSWPDisabledUserNotAllowed() {
        givenUserIsSignedInButNotAllowedToSWP()
        PointOfSaleTestConfiguration.configurePointOfSale(context, "MockSharedData/pos_swp_enabled_config.json")
        Assert.assertTrue(PointOfSale.getPointOfSale().isSWPEnabledForHotels)

        Assert.assertFalse(LoyaltyUtil.isShopWithPointsAvailable(userStateManager))
    }

    @Test
    fun testSWPDisabledPOS() {
        givenUserIsSignedInAndAllowedToSWP()
        PointOfSaleTestConfiguration.configurePointOfSale(context, "MockSharedData/pos_swp_disabled_config.json")
        Assert.assertFalse(PointOfSale.getPointOfSale().isSWPEnabledForHotels)

        Assert.assertFalse(LoyaltyUtil.isShopWithPointsAvailable(userStateManager))
    }

    @Test
    fun testSWPEnabledPOS() {
        givenUserIsSignedInAndAllowedToSWP()
        PointOfSaleTestConfiguration.configurePointOfSale(context, "MockSharedData/pos_swp_enabled_config.json")
        Assert.assertTrue(PointOfSale.getPointOfSale().isSWPEnabledForHotels)

        Assert.assertTrue(LoyaltyUtil.isShopWithPointsAvailable(userStateManager))
    }

    private fun givenUserIsNotSignedIn() {
        userStateManager = UserStateManager(context, UserLoginStateChangedModel())
    }

    private fun givenUserIsSignedInAndAllowedToSWP() {
        signInUser(true)
    }

    private fun givenUserIsSignedInButNotAllowedToSWP() {
        signInUser(false)
    }

    private fun signInUser(allowShopWithPoints: Boolean) {
        val loyaltyInfo = UserLoyaltyMembershipInformation()
        loyaltyInfo.isAllowedToShopWithPoints = allowShopWithPoints
        val user = UserLoginTestUtil.mockUser()
        user.setLoyaltyMembershipInformation(loyaltyInfo)
        UserLoginTestUtil.setupUserAndMockLogin(user)
        userStateManager = UserStateManager(context, UserLoginStateChangedModel())
    }
}