package com.expedia.bookings.packages.adapter

import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.test.PointOfSaleTestConfiguration
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import io.reactivex.subjects.PublishSubject
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
class PackageHotelListAdapterTest {
    val context = RuntimeEnvironment.application
    val adapter = PackageHotelListAdapter(PublishSubject.create(), PublishSubject.create(), PublishSubject.create())

    @Test
    fun testHeaderVisibilityForDetailedPriceDisplay() {
        AbacusTestUtils.bucketTestsAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppPackagesHSRPriceDisplay)
        assertNull(adapter.getPriceDescriptorMessageIdForHSR(context))
    }

    @Test
    fun testPriceDescriptorMessage() {
        val initialPOSID = PointOfSale.getPointOfSale().pointOfSaleId
        assertEquals(R.string.package_hotel_results_header_TEMPLATE, adapter.getPriceDescriptorMessageIdForHSR(context))

        setPointOfSale(PointOfSaleId.JAPAN)
        assertEquals(R.string.package_hotel_results_includes_header_TEMPLATE, adapter.getPriceDescriptorMessageIdForHSR(context))
        setPointOfSale(initialPOSID)
    }

    private fun setPointOfSale(posId: PointOfSaleId) {
        PointOfSaleTestConfiguration.configurePOS(context, "ExpediaSharedData/ExpediaPointOfSaleConfig.json", Integer.toString(posId.id), false)
    }
}
