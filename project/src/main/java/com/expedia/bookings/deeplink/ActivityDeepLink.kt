package com.expedia.bookings.deeplink

import org.joda.time.LocalDate

class ActivityDeepLink : DeepLink() {
    var startDate: LocalDate? = null
    var location: String? = null
    var activityID: String? = null
    var filters: String? = null
}
