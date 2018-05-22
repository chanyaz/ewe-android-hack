package com.expedia.bookings.data

import com.google.gson.annotations.SerializedName

class HolidayCalendarResponse {
    @SerializedName("holiday")
    var holidays: List<HolidayEntity> = emptyList()
}

data class HolidayEntity(
        @SerializedName("date")
        var holidayDateString: String,
        var holidayName: String)
