package com.expedia.bookings.data.multiitem

import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.hotels.HotelOffersResponse

interface BundleHotelRoomResponse {
    fun getBundleRoomResponse(): List<HotelOffersResponse.HotelRoomResponse>
    fun hasRoomResponseErrors(): Boolean
    val roomResponseFirstErrorCode: ApiError.Code
    fun getHotelCheckInDate(): String
    fun getHotelCheckOutDate(): String
}
