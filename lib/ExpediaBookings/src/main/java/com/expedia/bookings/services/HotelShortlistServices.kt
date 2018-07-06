package com.expedia.bookings.services

import com.expedia.bookings.data.hotels.shortlist.HotelShortlistItem
import com.expedia.bookings.data.hotels.shortlist.HotelShortlistResponse
import com.expedia.bookings.data.hotels.shortlist.ShortlistItem
import com.expedia.bookings.data.hotels.shortlist.ShortlistItemMetadata
import com.expedia.bookings.extensions.subscribeObserver
import io.reactivex.Observer
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class HotelShortlistServices(endpoint: String, okHttpClient: OkHttpClient,
                             interceptor: Interceptor, hotelShortlistInterceptor: Interceptor,
                             val observeOn: Scheduler, val subscribeOn: Scheduler) : HotelShortlistServicesInterface {

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

    override fun fetchFavoriteHotels(observer: Observer<HotelShortlistResponse<HotelShortlistItem>>): Disposable {
        return hotelShortListApi.fetch(CONFIG_ID)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribeObserver(observer)
    }

    override fun saveFavoriteHotel(hotelId: String, metadata: ShortlistItemMetadata, observer: Observer<HotelShortlistResponse<ShortlistItem>>): Disposable {
        return hotelShortListApi.save(metadata, hotelId, CONFIG_ID, PAGE_NAME)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribeObserver(observer)
    }

    override fun removeFavoriteHotel(hotelId: String, observer: Observer<ResponseBody>): Disposable {
        val metadata = ShortlistItemMetadata()
        return hotelShortListApi.remove(metadata, hotelId, CONFIG_ID, PAGE_NAME)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribeObserver(observer)
    }
}
