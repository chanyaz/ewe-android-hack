package com.expedia.bookings.itin.helpers

import com.expedia.bookings.itin.utils.IWebViewLauncher

class MockWebViewLauncher : IWebViewLauncher {

    var lastSeenTitle: Int? = null
    var lastSeenURL: String? = null
    var lastSeenTripId: String? = null
    var toolbarTitle: String? = null
    var sharableWebviewCalled = false
    var shouldScrapTitle = false
    var isGuest = false

    override fun launchWebViewActivity(title: Int, url: String, anchor: String?, tripId: String, scrapeTitle: Boolean, isGuest: Boolean) {
        lastSeenTitle = title
        lastSeenURL = url
        lastSeenTripId = tripId
        shouldScrapTitle = scrapeTitle
        this.isGuest = isGuest
    }

    override fun launchWebViewSharableActivity(title: String, url: String, anchor: String?, tripId: String?, isGuest: Boolean) {
        toolbarTitle = title
        lastSeenURL = url
        lastSeenTripId = tripId
        sharableWebviewCalled = true
        this.isGuest = isGuest
    }
}
