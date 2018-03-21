package com.expedia.bookings.trace.services

import com.expedia.bookings.trace.data.DebugTraceToken
import com.expedia.bookings.extensions.subscribeObserver
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class ServerDebugTracingServices(endpoint: String = "https://lobot-api-java.us-east-1.prod.expedia.com/") {

    private val serverDebugTracingApi: ServerDebugTracingApi by lazy {
        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        adapter.create(ServerDebugTracingApi::class.java)
    }

    fun getDebugTraceToken(observer: Observer<DebugTraceToken>): Disposable {
        return serverDebugTracingApi.getDebugTraceToken()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeObserver(observer)
    }
}
