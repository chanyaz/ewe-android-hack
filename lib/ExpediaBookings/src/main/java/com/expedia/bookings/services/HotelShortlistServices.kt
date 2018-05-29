package com.expedia.bookings.services

import com.expedia.bookings.data.hotels.shortlist.HotelShortlistItem
import com.expedia.bookings.data.hotels.shortlist.HotelShortlistResponse
import com.expedia.bookings.data.hotels.shortlist.ShortlistItem
import com.expedia.bookings.data.hotels.shortlist.ShortlistItemMetadata
import io.reactivex.Observer
import io.reactivex.Scheduler
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.joda.time.LocalDate
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class HotelShortlistServices(endpoint: String, okHttpClient: OkHttpClient,
                             interceptor: Interceptor, hotelShortlistInterceptor: Interceptor,
                             val observeOn: Scheduler, val subscribeOn: Scheduler) {

    private val CONFIG_ID = "hotel"
    private val PAGE_NAME = "scratchpad"

    private val hotelShortListApi: HotelShortlistApi by lazy {
        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(interceptor).addInterceptor(hotelShortlistInterceptor).build())
                .build()

        adapter.create(HotelShortlistApi::class.java)
    }

    fun fetchFavoriteHotels(observer: Observer<HotelShortlistResponse<HotelShortlistItem>>) {
        return hotelShortListApi.fetch(CONFIG_ID)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribe(observer)
    }

    fun saveFavoriteHotel(hotelId: String, checkIn: LocalDate, checkOut: LocalDate, roomConfiguration: String, observer: Observer<HotelShortlistResponse<ShortlistItem>>) {
        val metadata = ShortlistItemMetadata().apply {
            this.hotelId = hotelId
            chkIn = checkIn.toString("yyyyMMdd")
            chkOut = checkOut.toString("yyyyMMdd")
            this.roomConfiguration = roomConfiguration
        }
        return hotelShortListApi.save(metadata, hotelId, CONFIG_ID, PAGE_NAME)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribe(observer)
    }

    fun removeFavoriteHotel(hotelId: String, metadata: ShortlistItemMetadata, observer: Observer<ResponseBody>) {
        return hotelShortListApi.remove(metadata, hotelId, CONFIG_ID, PAGE_NAME)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribe(observer)
    }
}
