package com.expedia.bookings.test

import android.app.Application
import android.support.annotation.StringRes
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.test.robolectric.HotelPresenterTestUtil
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.expedia.bookings.tracking.hotel.PageUsableData
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

    lateinit private var createTripResponse: HotelCreateTripResponse
    lateinit private var context: Application
    private lateinit var mockAnalyticsProvider: AnalyticsProvider

    @Before
    fun before() {
        context = RuntimeEnvironment.application
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testDisabledSTPStateBucketedTrackingCallFiredWhenFeatureToggleON() {

        enableFeatureFlag(true, R.string.preference_enable_disabled_stp_hotels)
        enableABTest(true, AbacusUtils.EBAndroidAppDisabledSTPStateHotels.key)

        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)

        trackPageLoadHotelCheckoutInfo()

        val expectedEvars = mapOf(34 to "15923.0.1")
        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withEvars(expectedEvars), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testDisabledSTPStateControlTrackingCallFiredWhenFeatureToggleON() {

        enableFeatureFlag(true, R.string.preference_enable_disabled_stp_hotels)
        enableABTest(false, AbacusUtils.EBAndroidAppDisabledSTPStateHotels.key)

        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)

        trackPageLoadHotelCheckoutInfo()

        val expectedEvars = mapOf(34 to "15923.0.0")
        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withEvars(expectedEvars), mockAnalyticsProvider)
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
    fun testHotelMaterialFormsBucketedTrackingCallFiredWhenFeatureToggleON() {

        enableFeatureFlag(true, R.string.preference_enable_hotel_material_forms)
        enableABTest(true, AbacusUtils.EBAndroidAppHotelMaterialForms.key)

        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)

        trackPageLoadHotelCheckoutInfo()

        val expectedEvars = mapOf(34 to "16138.0.1")
        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withEvars(expectedEvars), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testHotelMaterialFormsControlTrackingCallFiredWhenFeatureToggleON() {

        enableFeatureFlag(true, R.string.preference_enable_hotel_material_forms)
        enableABTest(false, AbacusUtils.EBAndroidAppHotelMaterialForms.key)

        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)

        trackPageLoadHotelCheckoutInfo()

        val expectedEvars = mapOf(34 to "16138.0.0")
        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withEvars(expectedEvars), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testHotelMaterialFormsBucketedTrackingCallNotFiredWhenFeatureToggleOFF() {

        enableFeatureFlag(false, R.string.preference_enable_hotel_material_forms)
        enableABTest(true, AbacusUtils.EBAndroidAppHotelMaterialForms.key)

        trackPageLoadHotelCheckoutInfo()
        OmnitureTestUtils.assertStateNotTracked(OmnitureMatchers.withEvars(mapOf(34 to "16138.0.0")), mockAnalyticsProvider)
        OmnitureTestUtils.assertStateNotTracked(OmnitureMatchers.withEvars(mapOf(34 to "16138.0.1")), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testHotelMaterialFormsControlTrackingCallNotFiredWhenFeatureToggleOFF() {

        enableFeatureFlag(false, R.string.preference_enable_hotel_material_forms)
        enableABTest(false, AbacusUtils.EBAndroidAppHotelMaterialForms.key)

        trackPageLoadHotelCheckoutInfo()
        OmnitureTestUtils.assertStateNotTracked(OmnitureMatchers.withEvars(mapOf(34 to "16138.0.0")), mockAnalyticsProvider)
        OmnitureTestUtils.assertStateNotTracked(OmnitureMatchers.withEvars(mapOf(34 to "16138.0.1")), mockAnalyticsProvider)
    }

    private fun enableABTest(enable: Boolean, ABTestKey: Int) {
        Db.getAbacusResponse().updateABTestForDebug(ABTestKey,
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
