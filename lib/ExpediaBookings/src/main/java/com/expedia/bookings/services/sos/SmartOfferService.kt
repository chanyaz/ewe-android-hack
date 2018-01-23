package com.expedia.bookings.services.sos

import com.expedia.bookings.data.sos.DealsResponse
import com.expedia.bookings.data.sos.LastMinuteDealsRequest
import com.expedia.bookings.data.sos.MemberDealsRequest
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

class SmartOfferService(endpoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor,
                        val observeOn: Scheduler, val subscribeOn: Scheduler) {
    val api: SmartOfferApi by lazy {

        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(interceptor).build())
                .build()

        adapter.create(SmartOfferApi::class.java)
    }

    fun fetchMemberDeals(request: MemberDealsRequest,
                         dealsObserver: Observer<DealsResponse>): Disposable {

        return api.memberDeals(request.siteId, request.locale, request.productType, request.groupBy,
                request.destinationLimit, request.clientId)
                .subscribeOn(subscribeOn)
                .observeOn(observeOn)
                .subscribeObserver(dealsObserver)
    }

    fun fetchLastMinuteDeals(request: LastMinuteDealsRequest,
                             dealsObserver: Observer<DealsResponse>): Disposable {
        return api.lastMinuteDeals(request.siteId, request.locale, request.groupBy, request.productType, request.destinationLimit, request.clientId, request.stayDateRanges)
                .subscribeOn(subscribeOn)
                .observeOn(observeOn)
                .subscribeObserver(dealsObserver)
    }
}
