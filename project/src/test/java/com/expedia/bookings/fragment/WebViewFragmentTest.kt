package com.expedia.bookings.fragment

import com.expedia.bookings.test.robolectric.RobolectricRunner
import junit.framework.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(RobolectricRunner::class)
class WebViewFragmentTest {

    @Test
    fun userAgentStringForPhone() {
        val wvf = WebViewFragment()
        val ua = "Mozilla/512"
        val expected = "Android $ua app.webview.phone"
        Assert.assertEquals(expected, wvf.generateUserAgentString(ua, false))
    }

    @Test
    fun userAgentStringForTablet() {
        val wvf = WebViewFragment()
        val ua = "Mozilla/512"
        val expected = "Android $ua app.webview.tablet"
        Assert.assertEquals(expected, wvf.generateUserAgentString(ua, true))
    }
}