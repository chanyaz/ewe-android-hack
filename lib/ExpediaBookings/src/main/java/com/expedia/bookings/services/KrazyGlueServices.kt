package com.expedia.bookings.services

import com.expedia.bookings.data.flights.KrazyglueResponse
import com.expedia.bookings.subscribeObserver
import com.google.gson.GsonBuilder
import io.reactivex.Observer
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import okhttp3.OkHttpClient
import org.joda.time.DateTime
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class KrazyglueServices(endpoint: String, okHttpClient: OkHttpClient, val observeOn: Scheduler, val subscribeOn: Scheduler) {
    private val TIME_OUT_SECONDS = 10L

    val krazyglueApi: KrazyglueApi by lazy {
        val gson = GsonBuilder()
                .registerTypeAdapter(DateTime::class.java, DateTimeTypeAdapter())
                .create()

        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient.newBuilder().build())
                .build()

        adapter.create(KrazyglueApi::class.java)
    }

    var krazyglueSubscription: Disposable? = null

    fun getKrazyglueHotels(signedUrl: String, observer: Observer<KrazyglueResponse>) : Disposable {
        krazyglueSubscription?.dispose()

        val krazyglueSubscription = krazyglueApi.getKrazyglueHotels(signedUrl)
                .timeout(TIME_OUT_SECONDS, TimeUnit.SECONDS)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribeObserver(observer)
        this.krazyglueSubscription = krazyglueSubscription
        return krazyglueSubscription
    }
}
