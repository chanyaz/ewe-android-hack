package com.expedia.bookings.services.os

import com.expedia.bookings.data.os.LastMinuteDealsRequest
import com.expedia.bookings.data.os.LastMinuteDealsResponse
import com.google.gson.GsonBuilder
import io.reactivex.Observer
import io.reactivex.Scheduler
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class OfferService(
    endpoint: String,
    okHttpClient: OkHttpClient,
    interceptor: Interceptor,
    val observeOn: Scheduler,
    val subscribeOn: Scheduler
) : IOfferService {
    val api: OfferApi by lazy {

        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(interceptor).build())
                .build()

        adapter.create(OfferApi::class.java)
    }

    override fun fetchDeals(
        request: LastMinuteDealsRequest,
        dealsObserver: Observer<LastMinuteDealsResponse>
    ) {
        return api.lastMinuteDeals(request.siteId, request.locale, request.groupBy, request.productType, request.destinationLimit, request.clientId, request.page, request.uid, request.scenario, request.stayDateRanges)
                .subscribeOn(subscribeOn)
                .observeOn(observeOn)
                .subscribe(dealsObserver)
    }
}
