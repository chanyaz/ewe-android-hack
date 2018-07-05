package com.expedia.bookings.itin.scopes

import android.arch.lifecycle.LifecycleOwner
import com.expedia.bookings.itin.common.ItinRepoInterface
import com.expedia.bookings.itin.utils.IWebViewLauncher
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.tracking.ITripsTracking

data class CruiseScope(
        override val itinRepo: ItinRepoInterface,
        override val strings: StringSource,
        override val lifecycleOwner: LifecycleOwner,
        override val tripsTracking: ITripsTracking,
        override val webViewLauncher: IWebViewLauncher
) : HasItinRepo, HasStringProvider, HasLifecycleOwner, HasTripsTracking, HasWebViewLauncher
