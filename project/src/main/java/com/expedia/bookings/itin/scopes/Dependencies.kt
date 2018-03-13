package com.expedia.bookings.itin

import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinHotel
import com.expedia.bookings.itin.utils.IWebViewLauncher
import com.expedia.bookings.itin.utils.StringSource

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
