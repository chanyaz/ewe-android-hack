package com.expedia.bookings.test

import android.app.Application
import android.support.annotation.StringRes
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.ABTest
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.abacus.AbacusVariant
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.test.robolectric.HotelPresenterTestUtil
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.tracking.OmnitureTracking
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
    fun testDisabledSTPStateBucketedTrackingCallFired() {

        enableABTestWithRemoteFeatureFlag(true, AbacusUtils.EBAndroidAppDisabledSTPStateHotels)

        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)

        trackPageLoadHotelCheckoutInfo()

        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withAbacusTestBucketed(AbacusUtils.EBAndroidAppDisabledSTPStateHotels.key), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testDisabledSTPStateControlTrackingCallFired() {

        enableABTestWithRemoteFeatureFlag(false, AbacusUtils.EBAndroidAppDisabledSTPStateHotels)

        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)

        trackPageLoadHotelCheckoutInfo()

        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withAbacusTestControl(AbacusUtils.EBAndroidAppDisabledSTPStateHotels.key), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testHotelMaterialFormsBucketedTrackingEditTraveler() {
        enableABTestWithRemoteFeatureFlag(true, AbacusUtils.EBAndroidAppHotelMaterialForms)

        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)

        OmnitureTracking.trackHotelV2CheckoutTraveler()

        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withAbacusTestBucketed(AbacusUtils.EBAndroidAppHotelMaterialForms.key), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testHotelMaterialFormsBucketedTrackingEditPayment() {
        enableABTestWithRemoteFeatureFlag(true, AbacusUtils.EBAndroidAppHotelMaterialForms)

        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)

        OmnitureTracking.trackHotelV2PaymentEdit()

        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withAbacusTestBucketed(AbacusUtils.EBAndroidAppHotelMaterialForms.key), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testHotelMaterialFormsBucketedTrackingExpandCoupon() {
        enableABTestWithRemoteFeatureFlag(true, AbacusUtils.EBAndroidAppHotelMaterialForms)

        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)

        OmnitureTracking.trackHotelV2ExpandCoupon()

        OmnitureTestUtils.assertLinkTracked("CKO:Coupon Action", "App.CKO.Coupon.Expand",
                OmnitureMatchers.withProps(mapOf(34 to "24870.0.1")), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testHotelMaterialFormsControlTrackingEditTraveler() {
        enableABTestWithRemoteFeatureFlag(false, AbacusUtils.EBAndroidAppHotelMaterialForms)

        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)

        OmnitureTracking.trackHotelV2CheckoutTraveler()

        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withAbacusTestControl(AbacusUtils.EBAndroidAppHotelMaterialForms.key), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testHotelMaterialFormsControlTrackingEditPayment() {
        enableABTestWithRemoteFeatureFlag(false, AbacusUtils.EBAndroidAppHotelMaterialForms)

        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)

        OmnitureTracking.trackHotelV2PaymentEdit()

        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withAbacusTestControl(AbacusUtils.EBAndroidAppHotelMaterialForms.key), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testHotelMaterialFormsControlTrackingExpandCoupon() {
        enableABTestWithRemoteFeatureFlag(false, AbacusUtils.EBAndroidAppHotelMaterialForms)

        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)

        OmnitureTracking.trackHotelV2ExpandCoupon()

        OmnitureTestUtils.assertLinkTracked("CKO:Coupon Action", "App.CKO.Coupon.Expand",
                OmnitureMatchers.withProps(mapOf(34 to "24870.0.0")), mockAnalyticsProvider)
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
                if (enable) AbacusVariant.BUCKETED.value else AbacusVariant.CONTROL.value)
    }

    private fun enableABTestWithRemoteFeatureFlag(enable: Boolean, abTest: ABTest) {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, abTest,
                if (enable) AbacusVariant.BUCKETED.value else AbacusVariant.CONTROL.value)
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
