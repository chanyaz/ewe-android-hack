package com.expedia.bookings.features

interface Feature {
    val name: String
    fun enabled(): Boolean
}

class Features {
    companion object {
        val all = Features()
    }

    val showSplashLoadingAnimationScreen: Feature by RemoteFeatureDelegate()
    val universalWebviewDeepLink: Feature by RemoteFeatureDelegate()
    val lxRedesign: Feature by RemoteFeatureDelegate()
    val carRedesign: Feature by RemoteFeatureDelegate()
    val productionAbacus: Feature by RemoteFeatureDelegate()
    val launchAllTripNotifications: Feature by RemoteFeatureDelegate()
    val universalCheckoutOnLx: Feature by RemoteFeatureDelegate()
    val activityMap: Feature by RemoteFeatureDelegate()
    val hotelGreedySearch: Feature by RemoteFeatureDelegate()
    val accountWebViewInjections: Feature by RemoteFeatureDelegate()
    val genericAttach: Feature by RemoteFeatureDelegate()
    val viewReceipt: Feature by RemoteFeatureDelegate()
    val facebookAdTracking: Feature by RemoteFeatureDelegate()
    val lxMultipleDatesSearch: Feature by RemoteFeatureDelegate()
    val tripsApiCallMade: Feature by RemoteFeatureDelegate()
    val tripsApiCallSuccess: Feature by RemoteFeatureDelegate()
    val hotelReviewsTrueRecency: Feature by RemoteFeatureDelegate()
    val tripsGlassbox: Feature by RemoteFeatureDelegate()
    val lxActivityResultsHeader: Feature by RemoteFeatureDelegate()
    val hotMipRedesign: Feature by RemoteFeatureDelegate()
    val lxEssRegionTypeCall: Feature by RemoteFeatureDelegate()
}
