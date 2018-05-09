package com.expedia.bookings.services

import io.reactivex.Observable
import io.reactivex.Scheduler
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.internal.http.RealResponseBody

class TestSuggestionV4Services(essEndpoint: String, gaiaEndPoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor,
                               essInterceptor: Interceptor, gaiaInterceptor: Interceptor, val observeon: Scheduler, val subscribeon: Scheduler)
    : SuggestionV4Services(essEndpoint, gaiaEndPoint, okHttpClient, interceptor, essInterceptor, gaiaInterceptor, observeon, subscribeon) {

    override fun essDomainResolution(): Observable<ResponseBody> {
        return Observable.just(RealResponseBody("cshjbjhc", 0L, null))
    }
}
