package com.expedia.bookings.services

import com.expedia.bookings.data.hotelshortlist.HotelShortlistFetchResponse
import io.reactivex.Observer
import io.reactivex.Scheduler
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class HotelShortlistServices(endpoint: String, okHttpClient: OkHttpClient,
                             interceptor: Interceptor, hotelShortlistInterceptor: Interceptor,
                             val observeOn: Scheduler, val subscribeOn: Scheduler) {

    private val hotelShortListApi: HotelShortlistApi by lazy {
        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(interceptor).addInterceptor(hotelShortlistInterceptor).build())
                .build()

        adapter.create(HotelShortlistApi::class.java)
    }

    fun fetchFavoriteHotels(observer: Observer<HotelShortlistFetchResponse>) {
        val configId = "hotel"
        return hotelShortListApi.fetch(configId)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribe(observer)
    }
}
