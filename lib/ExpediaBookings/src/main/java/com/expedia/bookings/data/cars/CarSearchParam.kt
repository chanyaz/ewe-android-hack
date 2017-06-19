package com.expedia.bookings.data.cars

import com.expedia.bookings.data.BaseSearchParams
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.SuggestionStrUtils
import com.expedia.bookings.utils.Strings
import org.joda.time.DateTime

open class CarSearchParam(val originLocation: String?, val startDateTime: DateTime, val endDateTime: DateTime, val pickupLocationLatLng: LatLong? = null, val originDescription: String = "") :
        BaseSearchParams(origin = null, destination = null, adults = 0, children = emptyList(), startDate = startDateTime.toLocalDate(), endDate = endDateTime.toLocalDate()) {

    companion object {
        val carMaxStay: Int = 330;
        val carMaxRange: Int = 330;
    }

    class Builder() : BaseSearchParams.Builder(carMaxStay, carMaxRange) {
        var origin: String? = null
        var startDateTime : DateTime? = null
        var endDateTime : DateTime? =null
        var pickupLocationLatLng: LatLong? = null
        var originDescription = ""
        var startDateTimeAsMillis = 0
        var endDateTimeAsMillis = 0

        fun startDateTime(startDateTime: DateTime): Builder {
            this.startDateTime = startDateTime
            return this
        }

        fun endDateTime(endDateTime: DateTime): Builder {
            this.endDateTime = endDateTime
            return this
        }

        fun pickupLocationLatLng(pickupLocationLatLng: LatLong?): Builder {
            this.pickupLocationLatLng = pickupLocationLatLng
            return this
        }


        fun startDateTimeAsMillis(millis: Int): Builder {
            startDateTimeAsMillis = millis
            return this
        }

        fun endDateTimeAsMillis(millis: Int): Builder {
            endDateTimeAsMillis = millis
            return this
        }

        override fun build(): CarSearchParam {
            val startDateTime = if (startDateTime != null) startDateTime else DateUtils.localDateAndMillisToDateTime(startDate,
                    startDateTimeAsMillis)
            val endDateTime = if (endDateTime != null) endDateTime else DateUtils.localDateAndMillisToDateTime(endDate,
                    endDateTimeAsMillis)

            //Do not build the params if the input selected by user is null
            val location = originLocation ?: throw IllegalArgumentException("Incomplete params: origin is null")
            if (!location.isMajorAirport && location.coordinates.lat != 0.0 && location.coordinates.lng != 0.0) {
                origin = null
                pickupLocationLatLng = LatLong(location.coordinates.lat, location.coordinates.lng);
                originDescription = SuggestionStrUtils.formatCityName(location.regionNames.fullName)
            } else {
                pickupLocationLatLng = null
                origin = location.hierarchyInfo!!.airport!!.airportCode
                originDescription = SuggestionStrUtils.formatAirport(location)
            }

            //Input Validation
            //1. One of `origin` and `pickupLocationLatLng` should exist for Car Search Params to be valid
            if (Strings.isEmpty(origin) && pickupLocationLatLng == null) {
                throw IllegalStateException("Incomplete params: Origin and pickupLocationLatLong both cannot be null")
            }

            var params = CarSearchParam(origin, startDateTime!!, endDateTime!!, pickupLocationLatLng, originDescription)
            return params
        }

        override fun areRequiredParamsFilled(): Boolean {
            return hasOriginLocation() && hasStartAndEndDates()
        }

        override fun isOriginSameAsDestination(): Boolean {
            return true
        }
    }

    fun toServerPickupDate(): String {
        return DateUtils.carSearchFormatFromDateTime(startDateTime)
    }

    fun toServerDropOffDate(): String {
        return DateUtils.carSearchFormatFromDateTime(endDateTime)
    }

    fun shouldSearchByLocationLatLng(): Boolean {
        return pickupLocationLatLng != null
    }
}

