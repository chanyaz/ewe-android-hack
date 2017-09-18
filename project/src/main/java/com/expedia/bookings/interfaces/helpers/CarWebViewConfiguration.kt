package com.expedia.bookings.interfaces.helpers

import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.interfaces.LOBWebViewConfigurator
import com.expedia.bookings.tracking.CarWebViewTracking

class CarWebViewConfiguration : LOBWebViewConfigurator {

    override fun trackAppWebViewClose() {
        CarWebViewTracking().trackAppCarWebViewClose()
    }

    override fun trackAppWebViewBack() {
        CarWebViewTracking().trackAppCarWebViewBack()
    }

    override val lineOfBusiness = LineOfBusiness.CARS
}