package com.expedia.bookings.services

import com.expedia.bookings.data.trips.TripsShareUrlShortenResponse
import com.expedia.bookings.extensions.subscribeObserver
import com.google.gson.GsonBuilder
import io.reactivex.Observer
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class TripShareUrlShortenService(endpoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor, val observeOn: Scheduler, val subscribeOn: Scheduler) : TripShareUrlShortenServiceInterface {
    private val TIME_OUT_SECONDS = 2L

    val tripsShareUrlShortenApi: TripShareUrlShortenAPI by lazy {
        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(interceptor).build())
                .build()

        adapter.create(TripShareUrlShortenAPI::class.java)
    }

    override fun getShortenedShareUrl(url: String, observer: Observer<TripsShareUrlShortenResponse>): Disposable {
        val json = JSONObject()
        json.putOpt("long_url", url)
        val requestBody = RequestBody.create(MediaType.parse("application/json"), json.toString())

        val subscription = tripsShareUrlShortenApi.shortenURL(requestBody)
                .timeout(TIME_OUT_SECONDS, TimeUnit.SECONDS)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribeObserver(observer)

        return subscription
    }
}
