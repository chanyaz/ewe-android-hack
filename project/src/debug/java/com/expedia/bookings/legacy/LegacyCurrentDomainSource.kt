package com.expedia.bookings.legacy

import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.utils.CurrentDomainSource

class LegacyCurrentDomainSource : CurrentDomainSource {
    override fun currentDomain(): String {
        return PointOfSale.getPointOfSale().url
    }
}
