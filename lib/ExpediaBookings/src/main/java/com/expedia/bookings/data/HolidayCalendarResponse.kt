package com.expedia.bookings.data

import com.google.gson.annotations.SerializedName

class HolidayCalendarResponse {
    lateinit var holiday: HashMap<String, ArrayList<HolidayEntity>>
}

data class HolidayEntity(
        @SerializedName("date")
        var holidayDate: String,
        var holidayName: String)
