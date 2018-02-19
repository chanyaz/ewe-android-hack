package com.expedia.bookings.services.sos

import com.expedia.bookings.data.sos.MemberDealsRequest
import com.expedia.bookings.data.sos.MemberDealsResponse
import com.google.gson.GsonBuilder
import io.reactivex.Observer
import io.reactivex.Scheduler
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class SmartOfferService(endpoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor,
                        val observeOn: Scheduler, val subscribeOn: Scheduler) : ISmartOfferService {
    val api: SmartOfferApi by lazy {
        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(interceptor).build())
                .build()

        adapter.create(SmartOfferApi::class.java)
    }

    override fun fetchDeals(request: MemberDealsRequest,
                            dealsObserver: Observer<MemberDealsResponse>) {
        return api.memberDeals(request.siteId, request.locale, request.productType, request.groupBy,
                request.destinationLimit, request.clientId)
                .subscribeOn(subscribeOn)
                .observeOn(observeOn)
                .subscribe(dealsObserver)
    }
}
