package com.expedia.bookings.trace.util

import android.os.SystemClock
import com.expedia.bookings.trace.data.DebugTraceToken
import com.expedia.bookings.trace.services.ServerDebugTracingServices
import io.reactivex.Observer
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.PublishSubject

class ServerDebugTraceUtil {
    companion object {

        @JvmField
        var debugTraceData = ArrayList<Pair<String, String>>()

        val fetchingSubject = PublishSubject.create<Unit>()
        val successSubject = PublishSubject.create<Unit>()
        val errorSubject = PublishSubject.create<Throwable>()
        val disabledSubject = PublishSubject.create<Unit>()

        private var debugTraceEnabled = false
        private var debugTokenAndTimeStamp: DebugTokenAndTimestamp? = null
        private val serverDebugTracingServices = ServerDebugTracingServices()
        private const val tokenLifeSpanMilliSec: Long = 600000

        fun toggleDebugTrace() {
            debugTraceEnabled = !debugTraceEnabled
            if (debugTraceEnabled) {
                getDebugTokenAndRefreshIfNeeded()
            } else {
                debugTokenAndTimeStamp = null
                disabledSubject.onNext(Unit)
            }
        }

        @JvmStatic
        fun isDebugTracingAvailable(): Boolean {
            return !debugTokenAndTimeStamp?.debugToken.isNullOrBlank()
        }

        @JvmStatic
        fun getDebugTokenAndRefreshIfNeeded(): String? {
            if (debugTraceEnabled && (debugTokenAndTimeStamp == null || !isTimestampActive(debugTokenAndTimeStamp?.timeStamp))) {
                requestDebugTraceToken()
            }
            return debugTokenAndTimeStamp?.debugToken
        }

        private fun isTimestampActive(timeStamp: Long?): Boolean {
            if (timeStamp == null) {
                return false
            } else {
                val currentTime = SystemClock.elapsedRealtime()
                return (currentTime - timeStamp) < tokenLifeSpanMilliSec
            }
        }

        private fun requestDebugTraceToken() {
            serverDebugTracingServices.getDebugTraceToken(getDebugTraceTokenResponseObserver())
            fetchingSubject.onNext(Unit)
        }

        private fun getDebugTraceTokenResponseObserver(): Observer<DebugTraceToken> {
            return object : DisposableObserver<DebugTraceToken>() {
                override fun onNext(debugTraceToken: DebugTraceToken) {
                    if (debugTraceToken.data.isNullOrBlank()) {
                        val error = Throwable("Api returned invalid token", null)
                        errorSubject.onNext(error)
                    } else {
                        val debugToken = debugTraceToken.data!!
                        val currentTime = SystemClock.elapsedRealtime()
                        debugTokenAndTimeStamp = DebugTokenAndTimestamp(debugToken, currentTime)
                        successSubject.onNext(Unit)
                    }
                }

                override fun onComplete() {
                }

                override fun onError(e: Throwable) {
                    errorSubject.onNext(e)
                }
            }
        }
    }

    private data class DebugTokenAndTimestamp(val debugToken: String, val timeStamp: Long)
}
