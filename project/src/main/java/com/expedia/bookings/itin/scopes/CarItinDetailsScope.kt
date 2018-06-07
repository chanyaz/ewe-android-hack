package com.expedia.bookings.itin.scopes

import android.arch.lifecycle.LifecycleOwner
import com.expedia.bookings.itin.cars.ItinCarRepoInterface
import com.expedia.bookings.itin.utils.IActivityLauncher
import com.expedia.bookings.itin.utils.IPhoneHandler
import com.expedia.bookings.itin.utils.IToaster
import com.expedia.bookings.itin.utils.IWebViewLauncher
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.tracking.ITripsTracking

data class CarsMasterScope(
        override val strings: StringSource,
        override val webViewLauncher: IWebViewLauncher,
        override val lifecycleOwner: LifecycleOwner,
        override val activityLauncher: IActivityLauncher,
        override val itinCarRepo: ItinCarRepoInterface,
        override val toaster: IToaster,
        override val phoneHandler: IPhoneHandler,
        override val tripsTracking: ITripsTracking) : HasCarRepo, HasLifecycleOwner, HasStringProvider, HasActivityLauncher, HasWebViewLauncher, HasToaster, HasPhoneHandler, HasTripsTracking

data class CarItinMoreHelpMasterScope(
        override val strings: StringSource,
        override val lifecycleOwner: LifecycleOwner,
        override val itinCarRepo: ItinCarRepoInterface,
        override val tripsTracking: ITripsTracking
) : HasStringProvider, HasCarRepo, HasTripsTracking, HasLifecycleOwner
