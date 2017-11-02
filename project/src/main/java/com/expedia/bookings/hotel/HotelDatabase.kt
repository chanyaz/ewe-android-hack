package com.expedia.bookings.hotel

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import com.expedia.bookings.hotel.dao.HotelGalleryDao
import com.expedia.bookings.hotel.data.PersistableHotelImageInfo


@Database(entities = arrayOf(PersistableHotelImageInfo::class),
        version = 1)
abstract class HotelDatabase : RoomDatabase() {
    abstract fun hotelGalleryDao(): HotelGalleryDao
}