package com.expedia.bookings.itin.helpers

import com.expedia.bookings.itin.common.ItinExpandedMapViewModel.MapUri
import com.expedia.bookings.itin.utils.AnimationDirection
import com.expedia.bookings.itin.utils.IActivityLauncher
import com.expedia.bookings.itin.utils.Intentable
import com.expedia.bookings.itin.utils.IntentableWithType

class MockActivityLauncher : IActivityLauncher {
    var intentableActivityLaunched = false
    var intentableWithTypeActivityLaunched = false
    var externalMapActivityLaunched = false

    override fun launchActivity(intentable: Intentable, id: String, animationDirection: AnimationDirection) {
        intentableActivityLaunched = true
    }

    override fun launchActivity(intentable: IntentableWithType, id: String, animationDirection: AnimationDirection, itinType: String) {
        intentableWithTypeActivityLaunched = true
    }

    override fun launchExternalMapActivity(data: MapUri) {
        externalMapActivityLaunched = true
    }
}
