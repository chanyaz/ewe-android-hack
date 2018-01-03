package com.expedia.bookings.services

import com.expedia.bookings.data.trips.TripsShareUrlShortenResponse
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.Observer
import rx.Scheduler
import rx.Subscription
import java.util.concurrent.TimeUnit

class TripShareUrlShortenService(endpoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor, val observeOn: Scheduler, val subscribeOn: Scheduler) : TripShareUrlShortenServiceInterface {
    private val TIME_OUT_SECONDS = 2L

    val tripsShareUrlShortenApi: TripShareUrlShortenAPI by lazy {
        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(interceptor).build())
                .build()

        adapter.create(TripShareUrlShortenAPI::class.java)
    }

    override fun getShortenedShareUrl(url: String, observer: Observer<TripsShareUrlShortenResponse>): Subscription {
        val json = JSONObject()
        json.putOpt("long_url", url)
        val requestBody = RequestBody.create(MediaType.parse("application/json"), json.toString())

        val subscription = tripsShareUrlShortenApi.shortenURL(requestBody)
                .timeout(TIME_OUT_SECONDS, TimeUnit.SECONDS)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribe(observer)

        return subscription as Subscription
    }
}
