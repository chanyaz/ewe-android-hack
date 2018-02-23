package com.expedia.bookings.utils

import com.expedia.bookings.tracking.OmnitureTracking

class OmnitureDeepLinkAnalytics : DeepLinkAnalytics {

    override fun setDeepLinkTrackingParams(params: HashMap<String, String>) {
        OmnitureTracking.storeDeepLinkParams(params)
    }
}
