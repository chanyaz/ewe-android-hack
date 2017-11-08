package com.expedia.bookings.data.user

import com.crashlytics.android.Crashlytics

open class ExceptionLoggingProvider {
    open fun logException(throwable: Throwable) {
        Crashlytics.logException(throwable)
    }
}