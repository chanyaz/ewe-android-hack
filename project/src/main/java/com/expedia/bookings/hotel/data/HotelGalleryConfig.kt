package com.expedia.bookings.hotel.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class HotelGalleryConfig(val hotelName: String,
                              val hotelStarRating: Float,
                              val roomCode: String,
                              val showDescription: Boolean,
                              val startIndex: Int) : Parcelable