package com.mobiata.mocke3

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

class TripsDispatcher(fileOpener: FileOpener, private val dispatcherSettings: Map<Dispatchers, String>) : AbstractDispatcher(fileOpener) {

    override fun dispatch(request: RecordedRequest): MockResponse {
        var filename = dispatcherSettings[Dispatchers.TRIPS_DISPATCHER]
        if (filename.isNullOrBlank()) {
            filename = "tripfolders_m1_hotel"
        }
        return getMockResponse("api/trips/tripfolders/$filename.json")
    }
}