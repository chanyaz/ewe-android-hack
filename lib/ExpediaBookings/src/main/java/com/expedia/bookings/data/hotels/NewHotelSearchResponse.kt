package com.expedia.bookings.data.hotels

import com.google.gson.annotations.SerializedName

class NewHotelSearchResponse {
    @SerializedName("pageData")
    var pageSummaryData: PageSummaryData? = null
    var hotels: List<HotelInfo> = emptyList()
    var errors: List<ErrorInfo> = emptyList()

    class ErrorInfo {
        var message: String = ""
        var localizedMessage: String = ""
        var errors: List<String> = emptyList()
    }
}
