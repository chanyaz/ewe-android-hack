package com.expedia.bookings.interfaces.helpers

import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.interfaces.LOBWebViewConfigurator
import com.expedia.bookings.tracking.PackageWebViewTracking

class PackageWebViewConfiguration : LOBWebViewConfigurator {

    override fun trackAppWebViewClose() {
        PackageWebViewTracking.trackAppPackageWebViewClose()
    }

    override fun trackAppWebViewBack() {
        PackageWebViewTracking.trackAppPackageWebViewBack()
    }

    override val lineOfBusiness = LineOfBusiness.PACKAGES
}
