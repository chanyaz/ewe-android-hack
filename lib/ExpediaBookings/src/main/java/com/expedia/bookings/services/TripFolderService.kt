package com.expedia.bookings.services

import com.expedia.bookings.data.trips.TripFolder
import com.expedia.bookings.extensions.subscribeObserver
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class TripFolderService(endpoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor, satelliteInterceptor: Interceptor, val observeOn: Scheduler, val subscribeOn: Scheduler) : TripFolderServiceInterface {

    private val tripFolderApi: TripFolderApi by lazy {
        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient.newBuilder()
                        .addInterceptor(interceptor)
                        .addInterceptor(satelliteInterceptor)
                        .build())
                .build()
        adapter.create(TripFolderApi::class.java)
    }

    override fun getTripFolders(observer: Observer<List<TripFolder>>): Disposable {
        return tripFolderApi.getTripFolders()
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribeObserver(observer)
    }
}

interface TripFolderServiceInterface {

    fun getTripFolders(observer: Observer<List<TripFolder>>): Disposable
}

interface TripFolderApi {

    @GET("/m/api/trips/tripfolders")
    fun getTripFolders(): Observable<List<TripFolder>>
}
