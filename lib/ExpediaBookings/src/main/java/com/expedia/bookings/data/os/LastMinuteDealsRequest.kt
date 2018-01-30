package com.expedia.bookings.data.os

import com.expedia.bookings.data.sos.DealsRequest

class LastMinuteDealsRequest(tuid: String?) : DealsRequest() {
    var stayDateRanges: String? = "THISWEEK,NEXTWEEK"
    var scenario: String? = "last-minute-deals"
    var uid: String? = tuid
    var page: String? = "App.LaunchScreen"
}
