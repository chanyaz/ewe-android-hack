package com.expedia.bookings.services.sos

import com.expedia.bookings.data.sos.MemberOnlyDealRequest
import com.expedia.bookings.data.sos.MemberOnlyDealResponse
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.Observer
import rx.Scheduler
import rx.Subscription

class SmartOfferService(endpoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor,
                        val observeOn: Scheduler, val subscribeOn: Scheduler) {
    val api: SmartOfferApi by lazy {

        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(interceptor).build())
                .build()

        adapter.create(SmartOfferApi::class.java)
    }

    fun fetchMemberOnlyDeals(request: MemberOnlyDealRequest,
                             dealsObserver: Observer<MemberOnlyDealResponse>) : Subscription {

        return api.memberOnlyDeals(request.siteId, request.locale, request.productType, request.groupBy,
                request.destinationLimit, request.clientId)
                .subscribeOn(subscribeOn)
                .observeOn(observeOn)
                .subscribe(dealsObserver)
    }
}