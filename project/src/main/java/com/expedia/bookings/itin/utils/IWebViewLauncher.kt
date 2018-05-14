package com.expedia.bookings.itin.utils

interface IWebViewLauncher {
    fun launchWebViewActivity(title: Int, url: String, anchor: String?, tripId: String, scrapeTitle: Boolean = false, isGuest: Boolean = false)
    fun launchWebViewSharableActivity(title: String, url: String, anchor: String?, tripId: String?, isGuest: Boolean = false)
}
