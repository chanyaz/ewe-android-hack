package com.expedia.bookings.itin.utils

interface IWebViewLauncher {
    fun launchWebViewActivity(title: Int, url: String, anchor: String?, tripId: String, scrapeTitle: Boolean = false)
    fun launchWebViewSharableActivity(title: String, url: String, anchor: String?, tripId: String?)
}
