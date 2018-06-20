package com.expedia.bookings.data

import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.features.Features
import org.junit.Test
import org.junit.runner.RunWith

import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.FeatureTestUtils

import kotlin.test.assertEquals
import org.junit.Before
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricRunner::class)
class HotelRateTest {

    lateinit var sut: HotelRate
    private val context = RuntimeEnvironment.application

    @Before
    fun setup() {
        sut = HotelRate()
        sut.strikeThroughPrice = 100f
        sut.strikethroughPriceToShowUsers = 200f
    }

    @Test
    fun getStrikeThroughPriceTestFeatureEnabled() {
        FeatureTestUtils.enableFeature(context, Features.all.strikethroughPricingExcludesBurnAmount)
        assertEquals(100f, sut.strikeThroughPrice)
    }

    @Test
    fun getStrikeThroughPriceTestFeatureDisabled() {
        FeatureTestUtils.disableFeature(context, Features.all.strikethroughPricingExcludesBurnAmount)
        assertEquals(200f, sut.strikeThroughPrice)
    }
}
