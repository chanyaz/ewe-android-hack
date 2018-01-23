package com.expedia.bookings.data

class ItinDetailsResponse : AbstractItinDetailsResponse() {

    var responseData: ResponseData? = null

    override fun getResponseDataForItin(): ResponseData? {
        return responseData
    }
}
