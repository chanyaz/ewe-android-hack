package com.expedia.bookings.services

import com.expedia.bookings.data.collections.Collection
import com.expedia.bookings.utils.Strings
import com.google.gson.GsonBuilder
import com.squareup.okhttp.OkHttpClient
import retrofit.RequestInterceptor
import retrofit.RestAdapter
import retrofit.client.OkClient
import retrofit.converter.GsonConverter
import rx.Observable
import rx.Observer
import rx.Scheduler
import rx.Subscription
import kotlin.properties.Delegates

public class CollectionServices(endpoint: String, okHttpClient: OkHttpClient, requestInterceptor: RequestInterceptor, val observeOn: Scheduler, val subscribeOn: Scheduler, logLevel: RestAdapter.LogLevel) {

    val collectionApi: CollectionApi by Delegates.lazy {
        val gson = GsonBuilder().create()

        val adapter = RestAdapter.Builder()
                .setEndpoint(endpoint)
                .setRequestInterceptor(requestInterceptor)
                .setLogLevel(logLevel)
                .setConverter(GsonConverter(gson))
                .setClient(OkClient(okHttpClient))
                .build()
        adapter.create(javaClass<CollectionApi>())
    }

    // On tablet, we want to get all of the collections at once.
    // TODO - implement "Your Search" and "Nearby Hotels" tile logic into this stream

    fun getTabletCollections(twoLetterCountryCode: String, localeCode: String, observer: Observer<MutableList<Collection>>): Subscription {
        return collectionApi.collections(twoLetterCountryCode, localeCode)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .map { response -> response.collections }
                .subscribe(observer)
    }

    // On tablet, we want to get only the phone collection and display it.

    fun getPhoneCollection(twoLetterCountryCode: String, localeCode: String, observer: Observer<Collection>): Subscription {
        return collectionApi.phoneCollection(twoLetterCountryCode, localeCode)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribe(observer)
    }
}
