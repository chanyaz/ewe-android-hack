package com.expedia.bookings.utils

import org.junit.Test
import kotlin.test.assertEquals

class WebViewUtilsTest {

    @Test
    fun userAgentStringForPhone() {
        val ua = "Mozilla/512"
        val expected = "Android $ua app.webview.phone"

        assertEquals(expected, WebViewUtils.generateUserAgentStringWithDeviceType(ua, false))
    }

    @Test
    fun userAgentStringForTablet() {
        val ua = "Mozilla/512"
        val expected = "Android $ua app.webview.tablet"
        assertEquals(expected, WebViewUtils.generateUserAgentStringWithDeviceType(ua, true))
    }
}
