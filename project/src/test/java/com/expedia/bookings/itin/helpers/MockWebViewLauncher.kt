package com.expedia.bookings.itin.helpers

import com.expedia.bookings.itin.utils.IWebViewLauncher

class MockWebViewLauncher : IWebViewLauncher {

    var lastSeenTitle: Int? = null
    var lastSeenURL: String? = null
    var lastSeenTripId: String? = null
    var toolbarTitle: String? = null
    var sharableWebviewCalled = false

    override fun launchWebViewActivity(title: Int, url: String, anchor: String?, tripId: String) {
        lastSeenTitle = title
        lastSeenURL = url
        lastSeenTripId = tripId
    }
    override fun launchWebViewSharableActivity(title: String, url: String, anchor: String?, tripId: String?) {
        toolbarTitle = title
        lastSeenURL = url
        lastSeenTripId = tripId
        sharableWebviewCalled = true
    }
}
