package com.expedia.bookings.itin.helpers

import com.expedia.bookings.itin.utils.IActivityLauncher
import com.expedia.bookings.itin.utils.Intentable

class MockActivityLauncher : IActivityLauncher {
    var launched = false
    override fun launchActivity(intentable: Intentable, id: String) {
        launched = true
    }
}
