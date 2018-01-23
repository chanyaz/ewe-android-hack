package com.expedia.bookings.interfaces.helpers

import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.interfaces.LOBWebViewConfigurator
import com.expedia.bookings.tracking.RailWebViewTracking

class RailWebViewConfiguration : LOBWebViewConfigurator {

    override fun trackAppWebViewClose() {
        RailWebViewTracking.trackAppRailWebViewClose()
    }

    override fun trackAppWebViewBack() {
        RailWebViewTracking.trackAppRailWebViewBack()
    }

    override val lineOfBusiness = LineOfBusiness.RAILS
}
