package com.expedia.bookings.itin.triplist

import com.expedia.bookings.data.trips.TripFolder
import com.expedia.bookings.itin.tripstore.utils.IJsonToFoldersUtil
import com.expedia.bookings.itin.tripstore.utils.ITripsJsonFileUtils
import com.expedia.bookings.services.TripFolderServiceInterface
import com.google.gson.Gson
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class TripListRepository @Inject constructor(
        private val jsonToFolderUtil: IJsonToFoldersUtil,
        private val tripFolderService: TripFolderServiceInterface,
        private val tripFolderJsonFileUtils: ITripsJsonFileUtils) : ITripListRepository {

    override val foldersSubject: PublishSubject<List<TripFolder>> = PublishSubject.create()

    override fun getTripFolders() {
        val folders = jsonToFolderUtil.getTripFoldersFromDisk()
        if (folders.isEmpty()) {
            refreshTripFolders()
        } else {
            foldersSubject.onNext(folders)
        }
    }

    override fun refreshTripFolders() {
        //TODO: dispose?
        tripFolderService.getTripFoldersObservable(object : DisposableObserver<List<TripFolder>>() {
            override fun onNext(folders: List<TripFolder>) {
                folders.forEach { folder ->
                    tripFolderJsonFileUtils.writeToFile(folder.tripFolderId, Gson().toJson(folder))
                }
                foldersSubject.onNext(folders)
            }

            override fun onComplete() {
                dispose()
            }

            override fun onError(e: Throwable) {}
        })
    }
}

interface ITripListRepository {
    val foldersSubject: PublishSubject<List<TripFolder>>
    fun getTripFolders()
    fun refreshTripFolders()
}
