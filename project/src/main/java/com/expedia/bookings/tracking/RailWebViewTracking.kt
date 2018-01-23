package com.expedia.bookings.tracking

/**
 * Created by dkumarpanjabi on 6/29/17.
 */
object RailWebViewTracking {

    @JvmStatic fun trackAppRailWebViewRetry() {
        OmnitureTracking.trackAppRailWebViewRetry()
    }

    @JvmStatic fun trackAppRailWebViewBack() {
        OmnitureTracking.trackAppRailWebViewBack()
    }

    @JvmStatic fun trackAppRailWebViewSignIn() {
        OmnitureTracking.trackAppRailWebViewSignIn()
    }

    @JvmStatic fun trackAppRailWebViewLogOut() {
        OmnitureTracking.trackAppRailWebViewLogOut()
    }

    @JvmStatic fun trackAppRailWebViewClose() {
        OmnitureTracking.trackAppRailWebViewClose()
    }

    @JvmStatic fun trackAppRailWebViewABTest() {
        OmnitureTracking.trackAppRailWebViewABTest()
    }
}
