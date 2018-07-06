package com.expedia.bookings.services

import com.expedia.bookings.data.hotels.shortlist.HotelShortlistItem
import com.expedia.bookings.data.hotels.shortlist.HotelShortlistResponse
import com.expedia.bookings.data.hotels.shortlist.ShortlistItem
import com.expedia.bookings.data.hotels.shortlist.ShortlistItemMetadata
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import okhttp3.ResponseBody

interface HotelShortlistServicesInterface {
    fun fetchFavoriteHotels(observer: Observer<HotelShortlistResponse<HotelShortlistItem>>): Disposable
    fun saveFavoriteHotel(hotelId: String, metadata: ShortlistItemMetadata, observer: Observer<HotelShortlistResponse<ShortlistItem>>): Disposable
    fun removeFavoriteHotel(hotelId: String, observer: Observer<ResponseBody>): Disposable
}
