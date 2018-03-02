package com.expedia.bookings.hotel.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class HotelGalleryAnalyticsData(val creationTime: Long, val fromPackages: Boolean) : Parcelable
