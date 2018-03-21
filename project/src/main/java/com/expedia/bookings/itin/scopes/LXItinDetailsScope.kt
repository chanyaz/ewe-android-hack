package com.expedia.bookings.itin.scopes

import android.arch.lifecycle.LifecycleOwner
import com.expedia.bookings.itin.lx.ItinLxRepo
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinLx
import com.expedia.bookings.itin.utils.AbacusSource
import com.expedia.bookings.itin.utils.ActivityLauncher
import com.expedia.bookings.itin.utils.IWebViewLauncher
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.tracking.ITripsTracking

data class LXItinDetailsScope (
        override val itin: Itin,
        override val lx: ItinLx,
        override val strings: StringSource,
        override val webViewLauncher: IWebViewLauncher,
        override val tripsTracking: ITripsTracking,
        override val activityLauncher: ActivityLauncher,
        override val abacus: AbacusSource
) : HasItin, HasLx, HasStringProvider, HasWebViewLauncher, HasTripsTracking, HasActivityLauncher, HasAbacusProvider

data class LxItinToolbarScope(
        override val strings: StringSource,
        override val itinLxRepo: ItinLxRepo,
        override val lifecycleOwner: LifecycleOwner
) : HasStringProvider, HasLxRepo, HasLifecycleOwner
