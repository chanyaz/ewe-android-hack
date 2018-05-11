package com.expedia.bookings.data

import com.google.gson.annotations.SerializedName

class HolidayCalendarResponse {
    @SerializedName("holiday")
    lateinit var holidays: List<HolidayEntity>
}

data class HolidayEntity(
        @SerializedName("date")
        var holidayDateString: String,
        var holidayName: String)
