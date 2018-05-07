package com.expedia.bookings.services

import com.expedia.bookings.data.flights.RichContentRequest
import com.expedia.bookings.data.flights.RichContentResponse
import com.expedia.bookings.extensions.subscribeObserver
import com.google.gson.GsonBuilder
import io.reactivex.Observer
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.joda.time.DateTime
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class FlightRichContentService(val endpoint: String, okHttpClient: OkHttpClient, interceptors: List<Interceptor>, val observeOn: Scheduler, val subscribeOn: Scheduler) {
    private val flightRichContentAPI: FlightRichContentApi by lazy {
        val gson = GsonBuilder()
                .registerTypeAdapter(DateTime::class.java, DateTimeTypeAdapter())
                .create()

        val okHttpClientBuilder = okHttpClient.newBuilder()

        for (interceptor in interceptors) {
            okHttpClientBuilder.addInterceptor(interceptor)
        }

        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClientBuilder.build())
                .build()

        adapter.create(FlightRichContentApi::class.java)
    }

    var richContentOutboundSubscription: Disposable? = null
    var richContentInboundSubscription: Disposable? = null

    fun getOutboundFlightRichContent(requestPayload: RichContentRequest, observer: Observer<RichContentResponse>): Disposable {
        richContentOutboundSubscription?.dispose()
        richContentOutboundSubscription = getRichContent(requestPayload, observer)
        return richContentOutboundSubscription as Disposable
    }

    fun getInboundFlightRichContent(requestPayload: RichContentRequest, observer: Observer<RichContentResponse>): Disposable {
        richContentInboundSubscription?.dispose()
        richContentInboundSubscription = getRichContent(requestPayload, observer)
        return richContentInboundSubscription as Disposable
    }

    private fun getRichContent(requestPayload: RichContentRequest, observer: Observer<RichContentResponse>): Disposable {
        return flightRichContentAPI.richContent(requestPayload)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribeObserver(observer)
    }
}
