package com.expedia.bookings.utils

import android.content.Intent
import com.expedia.bookings.data.Location
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(RobolectricRunner::class)
class GoogleMapsUtilsTest {

    @Test
    fun testGetMapsLocationIntent() {
        val location = Location()
        location.latitude = 111.0
        location.longitude = 666.0

        var testIntent = GoogleMapsUtil.getGoogleMapsIntent(location, "TestLabel")
        Assert.assertEquals(Intent.ACTION_VIEW, testIntent.action)
        Assert.assertEquals("http://maps.google.com/maps?q=loc:111.0,666.0(TestLabel)", testIntent.data.toString())

        testIntent = GoogleMapsUtil.getGoogleMapsIntent(location, "Test Label With Space")
        Assert.assertEquals(Intent.ACTION_VIEW, testIntent.action)
        Assert.assertEquals("http://maps.google.com/maps?q=loc:111.0,666.0(Test%20Label%20With%20Space)", testIntent.data.toString())
    }

    @Test
    fun testGetMapsDirectionsIntent() {

        var testIntent = GoogleMapsUtil.getDirectionsIntent("TestAddress")
        Assert.assertEquals(Intent.ACTION_VIEW, testIntent!!.action)
        Assert.assertEquals("http://maps.google.com/maps?daddr=TestAddress", testIntent.data.toString())

        testIntent = GoogleMapsUtil.getDirectionsIntent("")
        Assert.assertNull(testIntent)
    }
}
