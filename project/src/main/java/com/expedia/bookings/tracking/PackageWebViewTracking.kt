package com.expedia.bookings.tracking

object PackageWebViewTracking {

    @JvmStatic fun trackAppPackageWebViewRetry() {
        OmnitureTracking.trackAppPackageWebViewRetry()
    }

    @JvmStatic fun trackAppPackageWebViewBack() {
        OmnitureTracking.trackAppPackageWebViewBack()
    }

    @JvmStatic fun trackAppPackageWebViewSignIn() {
        OmnitureTracking.trackAppPackageWebViewSignIn()
    }

    @JvmStatic fun trackAppPackageWebViewLogOut() {
        OmnitureTracking.trackAppPackageWebViewLogOut()
    }

    @JvmStatic fun trackAppPackageWebViewClose() {
        OmnitureTracking.trackAppPackageWebViewClose()
    }
}
