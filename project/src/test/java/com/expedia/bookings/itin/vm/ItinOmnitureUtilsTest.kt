package com.expedia.bookings.itin.vm

import android.app.Activity
import android.content.Context
import com.expedia.bookings.itin.flight.common.ItinOmnitureUtils
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.tripstore.extensions.firstCar
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.itin.support.ItinCardDataFlightBuilder
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class ItinOmnitureUtilsTest {
    private lateinit var activity: Activity
    private lateinit var sut: ItinOmnitureUtils
    private lateinit var context: Context

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        sut = ItinOmnitureUtils
        context = RuntimeEnvironment.application
    }

    @Test
    fun getTripDuration() {
        var testItinCardData = ItinCardDataFlightBuilder().build()
        val duration = sut.calculateTripDuration(testItinCardData)
        assertEquals("8", duration )
    }

    @Test
    fun getDaysUntilTrip() {
        val testItinCardData = ItinCardDataFlightBuilder().build()
        val daysUntil = sut.calculateDaysUntilTripStart(testItinCardData)
        assertEquals("30", daysUntil)
    }

    @Test
    fun buildOrderNumberAndItinNumberString() {
        val testItinCardData = ItinCardDataFlightBuilder().build()
        val orderNumberAndItinNumber = sut.buildOrderNumberAndItinNumberString(testItinCardData)
        assertEquals("8063550177859|7238007847306", orderNumberAndItinNumber)
    }

    @Test
    fun buildProductString() {
        val testItinCardData = ItinCardDataFlightBuilder().build(false, true)
        val productString = sut.buildFlightProductString(testItinCardData)
        assertEquals(";Flight:UA:RT;;", productString)
    }

    @Test
    fun testCarProductInfo() {
        val carData = ItinMocker.carDetailsHappy.firstCar()
        val productString = sut.carProductInfo(carData)
        assertEquals("ZT:EC", productString)
    }

    @Test
    fun testGetCarCategoryCharacter() {
        val carData = ItinMocker.carDetailsHappy.firstCar()
        val carCategory = sut.getCarCategoryCharacter(carData?.carCategory)
        assertEquals("E", carCategory)
    }

    @Test
    fun testGetCarTypeCharacter() {
        val carData = ItinMocker.carDetailsHappy.firstCar()
        val carType = sut.getCarTypeCharacter(carData?.carType)
        assertEquals("C", carType)
    }

    @Test
    fun testCarPaymentModel() {
        val carData = ItinMocker.carDetailsHappy.firstCar()
        val paymentModel = sut.carPaymentModel(carData?.paymentModel)
        assertEquals("Agency", paymentModel)
    }

    @Test
    fun testBuildLOBProductStringCar() {
        val carData = ItinMocker.carDetailsHappy
        val productString = sut.buildLOBProductString(carData, ItinOmnitureUtils.LOB.CAR)
        assertEquals(";CAR:ZT:EC;1;327.55;;eVar30=Agency:CAR:SYD-MEL:20560415-20560415", productString)
    }

    @Test
    fun testBasicCarProductString() {
        val carData = ItinMocker.carDetailsBadPickupAndTimes
        val productString = sut.buildLOBProductString(carData, ItinOmnitureUtils.LOB.CAR)
        assertEquals(";CAR:ZT:EC;1;327.55;;eVar30=:CAR:-:20560415-20560415", productString)
    }
}
