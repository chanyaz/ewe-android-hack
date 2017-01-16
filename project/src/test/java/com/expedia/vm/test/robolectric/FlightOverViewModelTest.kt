package com.expedia.vm.test.robolectric

import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.test.robolectric.RoboTestHelper
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.flights.FlightOverviewViewModel
import com.mobiata.android.util.SettingUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertFalse
import kotlin.test.assertTrue


@RunWith(RobolectricRunner::class)
class FlightOverViewModelTest {

    private val context = RuntimeEnvironment.application

    lateinit private var sut: FlightOverviewViewModel
    lateinit private var flightLeg: FlightLeg

    private fun setupSystemUnderTest() {
        sut = FlightOverviewViewModel(context)
    }

    @Test
    fun testFlightDistanceVisibilty() {
        setupSystemUnderTest()

        flightLeg = FlightLeg()
        flightLeg.totalTravelDistance = "123"
        SettingUtils.save(context, R.string.preference_enable_distance_on_flight_overview, true)


        RoboTestHelper.controlTests(AbacusUtils.EBAndroidAppMaterialFlightDistanceOnDetails)
        assertFalse(sut.showFlightDistance(flightLeg))

        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppMaterialFlightDistanceOnDetails)
        assertTrue(sut.showFlightDistance(flightLeg))

        flightLeg.totalTravelDistance = ""
        assertFalse(sut.showFlightDistance(flightLeg))

        RoboTestHelper.controlTests(AbacusUtils.EBAndroidAppMaterialFlightDistanceOnDetails)
        assertFalse(sut.showFlightDistance(flightLeg))

    }
}