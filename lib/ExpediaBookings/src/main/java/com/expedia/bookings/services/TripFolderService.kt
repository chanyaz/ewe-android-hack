package com.expedia.bookings.services

import com.expedia.bookings.extensions.subscribeObserver
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.json.JSONArray
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.http.GET

class TripFolderService(endpoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor, satelliteInterceptor: Interceptor, val observeOn: Scheduler, val subscribeOn: Scheduler) : TripFolderServiceInterface {

    private val tripFolderApi: TripFolderApi by lazy {
        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(JsonArrayConverterFactory())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient.newBuilder()
                        .addInterceptor(interceptor)
                        .addInterceptor(satelliteInterceptor)
                        .build())
                .build()

        adapter.create(TripFolderApi::class.java)
    }

    override fun getTripFoldersObservable(observer: Observer<JSONArray>): Disposable {
        return tripFolderApi.getTripFolders()
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribeObserver(observer)
    }
}

interface TripFolderServiceInterface {

    fun getTripFoldersObservable(observer: Observer<JSONArray>): Disposable
}

interface TripFolderApi {

    @GET("/m/api/trips/tripfolders")
    fun getTripFolders(): Observable<JSONArray>
}