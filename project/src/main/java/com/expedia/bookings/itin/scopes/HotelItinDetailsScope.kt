package com.expedia.bookings.itin.scopes

import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinHotel
import com.expedia.bookings.itin.utils.IWebViewLauncher
import com.expedia.bookings.itin.utils.StringSource

data class HotelItinDetailsScope(
        override val itin: Itin,
        override val hotel: ItinHotel,
        override val strings: StringSource,
        override val webViewLauncher: IWebViewLauncher
) : HasItin, HasHotel, HasStringProvider, HasWebViewLauncher
