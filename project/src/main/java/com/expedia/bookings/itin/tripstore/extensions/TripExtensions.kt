package com.expedia.bookings.itin.tripstore.extensions

import com.expedia.bookings.itin.common.TripProducts
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

fun Itin.firstCar(): ItinCar? {
    val packageCar = packages?.first()?.cars ?: emptyList()
    val standAloneCar = cars.orEmpty()
    return packageCar.plus(standAloneCar).firstOrNull()
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
    val tripProductsList = mutableListOf<TripProducts>()
    addProductIfExists(tripProductsList, productsContainer.hotels, TripProducts.HOTEL)
    addProductIfExists(tripProductsList, productsContainer.flights, TripProducts.FLIGHT)
    addProductIfExists(tripProductsList, productsContainer.cars, TripProducts.CAR)
    addProductIfExists(tripProductsList, productsContainer.activities, TripProducts.ACTIVITY)
    addProductIfExists(tripProductsList, productsContainer.rails, TripProducts.RAIL)
    addProductIfExists(tripProductsList, productsContainer.cruises, TripProducts.CRUISE)
    return tripProductsList
}

private fun addProductIfExists(tripProductsList: MutableList<TripProducts>, lobList: List<Any>?, product: TripProducts) {
    if (lobList != null && lobList.isNotEmpty()) {
        tripProductsList.add(product)
    }
}
