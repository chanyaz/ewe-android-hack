package com.expedia.bookings.tracking

import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.OmnitureTestUtils.Companion.assertStateTracked
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.OmnitureMatchers.Companion.withAbacusTestBucketed
import com.expedia.bookings.test.OmnitureMatchers.Companion.withAbacusTestControl
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(RobolectricRunner::class)
@RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
class RailTrackingTest {

    private lateinit var mockAnalyticsProvider: AnalyticsProvider
    private lateinit var sut: RailTracking

    @Before
    fun setup() {
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        sut = RailTracking()
    }

    @Test
    fun apimTestTrackedAsControlOnSearchInitWhenNotBucketed() {
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppAPIMAuth)

        sut.trackRailSearchInit()

        assertStateTracked("App.Rail.Dest-Search", withAbacusTestControl(AbacusUtils.EBAndroidAppAPIMAuth), mockAnalyticsProvider)
    }

    @Test
    fun apimTestTrackedAsVariantOnSearchInitWhenBucketed() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppAPIMAuth)

        sut.trackRailSearchInit()

        assertStateTracked("App.Rail.Dest-Search", withAbacusTestBucketed(AbacusUtils.EBAndroidAppAPIMAuth), mockAnalyticsProvider)
    }
}
