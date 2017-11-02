package com.expedia.bookings.hotel.util

import com.expedia.bookings.R
import com.expedia.bookings.data.HotelMedia
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.utils.Images
import java.util.ArrayList
import android.arch.persistence.room.Room
import android.content.Context
import com.expedia.bookings.hotel.DEFAULT_HOTEL_GALLERY_CODE
import com.expedia.bookings.hotel.HotelDatabase
import com.expedia.bookings.hotel.HOTEL_DB_NAME
import com.expedia.bookings.hotel.dao.HotelGalleryDao
import com.expedia.bookings.hotel.data.PersistableHotelImageInfo
import com.expedia.bookings.utils.Images.getMediaHost
import rx.Completable
import rx.Observable
import rx.Observer
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.concurrent.Callable

open class HotelGalleryManager(context: Context) {

    private val db: HotelDatabase by lazy {
        Room.databaseBuilder(context, HotelDatabase::class.java, HOTEL_DB_NAME).build()
    }

    protected open val galleryDao: HotelGalleryDao by lazy { db.hotelGalleryDao() }
    protected open val workScheduler = Schedulers.io()

    private val converter = HotelMediaStorageConverter()

    fun saveHotelOfferMedia(response: HotelOffersResponse) {
        val images = Images.getHotelImages(response, R.drawable.room_fallback)
        saveOverviewImages(images)
        response.hotelRoomResponse?.let { saveRoomImages(response.hotelRoomResponse) }
    }

    fun fetchMediaList(roomCode: String, observer: Observer<ArrayList<HotelMedia>>) : Subscription {
        return Observable.fromCallable(FetchMediaListCallable(roomCode))
                .subscribeOn(workScheduler)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer)
    }

    private fun saveOverviewImages(images: List<HotelMedia>) {
        if (images.isNotEmpty()) {
            val persistableImages = converter.toPersistableList(images, DEFAULT_HOTEL_GALLERY_CODE)
            saveToDatabase(persistableImages, roomLevel = false)
        } else {
            clearFromDatabase(roomLevel = false)
        }
    }

    private fun saveRoomImages(rooms: List<HotelOffersResponse.HotelRoomResponse>) {
        val persistableImages = ArrayList<PersistableHotelImageInfo>()
        for (i  in rooms.indices) {
            val room = rooms[i]
            room.roomThumbnailUrlArray?.forEach { url ->
                val persistable = converter.toPersistable(HotelMedia(getMediaHost() + url), room.roomTypeCode)
                persistableImages.add(persistable)
            }
        }
        saveToDatabase(persistableImages, roomLevel = true)
    }

    private fun saveToDatabase(imageList: List<PersistableHotelImageInfo>,
                               roomLevel: Boolean) {
        Completable.fromCallable(ReplaceImagesCallable(imageList, roomLevel))
                .subscribeOn(workScheduler)
                .subscribe()
    }

    private fun clearFromDatabase(roomLevel: Boolean) {
        Completable.create {
            galleryDao.deleteAll(roomLevel)
        }.subscribeOn(workScheduler).subscribe()
    }

    private inner class FetchMediaListCallable(private val roomCode: String) : Callable<ArrayList<HotelMedia>> {
        override fun call(): ArrayList<HotelMedia> {
            val persistableList = galleryDao.findImagesForCode(roomCode)
            persistableList?.let {
                return converter.toHotelMediaList(persistableList)
            }
            return ArrayList<HotelMedia>()
        }
    }

    private inner class ReplaceImagesCallable(private val images: List<PersistableHotelImageInfo>,
                                              private val roomLevel: Boolean) : Callable<Unit> {
        override fun call() {
            galleryDao.replaceImages(images, roomLevel)
        }
    }
}