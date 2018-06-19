package com.expedia.bookings.itin.tripsync

import com.expedia.bookings.data.trips.TripFolder
import com.expedia.bookings.itin.tripstore.utils.ITripsJsonFileUtils
import com.expedia.bookings.services.TripFolderServiceInterface
import com.google.gson.Gson
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.PublishSubject

class TripSync(private val tripFolderService: TripFolderServiceInterface, private val tripFolderJsonFileUtils: ITripsJsonFileUtils) : ITripSync {

    override val tripFoldersFetchedSubject: PublishSubject<Unit> = PublishSubject.create()

    override fun fetchTripFoldersFromApi() {
        tripFolderService.getTripFoldersObservable(getDisposableObserver())
    }

    private fun getDisposableObserver(): DisposableObserver<List<TripFolder>> {
        return object : DisposableObserver<List<TripFolder>>() {
            override fun onComplete() {
                tripFoldersFetchedSubject.onNext(Unit)
            }

            override fun onNext(folders: List<TripFolder>) {
                folders.forEach { folder ->
                    tripFolderJsonFileUtils.writeToFile(folder.tripFolderId, Gson().toJson(folder))
                }
            }

            override fun onError(e: Throwable) {
            }
        }
    }
}

interface ITripSync {
    val tripFoldersFetchedSubject: PublishSubject<Unit>
    fun fetchTripFoldersFromApi()
}
