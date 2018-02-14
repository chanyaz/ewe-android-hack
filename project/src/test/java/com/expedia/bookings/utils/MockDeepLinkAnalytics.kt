package com.expedia.bookings.utils

class MockDeepLinkAnalytics : DeepLinkAnalytics {

    val deepLinkArgs = HashMap<String, String>()

    override fun setDeepLinkTrackingParams(params: HashMap<String, String>) {
        params.forEach { (key, value) ->
            deepLinkArgs.put(key, value)
        }
    }
}
