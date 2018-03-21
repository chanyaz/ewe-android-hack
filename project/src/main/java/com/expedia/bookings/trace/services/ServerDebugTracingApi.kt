package com.expedia.bookings.trace.services

import com.expedia.bookings.trace.data.DebugTraceToken
import retrofit2.http.GET
import io.reactivex.Observable

interface ServerDebugTracingApi {

    @GET("api/v1/debug-trace-token/")
    fun getDebugTraceToken(): Observable<DebugTraceToken>
}
