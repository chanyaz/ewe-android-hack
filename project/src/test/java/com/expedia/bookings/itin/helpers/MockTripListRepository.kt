package com.expedia.bookings.itin.helpers

import com.expedia.bookings.data.trips.TripFolder
import com.expedia.bookings.itin.triplist.ITripListRepository
import com.google.gson.Gson
import com.mobiata.mocke3.getJsonStringFromMock
import io.reactivex.subjects.BehaviorSubject

class MockTripListRepository : ITripListRepository {
    private val stringFromMock = getJsonStringFromMock("api/trips/tripfolders/tripfolders_happy_path_m1_hotel.json", null)
    val tripFolders = Gson().fromJson(stringFromMock, Array<TripFolder>::class.java).toList()
    private val stringFromMockForRefresh = getJsonStringFromMock("api/trips/tripfolders/tripfolders_three_hotels_one_cruise.json", null)
    val tripFoldersForRefresh = Gson().fromJson(stringFromMockForRefresh, Array<TripFolder>::class.java).toList()

    override val foldersSubject: BehaviorSubject<List<TripFolder>> = BehaviorSubject.create()
    override fun refreshTripFolders() {
        foldersSubject.onNext(tripFoldersForRefresh)
    }

    init {
        foldersSubject.onNext(tripFolders)
    }
}
