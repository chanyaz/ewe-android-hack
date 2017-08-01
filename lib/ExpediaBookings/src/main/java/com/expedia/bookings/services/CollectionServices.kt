package com.expedia.bookings.services

import com.expedia.bookings.data.collections.Collection
import com.expedia.bookings.subscribeObserver
import com.google.gson.GsonBuilder
import io.reactivex.Observer
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class CollectionServices(endpoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor, val observeOn: Scheduler, val subscribeOn: Scheduler) {

    val collectionApi: CollectionApi by lazy {
        val gson = GsonBuilder().create()

        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(interceptor).build())
                .build()

        adapter.create(CollectionApi::class.java)
    }

    // On tablet, we want to get all of the collections at once.
    // TODO - implement "Your Search" and "Nearby Hotels" tile logic into this stream

    fun getTabletCollections(twoLetterCountryCode: String, localeCode: String, observer: Observer<MutableList<Collection>>): Disposable {
        return collectionApi.collections(twoLetterCountryCode, localeCode)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .map { response -> response.collections }
                .subscribeObserver(observer)
    }

    // On tablet, we want to get only the phone collection and display it.

    fun getPhoneCollection(phoneCollectionId: String, twoLetterCountryCode: String, localeCode: String, observer: Observer<Collection>): Disposable {
        return collectionApi.phoneCollection(phoneCollectionId, twoLetterCountryCode, localeCode)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribeObserver(observer)
    }
}
