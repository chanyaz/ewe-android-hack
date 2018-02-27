package com.expedia.bookings.tracking

open class DownloadableFontsTimeLogger(val fontName: String, pageName: String, timeSource: TimeSource = TimeSourceInMillis()) : TimeLogger(pageName = pageName, timeSource = timeSource)
