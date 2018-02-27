package com.expedia.bookings.tracking

class DownloadableFontsFailureLogger(fontName: String, timeSource: TimeSource = TimeSourceInMillis()) : DownloadableFontsTimeLogger(timeSource = timeSource, fontName = fontName, pageName = "App.DownloadableFonts.Failure")
