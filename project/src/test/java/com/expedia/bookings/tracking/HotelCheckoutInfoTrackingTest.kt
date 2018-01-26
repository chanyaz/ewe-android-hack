package com.expedia.bookings.test

import android.app.Application
import android.support.annotation.StringRes
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.ABTest
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.test.robolectric.HotelPresenterTestUtil
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.expedia.bookings.tracking.hotel.PageUsableData
import com.expedia.bookings.utils.AbacusTestUtils
import com.mobiata.android.util.SettingUtils
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricRunner::class)
class HotelCheckoutInfoTrackingTest {

    var mockHotelServiceTestRule: MockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get

    private lateinit var createTripResponse: HotelCreateTripResponse
    private lateinit var context: Application
    private lateinit var mockAnalyticsProvider: AnalyticsProvider

    @Before
    fun before() {
        context = RuntimeEnvironment.application
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        AbacusTestUtils.resetABTests()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testDisabledSTPStateBucketedTrackingCallFiredWhenFeatureToggleON() {

        enableFeatureFlag(true, R.string.preference_enable_disabled_stp_hotels)
        enableABTest(true, AbacusUtils.EBAndroidAppDisabledSTPStateHotels.key)

        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)

        trackPageLoadHotelCheckoutInfo()

        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withAbacusTestBucketed(AbacusUtils.EBAndroidAppDisabledSTPStateHotels.key), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testDisabledSTPStateControlTrackingCallFiredWhenFeatureToggleON() {

        enableFeatureFlag(true, R.string.preference_enable_disabled_stp_hotels)
        enableABTest(false, AbacusUtils.EBAndroidAppDisabledSTPStateHotels.key)

        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)

        trackPageLoadHotelCheckoutInfo()

        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withAbacusTestControl(AbacusUtils.EBAndroidAppDisabledSTPStateHotels.key), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testDisabledSTPStateBucketedTrackingCallNotFiredWhenFeatureToggleOFF() {

        enableFeatureFlag(false, R.string.preference_enable_disabled_stp_hotels)
        enableABTest(true, AbacusUtils.EBAndroidAppDisabledSTPStateHotels.key)

        trackPageLoadHotelCheckoutInfo()
        OmnitureTestUtils.assertStateNotTracked(OmnitureMatchers.withEvars(mapOf(34 to "15923.0.0")), mockAnalyticsProvider)
        OmnitureTestUtils.assertStateNotTracked(OmnitureMatchers.withEvars(mapOf(34 to "15923.0.1")), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testDisabledSTPStateControlTrackingCallNotFiredWhenFeatureToggleOFF() {

        enableFeatureFlag(false, R.string.preference_enable_disabled_stp_hotels)
        enableABTest(false, AbacusUtils.EBAndroidAppDisabledSTPStateHotels.key)

        trackPageLoadHotelCheckoutInfo()
        OmnitureTestUtils.assertStateNotTracked(OmnitureMatchers.withEvars(mapOf(34 to "15923.0.0")), mockAnalyticsProvider)
        OmnitureTestUtils.assertStateNotTracked(OmnitureMatchers.withEvars(mapOf(34 to "15923.0.1")), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testHotelMaterialFormsBucketedTracking() {
        enableABTest(true, AbacusUtils.EBAndroidAppHotelMaterialForms.key)

        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)

        trackPageLoadHotelCheckoutInfo()

        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withAbacusTestBucketed(AbacusUtils.EBAndroidAppHotelMaterialForms.key), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testHotelMaterialFormsControlTracking() {
        enableABTest(false, AbacusUtils.EBAndroidAppHotelMaterialForms.key)

        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)

        trackPageLoadHotelCheckoutInfo()

        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withAbacusTestControl(AbacusUtils.EBAndroidAppHotelMaterialForms.key), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testSavedCouponBucketedTrackingCallFired() {
        enableABTestWithRemoteFeatureFlag(true, AbacusUtils.EBAndroidAppSavedCoupons)
        trackPageLoadHotelCheckoutInfo()

        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withAbacusTestBucketed(AbacusUtils.EBAndroidAppSavedCoupons.key), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testSavedCouponUnbucketedTrackingCallFired() {
        enableABTestWithRemoteFeatureFlag(false, AbacusUtils.EBAndroidAppSavedCoupons)
        trackPageLoadHotelCheckoutInfo()

        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withAbacusTestControl(AbacusUtils.EBAndroidAppSavedCoupons.key), mockAnalyticsProvider)
    }

    private fun enableABTest(enable: Boolean, ABTestKey: Int) {
        Db.sharedInstance.abacusResponse.updateABTestForDebug(ABTestKey,
                if (enable) AbacusUtils.DefaultVariant.BUCKETED.ordinal else AbacusUtils.DefaultVariant.CONTROL.ordinal)
    }

    private fun enableABTestWithRemoteFeatureFlag(enable: Boolean, abTest: ABTest) {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, abTest,
                if (enable) AbacusUtils.DefaultVariant.BUCKETED.ordinal else AbacusUtils.DefaultVariant.CONTROL.ordinal)
    }

    private fun trackPageLoadHotelCheckoutInfo() {
        createTripResponse = mockHotelServiceTestRule.getHappyCreateTripResponse()
        val params = HotelPresenterTestUtil.getDummyHotelSearchParams(context)
        HotelTracking.trackPageLoadHotelCheckoutInfo(createTripResponse, params, PageUsableData())
    }

    private fun enableFeatureFlag(enable: Boolean, @StringRes featureKey: Int) {
        SettingUtils.save(context, featureKey, enable)
    }
}
