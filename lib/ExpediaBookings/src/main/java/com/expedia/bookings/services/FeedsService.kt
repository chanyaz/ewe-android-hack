package com.expedia.bookings.services

import com.expedia.bookings.data.feeds.FeedsResponse
import com.google.gson.GsonBuilder
import com.squareup.okhttp.OkHttpClient
import retrofit.RequestInterceptor
import retrofit.RestAdapter
import retrofit.client.OkClient
import retrofit.converter.GsonConverter
import rx.Observer
import rx.Scheduler
import rx.Subscription

class FeedsService(endpoint: String, okHttpClient: OkHttpClient, requestInterceptor: RequestInterceptor, val observeOn: Scheduler, val subscribeOn: Scheduler, logLevel: RestAdapter.LogLevel) {

    val feedsApi: FeedsApi by lazy {
        val gson = GsonBuilder().create()

        val adapter = RestAdapter.Builder()
                .setEndpoint(endpoint)
                .setRequestInterceptor(requestInterceptor)
                .setLogLevel(logLevel)
                .setConverter(GsonConverter(gson))
                .setClient(OkClient(okHttpClient))
                .build()
        adapter.create(FeedsApi::class.java)
    }

    fun getFeeds(tuid: String, expUserID: String, observer: Observer<FeedsResponse>): Subscription {
        return feedsApi.feeds(tuid, expUserID)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribe(observer)
    }

}
