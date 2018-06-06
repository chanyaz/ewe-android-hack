package com.expedia.bookings.itin.scopes

import android.arch.lifecycle.LifecycleOwner
import com.expedia.bookings.features.Feature
import com.expedia.bookings.itin.cars.ItinCarRepoInterface
import com.expedia.bookings.itin.common.ItinRepoInterface
import com.expedia.bookings.itin.hotel.repositories.ItinHotelRepoInterface
import com.expedia.bookings.itin.lx.ItinLxRepoInterface
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinHotel
import com.expedia.bookings.itin.tripstore.data.ItinLx
import com.expedia.bookings.itin.tripstore.utils.IJsonToItinUtil
import com.expedia.bookings.itin.utils.AbacusSource
import com.expedia.bookings.itin.utils.IActivityLauncher
import com.expedia.bookings.itin.utils.IPhoneHandler
import com.expedia.bookings.itin.utils.IToaster
import com.expedia.bookings.itin.utils.IWebViewLauncher
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.tracking.ITripsTracking
import com.expedia.bookings.utils.StrUtils

interface HasItin {
    val itin: Itin
}

interface HasHotel {
    val hotel: ItinHotel
}

interface HasStringProvider {
    val strings: StringSource
}

interface HasWebViewLauncher {
    val webViewLauncher: IWebViewLauncher
}

interface HasTripsTracking {
    val tripsTracking: ITripsTracking
}

interface HasFeature {
    val feature: Feature
}

interface HasE3Endpoint {
    val e3Endpoint: String
}

interface HasHotelRepo {
    val itinHotelRepo: ItinHotelRepoInterface
}

interface HasLifecycleOwner {
    val lifecycleOwner: LifecycleOwner
}

interface HasItinType {
    val type: String
}

interface HasActivityLauncher {
    val activityLauncher: IActivityLauncher
}

interface HasAbacusProvider {
    val abacus: AbacusSource
}

interface HasStringUtil {
    val stringUtils: StrUtils
}

interface HasLxRepo {
    val itinLxRepo: ItinLxRepoInterface
}

interface HasCarRepo {
    val itinCarRepo: ItinCarRepoInterface
}

interface HasLx {
    val lx: ItinLx
}

interface HasJsonUtil {
    val jsonUtil: IJsonToItinUtil
}

interface HasItinId {
    val id: String
}

interface HasItinRepo {
    val itinRepo: ItinRepoInterface
}

interface HasToaster {
    val toaster: IToaster
}

interface HasPhoneHandler {
    val phoneHandler: IPhoneHandler
}
