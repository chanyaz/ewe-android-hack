package com.expedia.bookings.services

import com.expedia.bookings.data.travelpulse.TravelPulseFetchResponse
import com.expedia.bookings.extensions.subscribeObserver
import io.reactivex.Observer
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class TravelPulseServices(endpoint: String, okHttpClient: OkHttpClient,
                          interceptor: Interceptor, travelPulseInterceptor: Interceptor,
                          val observeOn: Scheduler, val subscribeOn: Scheduler) {

    private val travelPulseApi: TravelPulseApi by lazy {
        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(interceptor).addInterceptor(travelPulseInterceptor).build())
                .build()

        adapter.create(TravelPulseApi::class.java)
    }

    fun fetchFavoriteHotels(siteId: String, clientId: String, expUserId: String, guid: String, langId: String, configId: String,
                            observer: Observer<TravelPulseFetchResponse>): Disposable {

        return travelPulseApi.fetch(siteId, clientId, expUserId, guid, langId, configId)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribeObserver(observer)
    }
}
