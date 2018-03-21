package com.expedia.bookings.itin.tripstore.extensions

import com.expedia.bookings.itin.tripstore.data.ItinHotel
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinLx

fun Itin.firstHotel(): ItinHotel? {
    val packageHotels = packages?.first()?.hotels ?: emptyList()
    val standAloneHotels = hotels.orEmpty()
    return packageHotels.plus(standAloneHotels).firstOrNull()
}

fun Itin.eligibleForRewards(): Boolean {
    return rewardList != null && rewardList.isNotEmpty()
}

fun Itin.packagePrice(): String? {
    val firstPackage = packages?.first()
    return firstPackage?.price?.totalFormatted
}

fun Itin.firstLx(): ItinLx? {
    val packageLx = packages?.first()?.activities ?: emptyList()
    val standAloneLx = activities.orEmpty()
    return packageLx.plus(standAloneLx).firstOrNull()
}
