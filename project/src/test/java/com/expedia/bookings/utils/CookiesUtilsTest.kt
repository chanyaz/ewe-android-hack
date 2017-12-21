package com.expedia.bookings.utils

import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.PointOfSaleTestConfiguration
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.mobiata.android.util.SettingUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
@RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
class CookiesUtilsTest {

    val context = RuntimeEnvironment.application

    @Test
    fun testNewCookiesMechanismTrueWhenShouldUseWebSyncCookieStoreFalseAndFeatureFlagOFF() {
        SettingUtils.save(context, CookiesUtils.FEATURE_TOGGLE_OLD_COOKIES_MECAHNISM, false)

        PointOfSaleTestConfiguration.configurePointOfSale(context, "MockSharedData/pos_test_config.json", false)
        val pos = PointOfSale.getPointOfSale()
        assertFalse(pos.shouldUseWebViewSyncCookieStore())

        assertTrue(CookiesUtils.shouldUseNewCookiesMechanism(context))
    }

    @Test
    fun testNewCookiesMechanismTrueWhenShouldUseWebSyncCookieStoreTrueAndFeatureFlagON() {
        SettingUtils.save(context, CookiesUtils.FEATURE_TOGGLE_OLD_COOKIES_MECAHNISM, true)

        PointOfSaleTestConfiguration
                .configurePOS(context, "ExpediaSharedData/ExpediaPointOfSaleConfig.json", Integer.toString(PointOfSaleId.INDIA.id), false)
        val pos = PointOfSale.getPointOfSale()
        assertTrue(pos.shouldUseWebViewSyncCookieStore())

        assertTrue(CookiesUtils.shouldUseNewCookiesMechanism(context))
    }

    @Test
    fun testNewCookiesMechanismFalseWhenShouldUseWebSyncCookieStoreFalseAndFeatureFlagON() {
        SettingUtils.save(context, CookiesUtils.FEATURE_TOGGLE_OLD_COOKIES_MECAHNISM, true)

        PointOfSaleTestConfiguration.configurePointOfSale(context, "MockSharedData/pos_test_config.json", false)
        val pos = PointOfSale.getPointOfSale()
        assertFalse(pos.shouldUseWebViewSyncCookieStore())

        assertFalse(CookiesUtils.shouldUseNewCookiesMechanism(context))
    }

    @Test
    fun testNewCookiesMechanismTrueWhenShouldUseWebSyncCookieStoreTrueAndFeatureFlagOFF() {
        SettingUtils.save(context, CookiesUtils.FEATURE_TOGGLE_OLD_COOKIES_MECAHNISM, false)

        PointOfSaleTestConfiguration
                .configurePOS(context, "ExpediaSharedData/ExpediaPointOfSaleConfig.json", Integer.toString(PointOfSaleId.INDIA.id), false)
        val pos = PointOfSale.getPointOfSale()
        assertTrue(pos.shouldUseWebViewSyncCookieStore())

        assertTrue(CookiesUtils.shouldUseNewCookiesMechanism(context))
    }
}