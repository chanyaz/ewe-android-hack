package com.expedia.bookings.utils

import com.expedia.bookings.data.User
import com.expedia.bookings.data.UserLoyaltyMembershipInformation
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.test.PointOfSaleTestConfiguration
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.util.LoyaltyUtil
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
class LoyaltyUtilTest {
    val context = RuntimeEnvironment.application

    @Test
    fun testSWPDisabledNotSignedIn() {
        PointOfSaleTestConfiguration.configurePointOfSale(context, "MockSharedData/pos_swp_enabled_config.json")
        Assert.assertTrue(PointOfSale.getPointOfSale().isSWPEnabledForHotels)

        Assert.assertFalse(LoyaltyUtil.isShopWithPointsAvailable(context))
    }

    @Test
    fun testSWPDisabledUserNotAllowed() {
        signInUser(false)
        PointOfSaleTestConfiguration.configurePointOfSale(context, "MockSharedData/pos_swp_enabled_config.json")
        Assert.assertTrue(PointOfSale.getPointOfSale().isSWPEnabledForHotels)

        Assert.assertFalse(LoyaltyUtil.isShopWithPointsAvailable(context))
    }

    @Test
    fun testSWPDisabledPOS() {
        signInUser(true)
        PointOfSaleTestConfiguration.configurePointOfSale(context, "MockSharedData/pos_swp_disabled_config.json")
        Assert.assertFalse(PointOfSale.getPointOfSale().isSWPEnabledForHotels)

        Assert.assertFalse(LoyaltyUtil.isShopWithPointsAvailable(context))
    }

    @Test
    fun testSWPEnabledPOS() {
        signInUser(true)
        PointOfSaleTestConfiguration.configurePointOfSale(context, "MockSharedData/pos_swp_enabled_config.json")
        Assert.assertTrue(PointOfSale.getPointOfSale().isSWPEnabledForHotels)

        Assert.assertTrue(LoyaltyUtil.isShopWithPointsAvailable(context))
    }

    private fun signInUser(allowShopWithPoints: Boolean) {
        val loyaltyInfo = UserLoyaltyMembershipInformation()
        loyaltyInfo.isAllowedToShopWithPoints = allowShopWithPoints
        val user = UserLoginTestUtil.mockUser()
        user.setLoyaltyMembershipInformation(loyaltyInfo)
        UserLoginTestUtil.setupUserAndMockLogin(user)
        Assert.assertTrue(User.isLoggedIn(context))
    }
}