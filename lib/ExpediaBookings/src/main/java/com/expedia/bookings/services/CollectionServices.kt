package com.expedia.bookings.services

import com.expedia.bookings.data.collections.Collection
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.Observer
import rx.Scheduler
import rx.Subscription

class CollectionServices(endpoint: String, okHttpClient: OkHttpClient, val observeOn: Scheduler, val subscribeOn: Scheduler) {

    val collectionApi: CollectionApi by lazy {
        val gson = GsonBuilder().create()

        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient)
                .build()

        adapter.create(CollectionApi::class.java)
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

    fun getPhoneCollection(phoneCollectionId: String, twoLetterCountryCode: String, localeCode: String, observer: Observer<Collection>): Subscription {
        return collectionApi.phoneCollection(phoneCollectionId, twoLetterCountryCode, localeCode)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribe(observer)
    }
}
