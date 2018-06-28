package com.expedia.bookings.itin.scopes

import android.arch.lifecycle.LifecycleOwner
import com.expedia.bookings.itin.common.ItinRepoInterface
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinHotel
import com.expedia.bookings.itin.utils.AbacusSource
import com.expedia.bookings.itin.utils.ActivityLauncher
import com.expedia.bookings.itin.utils.FeatureSource
import com.expedia.bookings.itin.utils.IActivityLauncher
import com.expedia.bookings.itin.utils.IWebViewLauncher
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.tracking.ITripsTracking

data class HotelItinDetailsScope(
        override val itin: Itin,
        override val hotel: ItinHotel,
        override val strings: StringSource,
        override val webViewLauncher: IWebViewLauncher,
        override val tripsTracking: ITripsTracking,
        override val activityLauncher: ActivityLauncher,
        override val abacus: AbacusSource,
        override val features: FeatureSource
) : HasItin, HasHotel, HasStringProvider, HasWebViewLauncher, HasTripsTracking, HasActivityLauncher, HasAbacusProvider, HasFeatureProvider

data class HotelItinToolbarScope(
        override val strings: StringSource,
        override val itinRepo: ItinRepoInterface,
        override val lifecycleOwner: LifecycleOwner
) : HasStringProvider, HasItinRepo, HasLifecycleOwner

data class HotelItinPricingSummaryScope(
        override val itinRepo: ItinRepoInterface,
        override val strings: StringSource,
        override val activityLauncher: IActivityLauncher,
        override val lifecycleOwner: LifecycleOwner
) : HasLifecycleOwner, HasStringProvider, HasItinRepo, HasActivityLauncher

data class HotelItinPricingBundleScope(
        override val itinRepo: ItinRepoInterface,
        override val strings: StringSource,
        override val lifecycleOwner: LifecycleOwner
) : HasStringProvider, HasItinRepo, HasLifecycleOwner

data class HotelItinViewReceiptScope(
        override val strings: StringSource,
        override val itinRepo: ItinRepoInterface,
        override val lifecycleOwner: LifecycleOwner,
        override val tripsTracking: ITripsTracking,
        override val webViewLauncher: IWebViewLauncher,
        override val features: FeatureSource
) : HasStringProvider, HasItinRepo, HasLifecycleOwner, HasTripsTracking, HasWebViewLauncher, HasFeatureProvider

data class HotelItinTaxiViewModelScope(
        override val itinRepo: ItinRepoInterface,
        override val lifecycleOwner: LifecycleOwner
) : HasItinRepo, HasLifecycleOwner

data class HotelItinPricingAdditionalInfoScope(
        override val itinRepo: ItinRepoInterface,
        override val strings: StringSource,
        override val lifecycleOwner: LifecycleOwner
) : HasItinRepo, HasStringProvider, HasLifecycleOwner

data class HotelItinRewardsScope(
        override val strings: StringSource,
        override val itinRepo: ItinRepoInterface,
        override val lifecycleOwner: LifecycleOwner,
        override val tripsTracking: ITripsTracking,
        override val webViewLauncher: IWebViewLauncher,
        override val e3Endpoint: String
) : HasStringProvider, HasItinRepo, HasLifecycleOwner, HasTripsTracking, HasWebViewLauncher, HasE3Endpoint
