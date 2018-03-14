package com.expedia.bookings.itin.helpers

import com.expedia.bookings.itin.utils.IWebViewLauncher

class MockWebViewLauncher : IWebViewLauncher {
    var lastSeenTitle: Int? = null
    var lastSeenURL: String? = null
    var lastSeenTripId: String? = null
    override fun launchWebViewActivity(title: Int, url: String, anchor: String?, tripId: String) {
        lastSeenTitle = title
        lastSeenURL = url
        lastSeenTripId = tripId
    }
}
