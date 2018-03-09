package com.expedia.bookings.itin.helpers

import com.expedia.bookings.itin.utils.IActivityLauncher

class MockActivityLauncher : IActivityLauncher {
    var launched = false
    override fun launchActivity() {
        launched = true
    }
}
