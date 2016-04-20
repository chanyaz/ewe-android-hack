package com.expedia.bookings.test.widget.packages

import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.test.robolectric.RoboTestHelper
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.packages.PackageHotelListAdapter
import org.junit.Test
import org.junit.runner.RunWith
import rx.subjects.PublishSubject
import kotlin.test.assertFalse

@RunWith(RobolectricRunner::class)
class PackageHotelListAdapterTest {
    val testAdapter = PackageHotelListAdapter(PublishSubject.create<Hotel>(),
            PublishSubject.create<Unit>())

    @Test
    fun testIsBucketedForResultMap() {
        RoboTestHelper.updateABTest(AbacusUtils.EBAndroidAppHotelResultMapTest,
                AbacusUtils.DefaultVariate.BUCKETED.ordinal)
        assertFalse(testAdapter.isBucketedForResultMap(), "All Hotel A/B tests must be disabled for packages")
    }

    @Test
    fun testIsNotBucketedForResultMap() {
        RoboTestHelper.updateABTest(AbacusUtils.EBAndroidAppHotelResultMapTest,
                AbacusUtils.DefaultVariate.CONTROL.ordinal)
        assertFalse(testAdapter.isBucketedForResultMap(), "All Hotel A/B tests must be disabled for packages")
    }
}