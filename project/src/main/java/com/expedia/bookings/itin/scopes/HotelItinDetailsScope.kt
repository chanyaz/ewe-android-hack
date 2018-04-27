package com.expedia.bookings.itin.scopes

import android.arch.lifecycle.LifecycleOwner
import com.expedia.bookings.itin.hotel.repositories.ItinHotelRepo
import com.expedia.bookings.itin.hotel.repositories.ItinHotelRepoInterface
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinHotel
import com.expedia.bookings.itin.utils.AbacusSource
import com.expedia.bookings.itin.utils.ActivityLauncher
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
        override val abacus: AbacusSource
) : HasItin, HasHotel, HasStringProvider, HasWebViewLauncher, HasTripsTracking, HasActivityLauncher, HasAbacusProvider

data class HotelItinToolbarScope(
        override val strings: StringSource,
        override val itinHotelRepo: ItinHotelRepo,
        override val lifecycleOwner: LifecycleOwner
) : HasStringProvider, HasHotelRepo, HasLifecycleOwner

data class HotelItinPricingSummaryScope(
        override val itinHotelRepo: ItinHotelRepo,
        override val strings: StringSource,
        override val lifecycleOwner: LifecycleOwner
) : HasLifecycleOwner, HasStringProvider, HasHotelRepo

data class HotelItinViewReceiptScope(
        override val strings: StringSource,
        override val itinHotelRepo: ItinHotelRepo,
        override val lifecycleOwner: LifecycleOwner,
        override val tripsTracking: ITripsTracking,
        override val webViewLauncher: IWebViewLauncher
) : HasStringProvider, HasHotelRepo, HasLifecycleOwner, HasTripsTracking, HasWebViewLauncher

data class HotelItinTaxiViewModelScope(
        override val itinHotelRepo: ItinHotelRepoInterface,
        override val lifecycleOwner: LifecycleOwner
) : HasHotelRepo, HasLifecycleOwner
