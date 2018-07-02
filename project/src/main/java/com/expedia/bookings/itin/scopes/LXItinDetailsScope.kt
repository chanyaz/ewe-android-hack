package com.expedia.bookings.itin.scopes

import android.arch.lifecycle.LifecycleOwner
import com.expedia.bookings.itin.common.ItinRepoInterface
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinLx
import com.expedia.bookings.itin.tripstore.utils.IJsonToItinUtil
import com.expedia.bookings.itin.utils.AbacusSource
import com.expedia.bookings.itin.utils.ActivityLauncher
import com.expedia.bookings.itin.utils.IActivityLauncher
import com.expedia.bookings.itin.utils.IPOSInfoProvider
import com.expedia.bookings.itin.utils.IPhoneHandler
import com.expedia.bookings.itin.utils.IToaster
import com.expedia.bookings.itin.utils.IWebViewLauncher
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.tracking.ITripsTracking

data class LXItinDetailsScope(
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
        override val itinRepo: ItinRepoInterface,
        override val lifecycleOwner: LifecycleOwner,
        override val posInfoProvider: IPOSInfoProvider
) : HasStringProvider, HasItinRepo, HasLifecycleOwner, HasPOSProvider

data class LxLifeCycleObserverScope(override val strings: StringSource,
                                    override val webViewLauncher: IWebViewLauncher,
                                    override val activityLauncher: IActivityLauncher,
                                    override val jsonUtil: IJsonToItinUtil,
                                    override val id: String,
                                    override val manageBooking: ManageBookingWidgetViewModelSetter,
                                    override val toolbar: ToolBarViewModelSetter,
                                    override val tripsTracking: ITripsTracking,
                                    override val map: MapWidgetViewModelSetter,
                                    override val redeemVoucher: RedeemVoucherViewModelSetter,
                                    override val toaster: IToaster,
                                    override val phoneHandler: IPhoneHandler,
                                    override val itinImage: ItinImageViewModelSetter,
                                    override val itinTimings: ItinTimingsViewModelSetter) : HasStringProvider, HasWebViewLauncher, HasActivityLauncher, HasJsonUtil, HasItinId, HasToolbarViewModelSetter, HasManageBookingWidgetViewModelSetter, HasTripsTracking, HasMapWidgetViewModelSetter, HasRedeemVoucherViewModelSetter, HasToaster, HasPhoneHandler, HasItinImageViewModelSetter, HasItinTimingsViewModelSetter

class WebViewCardScope(override val strings: StringSource,
                       override val webViewLauncher: IWebViewLauncher,
                       override val itinRepo: ItinRepoInterface,
                       override val type: String,
                       override val tripsTracking: ITripsTracking,
                       override val urlAnchor: String) : HasStringProvider, HasWebViewLauncher, HasItinRepo, HasItinType, HasTripsTracking, HasURLAnchor

class StringsActivityScope(override val strings: StringSource,
                           override val activityLauncher: IActivityLauncher,
                           override val itinRepo: ItinRepoInterface,
                           override val tripsTracking: ITripsTracking) : HasStringProvider, HasActivityLauncher, HasItinRepo, HasTripsTracking

data class LxItinManageBookingWidgetScope(override val strings: StringSource,
                                          override val webViewLauncher: IWebViewLauncher,
                                          override val activityLauncher: IActivityLauncher,
                                          override val itinRepo: ItinRepoInterface,
                                          override val tripsTracking: ITripsTracking) : HasWebViewLauncher, HasActivityLauncher, HasItinRepo, HasStringProvider, HasTripsTracking

data class LxItinMapWidgetViewModelScope(override val itinRepo: ItinRepoInterface,
                                         override val lifecycleOwner: LifecycleOwner,
                                         override val tripsTracking: ITripsTracking,
                                         override val toaster: IToaster,
                                         override val strings: StringSource,
                                         override val phoneHandler: IPhoneHandler,
                                         override val activityLauncher: IActivityLauncher) : HasItinRepo, HasLifecycleOwner, HasTripsTracking, HasToaster, HasStringProvider, HasPhoneHandler, HasActivityLauncher

data class LxItinRedeemVoucherViewModelScope(
        override val strings: StringSource,
        override val webViewLauncher: IWebViewLauncher,
        override val itinRepo: ItinRepoInterface,
        override val lifecycleOwner: LifecycleOwner,
        override val tripsTracking: ITripsTracking
) : HasStringProvider, HasTripsTracking, HasLifecycleOwner, HasWebViewLauncher, HasItinRepo

data class LxItinImageViewModelScope(
        override val lifecycleOwner: LifecycleOwner,
        override val itinRepo: ItinRepoInterface
) : HasItinRepo, HasLifecycleOwner

data class LxItinMoreHelpViewModelScope(override val strings: StringSource,
                                        override val itinRepo: ItinRepoInterface,
                                        override val lifecycleOwner: LifecycleOwner,
                                        override val tripsTracking: ITripsTracking) : HasItinRepo, HasStringProvider, HasLifecycleOwner, HasTripsTracking

data class ItinCustomerSupportWidgetViewModelScope(override val strings: StringSource,
                                                   override val itinRepo: ItinRepoInterface,
                                                   override val lifecycleOwner: LifecycleOwner,
                                                   override val tripsTracking: ITripsTracking,
                                                   override val webViewLauncher: IWebViewLauncher,
                                                   override val type: String
) : HasItinRepo, HasStringProvider, HasLifecycleOwner, HasTripsTracking, HasWebViewLauncher, HasItinType

data class LxItinTimingsScope(
        override val lifecycleOwner: LifecycleOwner,
        override val itinRepo: ItinRepoInterface,
        override val strings: StringSource
) : HasItinRepo, HasLifecycleOwner, HasStringProvider

data class ItinExpandedMapViewModelScope(
        override val activityLauncher: IActivityLauncher,
        override val lifecycleOwner: LifecycleOwner,
        override val itinRepo: ItinRepoInterface,
        override val type: String,
        override val tripsTracking: ITripsTracking
) : HasItinRepo, HasActivityLauncher, HasLifecycleOwner, HasItinType, HasTripsTracking
