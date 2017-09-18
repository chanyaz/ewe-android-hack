package com.expedia.bookings.interfaces

import com.expedia.bookings.data.LineOfBusiness

interface LOBWebViewConfigurator {

    fun trackAppWebViewClose()

    fun trackAppWebViewBack()

    val lineOfBusiness: LineOfBusiness
}