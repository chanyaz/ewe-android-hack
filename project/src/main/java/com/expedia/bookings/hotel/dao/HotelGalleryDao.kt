package com.expedia.bookings.hotel.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import android.arch.persistence.room.Transaction
import com.expedia.bookings.hotel.HOTEL_GALLERY_TABLE_NAME
import com.expedia.bookings.hotel.data.PersistableHotelImageInfo

@Dao
abstract class HotelGalleryDao {
    @Query("SELECT * FROM $HOTEL_GALLERY_TABLE_NAME WHERE roomCode=:roomCode")
    abstract fun findImagesForCode(roomCode: String) : List<PersistableHotelImageInfo>?

    @Transaction
    open fun replaceImages(galleryItems: List<PersistableHotelImageInfo>, roomLevel: Boolean) {
        deleteAll(isRoomLevel = roomLevel)
        insertAll(galleryItems)
    }

    @Insert
    abstract fun insertAll(galleryItems: List<PersistableHotelImageInfo>)

    @Query("DELETE FROM $HOTEL_GALLERY_TABLE_NAME WHERE isRoomImage=:isRoomLevel")
    abstract fun deleteAll(isRoomLevel: Boolean)
}