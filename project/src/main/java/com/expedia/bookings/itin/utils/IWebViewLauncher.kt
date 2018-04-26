package com.expedia.bookings.itin.utils

interface IWebViewLauncher {
    fun launchWebViewActivity(title: Int, url: String, anchor: String?, tripId: String)
    fun launchWebViewSharableActivity(title: String, url: String, anchor: String?, tripId: String?)
}
