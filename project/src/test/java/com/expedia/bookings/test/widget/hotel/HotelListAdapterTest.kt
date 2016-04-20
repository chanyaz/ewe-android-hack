package com.expedia.bookings.test.widget.hotel

import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.test.robolectric.RoboTestHelper
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.hotel.HotelListAdapter
import org.junit.Test
import org.junit.runner.RunWith
import rx.subjects.PublishSubject
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelListAdapterTest {
    val testHotelAdapter = HotelListAdapter(PublishSubject.create<Hotel>(),
            PublishSubject.create<Unit>())

    @Test
    fun testIsBucketedForResultMap() {
        RoboTestHelper.updateABTest(AbacusUtils.EBAndroidAppHotelResultMapTest,
                AbacusUtils.DefaultVariate.BUCKETED.ordinal)
        assertTrue(testHotelAdapter.isBucketedForResultMap())
    }

    @Test
    fun testIsNotBucketedForResultMap() {
        RoboTestHelper.updateABTest(AbacusUtils.EBAndroidAppHotelResultMapTest,
                AbacusUtils.DefaultVariate.CONTROL.ordinal)
        assertFalse(testHotelAdapter.isBucketedForResultMap())
    }
}