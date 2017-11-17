package com.expedia.bookings.data.sos

class LastMinuteDealsRequest: DealsRequest() {
    var stayDateRanges: String? = "THISWEEK,NEXTWEEK"
}
