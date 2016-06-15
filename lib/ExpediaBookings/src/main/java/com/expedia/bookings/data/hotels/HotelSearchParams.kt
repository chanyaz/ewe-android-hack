package com.expedia.bookings.data.hotels

import com.expedia.bookings.data.BaseSearchParams
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.packages.PackageSearchParams
import org.joda.time.LocalDate

open class HotelSearchParams(val suggestion: SuggestionV4, val checkIn: LocalDate, val checkOut: LocalDate, adults: Int, children: List<Int>, val shopWithPoints: Boolean, val filterUnavailable: Boolean) : BaseSearchParams(adults, children, checkIn, checkOut) {
    var forPackage = false

    class Builder(maxStay: Int, maxRange: Int, val filterUnavailable: Boolean = true) : BaseSearchParams.Builder(maxStay, maxRange) {
        private var isPackage: Boolean = false
        private var shopWithPoints: Boolean = false

        fun forPackage(pkg: Boolean): Builder {
            this.isPackage = pkg
            return this
        }

        fun shopWithPoints(shopWithPoints: Boolean): Builder {
            this.shopWithPoints = shopWithPoints
            return this
        }

        override fun build(): HotelSearchParams {
            val location = destinationLocation ?: throw IllegalArgumentException()
            if (destinationLocation?.gaiaId == null && destinationLocation?.coordinates == null) throw IllegalArgumentException()
            val checkInDate = startDate ?: throw IllegalArgumentException()
            val checkOutDate = endDate ?: throw IllegalArgumentException()
            var params = HotelSearchParams(location, checkInDate, checkOutDate, adults, children, shopWithPoints, filterUnavailable)
            params.forPackage = isPackage
            return params
        }

        override fun areRequiredParamsFilled(): Boolean {
            return hasDestinationLocation() && hasStartAndEndDates()
        }

        override fun hasOriginAndDestination(): Boolean {
            return hasDestinationLocation() //origin won't be set
        }

        override fun isOriginSameAsDestination(): Boolean {
            return false // not possible for hotel search
        }
    }
}

fun convertPackageToSearchParams(packageParams: PackageSearchParams, maxStay: Int, maxRange: Int): HotelSearchParams {
    val builder = HotelSearchParams.Builder(maxStay, maxRange).destination(packageParams.destination)
            .startDate(packageParams.checkIn).endDate(packageParams.checkOut).adults(packageParams.adults)
            .children(packageParams.children) as HotelSearchParams.Builder
    return builder.forPackage(true).build()
}
