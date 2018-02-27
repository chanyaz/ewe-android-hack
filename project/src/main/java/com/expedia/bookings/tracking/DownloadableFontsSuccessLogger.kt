package com.expedia.bookings.tracking

class DownloadableFontsSuccessLogger(fontName: String, timeSource: TimeSource = TimeSourceInMillis()) : DownloadableFontsTimeLogger(timeSource = timeSource, fontName = fontName, pageName = "App.DownloadableFonts.Success")
