package com.expedia.bookings.itin.tripstore.extensions

import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinCar
import com.expedia.bookings.itin.tripstore.data.ItinCruise
import com.expedia.bookings.itin.tripstore.data.ItinFlight
import com.expedia.bookings.itin.tripstore.data.ItinHotel
import com.expedia.bookings.itin.tripstore.data.ItinLx
import com.expedia.bookings.itin.tripstore.data.ItinRail
import com.expedia.bookings.utils.JodaUtils
import org.joda.time.DateTime

//interface to treat contents of Itin and ItinPackage to be the same
interface HasProducts {
    val hotels: List<ItinHotel>?
    val flights: List<ItinFlight>?
    val activities: List<ItinLx>?
    val cars: List<ItinCar>?
    val cruises: List<ItinCruise>?
    val rails: List<ItinRail>?
    fun listOfTripProducts(): List<TripProducts> = makeListOfTripProducts(this)
}

//enum representing the various products available in an itin
enum class TripProducts {
    HOTEL,
    FLIGHT,
    CAR,
    ACTIVITY,
    RAIL,
    CRUISE
}

fun Itin.firstHotel(): ItinHotel? {
    val packageHotels = packages?.firstOrNull()?.hotels ?: emptyList()
    val standAloneHotels = hotels.orEmpty()
    return packageHotels.plus(standAloneHotels).firstOrNull()
}

fun Itin.firstLx(): ItinLx? {
    val packageLx = packages?.firstOrNull()?.activities ?: emptyList()
    val standAloneLx = activities.orEmpty()
    return packageLx.plus(standAloneLx).firstOrNull()
}

fun Itin.isMultiItemCheckout(): Boolean {
    val totalNumberOfItems = hotels.orEmpty().size +
            flights.orEmpty().size +
            cars.orEmpty().size +
            activities.orEmpty().size +
            rails.orEmpty().size +
            cruises.orEmpty().size
    return !isPackage() && (totalNumberOfItems > 1)
}

fun Itin.isPackage(): Boolean {
    return packages != null
}

fun Itin.tripStartDate(): DateTime? {
    val epochSeconds = startTime?.epochSeconds
    val timezoneOffset = startTime?.timeZoneOffsetSeconds
    if (epochSeconds != null && timezoneOffset != null) {
        return JodaUtils.fromMillisAndOffset(epochSeconds * 1000, timezoneOffset * 1000)
    }
    return null
}

fun Itin.tripEndDate(): DateTime? {
    val epochSeconds = endTime?.epochSeconds
    val timezoneOffset = endTime?.timeZoneOffsetSeconds
    if (epochSeconds != null && timezoneOffset != null) {
        return JodaUtils.fromMillisAndOffset(epochSeconds * 1000, timezoneOffset * 1000)
    }
    return null
}

fun Itin.eligibleForRewards(): Boolean {
    return rewardList != null && rewardList.isNotEmpty()
}

fun Itin.packagePrice(): String? {
    return packages?.firstOrNull()?.price?.totalFormatted
}

fun makeListOfTripProducts(productsContainer: HasProducts): List<TripProducts> {
    val productList = mutableListOf<TripProducts>()
    if (productsContainer.hotels.hasItems()) productList.add(TripProducts.HOTEL)
    if (productsContainer.flights.hasItems()) productList.add(TripProducts.FLIGHT)
    if (productsContainer.cars.hasItems()) productList.add(TripProducts.CAR)
    if (productsContainer.activities.hasItems()) productList.add(TripProducts.ACTIVITY)
    if (productsContainer.rails.hasItems()) productList.add(TripProducts.RAIL)
    if (productsContainer.cruises.hasItems()) productList.add(TripProducts.CRUISE)
    return productList
}

private fun List<Any>?.hasItems(): Boolean {
    return this.orEmpty().isNotEmpty()
}
