package com.expedia.bookings.deeplink

import org.joda.time.DateTime

class CarDeepLink : DeepLink() {
    var pickupDateTime: DateTime? = null
    var dropoffDateTime: DateTime? = null
    var pickupLocation: String? = null
    var pickupLocationLat: String? = null
    var pickupLocationLng: String? = null
    var originDescription: String? = null
}
