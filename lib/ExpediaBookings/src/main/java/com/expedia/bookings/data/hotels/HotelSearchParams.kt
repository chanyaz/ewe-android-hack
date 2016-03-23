package com.expedia.bookings.data.hotels

import com.expedia.bookings.data.BaseSearchParams
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.packages.PackageSearchParams
import org.joda.time.Days
import org.joda.time.LocalDate

open class HotelSearchParams(val suggestion: SuggestionV4, val checkIn: LocalDate, val checkOut: LocalDate, adults: Int, children: List<Int>, val shopWithPoints: Boolean) : BaseSearchParams(adults, children) {
    var forPackage = false

    class Builder(maxStay: Int) : BaseSearchParams.Builder(maxStay) {
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
            val location = departure ?: throw IllegalArgumentException()
            if (departure?.gaiaId == null && departure?.coordinates == null) throw IllegalArgumentException()
            val checkInDate = checkIn ?: throw IllegalArgumentException()
            val checkOutDate = checkOut ?: throw IllegalArgumentException()
            var params = HotelSearchParams(location, checkInDate, checkOutDate, adults, children, shopWithPoints)
            params.forPackage = isPackage
            return params
        }

        override fun areRequiredParamsFilled(): Boolean {
            return hasDeparture() && hasStartAndEndDates()
        }

    }
}

fun convertPackageToSearchParams(packageParams: PackageSearchParams, maxStay: Int): HotelSearchParams {
    val builder = HotelSearchParams.Builder(maxStay).departure(packageParams.destination)
            .checkIn(packageParams.checkIn).checkOut(packageParams.checkOut).adults(packageParams.adults)
            .children(packageParams.children) as HotelSearchParams.Builder
    return builder.forPackage(true).build()
}
