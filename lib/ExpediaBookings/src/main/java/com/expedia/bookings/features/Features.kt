package com.expedia.bookings.features

interface Feature {
    val name: String
    fun enabled(): Boolean
}

class Features {
    companion object {
        val all = Features()
    }

    val universalWebviewDeepLink: Feature by RemoteFeatureDelegate()
    val lxRedesign: Feature by RemoteFeatureDelegate()
    val productionAbacus: Feature by RemoteFeatureDelegate()
    val launchAllTripNotifications: Feature by RemoteFeatureDelegate()
    val universalCheckoutOnLx: Feature by RemoteFeatureDelegate()
    val activityMap: Feature by RemoteFeatureDelegate()
}
