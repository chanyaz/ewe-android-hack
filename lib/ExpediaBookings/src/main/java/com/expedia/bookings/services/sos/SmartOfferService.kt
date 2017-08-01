package com.expedia.bookings.services.sos

import com.expedia.bookings.data.sos.MemberDealRequest
import com.expedia.bookings.data.sos.MemberDealResponse
import com.expedia.bookings.subscribeObserver
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import io.reactivex.Observer
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable

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

    fun fetchMemberDeals(request: MemberDealRequest,
                         dealsObserver: Observer<MemberDealResponse>) : Disposable {

        return api.memberDeals(request.siteId, request.locale, request.productType, request.groupBy,
                request.destinationLimit, request.clientId)
                .subscribeOn(subscribeOn)
                .observeOn(observeOn)
                .subscribeObserver(dealsObserver)
    }
}