package com.expedia.bookings.packages.adapter

import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import io.reactivex.subjects.PublishSubject
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
class PackageHotelListAdapterTest {
    val context = RuntimeEnvironment.application
    val adapter = PackageHotelListAdapter(PublishSubject.create(), PublishSubject.create(), PublishSubject.create())

    @Test
    fun testHeaderVisibilityForDetailedPriceDisplay() {
        AbacusTestUtils.bucketTestsAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppPackagesHSRPriceDisplay)
        assertNull(adapter.getPriceDescriptorMessageIdForHSR(context))

        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppPackagesHSRPriceDisplay)
        AbacusTestUtils.bucketTestsAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppPackagesMoveBundleOverviewForBreadcrumbs)
        assertNotNull(adapter.getPriceDescriptorMessageIdForHSR(context))
    }
}
