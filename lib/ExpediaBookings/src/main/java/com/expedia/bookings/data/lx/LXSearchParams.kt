package com.expedia.bookings.data.lx

import com.expedia.bookings.data.BaseSearchParams
import com.expedia.bookings.utils.ApiDateUtils
import com.expedia.bookings.utils.Strings
import org.joda.time.LocalDate

open class LxSearchParams(val location: String, val activityStartDate: LocalDate, val activityEndDate: LocalDate, val searchType: SearchType, val filters: String, val activityId: String?, val imageCode: String?, val modQualified: Boolean) : BaseSearchParams(null, null, 0, emptyList(), activityStartDate, activityEndDate) {

    companion object {
        val lxMaxStay: Int = 0
        val lxMaxRange: Int = 314
    }

    class Builder() : BaseSearchParams.Builder(LxSearchParams.lxMaxStay, LxSearchParams.lxMaxRange) {
        var location = ""
        var searchType = SearchType.EXPLICIT_SEARCH
        var filters = ""
        var activityId = ""
        var imageCode = ""
        var modQualified = false

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

        fun modQualified(modQualified: Boolean): Builder {
            this.modQualified = modQualified
            return this
        }

        override fun build(): LxSearchParams {
            val location = if (Strings.isNotEmpty(location)) location else destinationLocation?.regionNames?.fullName ?: throw IllegalArgumentException()
            val activityStartDate = startDate ?: throw IllegalArgumentException()
            val activityEndDate = endDate ?: throw IllegalArgumentException()
            val params = LxSearchParams(location, activityStartDate, activityEndDate, searchType, filters, activityId, imageCode, modQualified)
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
        return ApiDateUtils.localDateToyyyyMMdd(activityStartDate)
    }

    fun toServerEndDate(): String {
        return ApiDateUtils.localDateToyyyyMMdd(activityEndDate)
    }
}
