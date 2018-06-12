package com.expedia.bookings.itin.helpers

import com.expedia.bookings.itin.utils.IPOSInfoProvider

class MockPOSInfoProvider : IPOSInfoProvider {
    override fun getAppInfoURL(): String {
        return "app.info.url"
    }
}
