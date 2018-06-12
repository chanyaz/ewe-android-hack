package com.expedia.bookings.itin.utils

import com.expedia.bookings.data.pos.PointOfSale

class POSInfoProvider : IPOSInfoProvider {
    override fun getAppInfoURL(): String {
        return PointOfSale.getPointOfSale().appInfoUrl
    }
}

interface IPOSInfoProvider {
    fun getAppInfoURL(): String
}
