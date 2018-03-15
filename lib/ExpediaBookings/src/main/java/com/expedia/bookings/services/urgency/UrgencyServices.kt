package com.expedia.bookings.services.urgency

import com.expedia.bookings.data.urgency.UrgencyResponse
import com.expedia.bookings.services.DateTimeTypeAdapter
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.joda.time.DateTime
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import io.reactivex.Observable
import io.reactivex.Scheduler

open class UrgencyServices(
    endpoint: String,
    okHttpClient: OkHttpClient,
    interceptor: Interceptor,
    private val observeOn: Scheduler,
    private val subscribeOn: Scheduler
) {
    val clientId = "lodgingmobileapp"

    private val api: UrgencyApi by lazy {
        val gson = GsonBuilder()
                .registerTypeAdapter(DateTime::class.java, DateTimeTypeAdapter())
                .create()

        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(interceptor).build())
                .build()

        adapter.create(UrgencyApi::class.java)
    }

    open fun compressionUrgency(regionId: String, checkIn: String, checkOut: String): Observable<UrgencyResponse> {
        return api.compression(regionId, checkIn, checkOut, clientId).observeOn(observeOn)
                .subscribeOn(subscribeOn)
    }
}
