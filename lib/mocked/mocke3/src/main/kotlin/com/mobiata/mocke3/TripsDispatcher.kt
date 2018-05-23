package com.mobiata.mocke3

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

class TripsDispatcher(fileOpener: FileOpener, private val dispatcherSettings: Map<DispatcherSettingsKeys, String>) : AbstractDispatcher(fileOpener) {

    override fun dispatch(request: RecordedRequest): MockResponse {
        var filename = dispatcherSettings[DispatcherSettingsKeys.TRIPS_DISPATCHER]
        if (filename.isNullOrBlank()) {
            filename = "tripfolders_happy_path_m1_hotel"
        }
        return getMockResponse("api/trips/tripfolders/$filename.json")
    }
}