package com.expedia.bookings.services

import com.expedia.bookings.data.hotels.WeatherParams
import com.expedia.bookings.data.itin.WUndergroundSearchResponse
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import rx.Observable
import rx.Scheduler

class WeatherServices(endpoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor, val observeOn: Scheduler, val subscribeOn: Scheduler) {

    val weatherApi: WUndergroundApi by lazy {
        val gson = GsonBuilder().create()

        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(interceptor).build())
                .build()

        adapter.create(WUndergroundApi::class.java)
    }

    fun getWeather(params: WeatherParams): Observable<WUndergroundSearchResponse> {
        return weatherApi.getWeather(params.brand, params.getQuery())
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
    }
}
