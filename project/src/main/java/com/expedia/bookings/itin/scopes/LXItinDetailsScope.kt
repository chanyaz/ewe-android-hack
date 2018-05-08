package com.expedia.bookings.itin.scopes

import android.arch.lifecycle.LifecycleOwner
import com.expedia.bookings.itin.lx.ItinLxRepoInterface
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinLx
import com.expedia.bookings.itin.tripstore.utils.IJsonToItinUtil
import com.expedia.bookings.itin.utils.AbacusSource
import com.expedia.bookings.itin.utils.ActivityLauncher
import com.expedia.bookings.itin.utils.IActivityLauncher
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
        override val itinLxRepo: ItinLxRepoInterface,
        override val lifecycleOwner: LifecycleOwner
) : HasStringProvider, HasLxRepo, HasLifecycleOwner

data class LxLifeCycleObserverScope(override val strings: StringSource,
                                    override val webViewLauncher: IWebViewLauncher,
                                    override val activityLauncher: IActivityLauncher,
                                    override val jsonUtil: IJsonToItinUtil,
                                    override val id: String,
                                    override val manageBooking: ManageBookingWidgetViewModelSetter,
                                    override val toolbar: ToolBarViewModelSetter,
                                    override val tripsTracking: ITripsTracking,
                                    override val map: MapWidgetViewModelSetter) : HasStringProvider, HasWebViewLauncher, HasActivityLauncher, HasJsonUtil, HasItinId, HasToolbarViewModelSetter, HasManageBookingWidgetViewModelSetter, HasTripsTracking, HasMapWidgetViewModelSetter

class PriceSummaryCardScope(override val strings: StringSource,
                            override val webViewLauncher: IWebViewLauncher,
                            override val itinLxRepo: ItinLxRepoInterface) : HasStringProvider, HasWebViewLauncher, HasLxRepo

class StringsActivityScope(override val strings: StringSource,
                           override val activityLauncher: IActivityLauncher) : HasStringProvider, HasActivityLauncher

data class LxItinManageBookingWidgetScope(override val strings: StringSource,
                                          override val webViewLauncher: IWebViewLauncher,
                                          override val activityLauncher: IActivityLauncher,
                                          override val itinLxRepo: ItinLxRepoInterface) : HasWebViewLauncher, HasActivityLauncher, HasLxRepo, HasStringProvider
data class LxItinMapWidgetViewModelScope(override val itinLxRepo: ItinLxRepoInterface,
                                         override val lifecycleOwner: LifecycleOwner) : HasLxRepo, HasLifecycleOwner
