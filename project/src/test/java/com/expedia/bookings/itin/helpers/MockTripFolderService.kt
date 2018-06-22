package com.expedia.bookings.itin.helpers

import com.expedia.bookings.data.trips.TripFolder
import com.expedia.bookings.extensions.subscribeObserver
import com.expedia.bookings.services.TripFolderServiceInterface
import com.google.gson.Gson
import com.mobiata.mocke3.getJsonStringFromMock
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

class MockTripFolderService : TripFolderServiceInterface {
    override fun getTripFolders(observer: Observer<List<TripFolder>>): Disposable {
        val stringFromMock = getJsonStringFromMock("api/trips/tripfolders/tripfolders_happy_path_m1_hotel.json", null)
        val tripFolders = Gson().fromJson(stringFromMock, Array<TripFolder>::class.java).toList()
        return Observable.just(tripFolders).subscribeObserver(observer)
    }
}
