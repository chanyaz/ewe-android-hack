package com.expedia.bookings.hotel.data

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import com.expedia.bookings.hotel.DEFAULT_HOTEL_GALLERY_CODE
import com.expedia.bookings.hotel.HOTEL_GALLERY_TABLE_NAME

@Entity(tableName = "$HOTEL_GALLERY_TABLE_NAME")
data class PersistableHotelImageInfo(val url: String?,
                                     val displayText: String?,
                                     val roomCode: String = DEFAULT_HOTEL_GALLERY_CODE,
                                     val isRoomImage: Boolean = false) {
    @PrimaryKey(autoGenerate = true) var primaryKey: Long? = null
}