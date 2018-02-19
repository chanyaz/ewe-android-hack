package com.expedia.bookings.data.os

import com.expedia.bookings.data.BaseDealsRequest

class LastMinuteDealsRequest(tuid: String?) : BaseDealsRequest() {
    var stayDateRanges: String? = "THISWEEK,NEXTWEEK"
    var scenario: String? = "last-minute-deals"
    var uid: String? = tuid
    var page: String? = "App.LaunchScreen"
}
