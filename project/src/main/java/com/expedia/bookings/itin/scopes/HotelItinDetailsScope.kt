package com.expedia.bookings.itin.scopes

import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinHotel
import com.expedia.bookings.itin.utils.IWebViewLauncher
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.tracking.ITripsTracking

data class HotelItinDetailsScope(
    override val itin: Itin,
    override val hotel: ItinHotel,
    override val strings: StringSource,
    override val webViewLauncher: IWebViewLauncher,
    override val tripsTracking: ITripsTracking
) : HasItin, HasHotel, HasStringProvider, HasWebViewLauncher, HasTripsTracking
