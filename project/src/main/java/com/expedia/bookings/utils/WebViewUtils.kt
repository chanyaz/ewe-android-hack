package com.expedia.bookings.utils

class WebViewUtils {

    companion object {

        val tabletString = "app.webview.tablet"
        val phoneString = "app.webview.phone"

        val userAgentString = ServicesUtil.generateUserAgentString()

        @JvmStatic
        fun generateUserAgentStringWithDeviceType(userAgentString: String, isTabletDevice: Boolean): String {
            val deviceString = if (isTabletDevice) tabletString else phoneString
            return "Android $userAgentString $deviceString"
        }
    }
}
