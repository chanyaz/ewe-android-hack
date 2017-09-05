package com.expedia.bookings.data.lx

import com.expedia.bookings.data.BaseSearchParams
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.Strings
import org.joda.time.LocalDate

open class LxSearchParams(val location: String, val activityStartDate: LocalDate, val activityEndDate: LocalDate, val searchType:SearchType, val filters: String, val activityId: String?, val imageCode: String?) : BaseSearchParams(null, null, 0, emptyList(), activityStartDate, activityEndDate) {

    companion object {
        val lxMaxStay: Int = 0;
        val lxMaxRange: Int = 314;
    }

    class Builder() : BaseSearchParams.Builder(LxSearchParams.lxMaxStay, LxSearchParams.lxMaxRange) {
        var location = ""
        var searchType = SearchType.EXPLICIT_SEARCH
        var filters = ""
        var activityId = ""
        var imageCode = ""

        fun imageCode(pkg: String): Builder {
            this.imageCode = pkg
            return this
        }

        fun searchType(searchType: SearchType): Builder {
            this.searchType = searchType
            return this
        }

        fun filters(filters: String): Builder {
            this.filters = filters
            return this
        }

        fun activityId(activityId: String): Builder {
            this.activityId = activityId
            return this
        }

        fun location(locationName: String): Builder {
            this.location = locationName
            return this
        }

        override fun build(): LxSearchParams {
            val location = if (Strings.isNotEmpty(location)) location else destinationLocation?.regionNames?.fullName ?: throw IllegalArgumentException()
            val activityStartDate = startDate ?: throw IllegalArgumentException()
            val activityEndDate = endDate ?: throw IllegalArgumentException()
            val params = LxSearchParams(location, activityStartDate, activityEndDate, searchType, filters, activityId, imageCode)
            return params
        }

        override fun areRequiredParamsFilled(): Boolean {
            return hasDestinationLocation() && hasStartAndEndDates()
        }

        override fun hasOriginAndDestination(): Boolean {
            return hasDestinationLocation() //origin won't be set
        }

        override fun isOriginSameAsDestination(): Boolean {
            return false
        }

    }

    fun toServerStartDate(): String {
        return DateUtils.convertToLXDate(activityStartDate)
    }

    fun toServerEndDate(): String {
        return DateUtils.convertToLXDate(activityEndDate)
    }

}
