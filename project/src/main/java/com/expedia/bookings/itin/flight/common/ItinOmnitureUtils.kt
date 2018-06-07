package com.expedia.bookings.itin.flight.common

import com.expedia.bookings.data.trips.ItinCardDataFlight
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.extensions.firstHotel
import com.expedia.bookings.itin.tripstore.extensions.firstLx
import com.expedia.bookings.itin.tripstore.extensions.tripEndDate
import com.expedia.bookings.itin.tripstore.extensions.tripStartDate
import com.expedia.bookings.utils.JodaUtils
import org.joda.time.DateTime
import org.joda.time.Days

object ItinOmnitureUtils {

    enum class LOB {
        HOTEL,
        LX,
    }

    //These methods are for Flights using ItinCardData
    @JvmStatic
    fun createOmnitureTrackingValues(itinCardData: ItinCardDataFlight): HashMap<String, String?> {
        val duration = calculateTripDuration(itinCardData)
        val daysUntilTrip = calculateDaysUntilTripStart(itinCardData)
        val orderAndTripNumbers = buildOrderNumberAndItinNumberString(itinCardData)
        val tripStartDate = JodaUtils.format(itinCardData.tripStartDate, "yyyy-MM-dd")
        val tripEndDate = JodaUtils.format(itinCardData.tripEndDate, "yyyy-MM-dd")
        val productString = buildFlightProductString(itinCardData)
        val valueMap = HashMap<String, String?>()
        valueMap.put("duration", duration)
        valueMap.put("daysUntilTrip", daysUntilTrip)
        valueMap.put("orderAndTripNumbers", orderAndTripNumbers)
        valueMap.put("tripStartDate", tripStartDate)
        valueMap.put("tripEndDate", tripEndDate)
        valueMap.put("productString", productString)

        return valueMap
    }

    fun calculateTripDuration(trip: ItinCardDataFlight): String? {
        if (trip.tripStartDate != null && trip.tripEndDate != null) {
            val tripStartDate = trip.tripStartDate.withTimeAtStartOfDay()
            val tripEndDate = trip.tripEndDate.withTimeAtStartOfDay().plusHours(2)
            if (tripStartDate == tripEndDate) {
                return "0.0"
            } else {
                return (Days.daysBetween(tripStartDate, tripEndDate).days + 1).toString()
            }
        } else {
            return null
        }
    }

    fun calculateDaysUntilTripStart(trip: ItinCardDataFlight): String? {
        val now = DateTime.now()
        if (trip.tripStartDate != null) {
            val tripStartDate = trip.tripStartDate.withTimeAtStartOfDay()
            var daysUntil = Days.daysBetween(now, tripStartDate).days
            if (daysUntil >= 1) {
                daysUntil += 1 //this accounts for today
                return daysUntil.toString()
            } else {
                return "0.0"
            }
        } else {
            return null
        }
    }

    fun buildOrderNumberAndItinNumberString(trip: ItinCardDataFlight): String {
        val travelRecordLocator = trip.orderNumber ?: "NA"
        val itinNumber = trip.tripNumber ?: "NA"
        val orderItinstring = StringBuilder()
        orderItinstring.append(travelRecordLocator)
                .append("|")
                .append(itinNumber)

        return orderItinstring.toString()
    }

    fun buildFlightProductString(trip: ItinCardDataFlight): String {
        val tripType = getTripType(trip)
        val airlineCode = trip.flightLeg.firstAirlineCode ?: ""
        val productString = StringBuilder()
        productString.append(";Flight:")
                .append(airlineCode)
                .append(":")
                .append(tripType)
                .append(";;")
        return productString.toString()
    }

    fun getTripType(trip: ItinCardDataFlight): String {
        val numLegs = trip.legCount
        if (!trip.isSplitTicket) {
            when (numLegs) {
                1 -> return "OW"
                2 -> return "RT"
                else -> return "MD"
            }
        } else {
            return "ST"
        }
    }

    //These methods are for other LOBs using new objects rather than ItinCardData

    @JvmStatic
    fun createOmnitureTrackingValuesNew(trip: Itin, lob: LOB): HashMap<String, String?> {
        val duration = calculateTripDurationNew(trip, lob)
        val daysUntilTrip = calculateDaysUntilTripStartNew(trip)
        val orderAndTripNumbers = buildOrderNumberAndItinNumberStringNew(trip)
        val tripStartDate = JodaUtils.format(trip.tripStartDate(), "yyyy-MM-dd")
        val tripEndDate = JodaUtils.format(trip.tripEndDate(), "yyyy-MM-dd")
        val productString = buildLOBProductString(trip, lob)
        val valueMap = HashMap<String, String?>()
        valueMap.put("duration", duration)
        valueMap.put("daysUntilTrip", daysUntilTrip)
        valueMap.put("orderAndTripNumbers", orderAndTripNumbers)
        valueMap.put("tripStartDate", tripStartDate)
        valueMap.put("tripEndDate", tripEndDate)
        valueMap.put("productString", productString)

        return valueMap
    }

    fun calculateTripDurationNew(trip: Itin, lob: LOB): String? {
        var duration = ""
        if (lob == LOB.HOTEL) {
            trip.firstHotel()?.let { hotel ->
                duration = hotel.numberOfNights!!
            }
        } else {
            val tripStartDate = trip.tripStartDate()
            val tripEndDate = trip.tripEndDate()
            if (tripStartDate != null && tripEndDate != null) {
                val tripFirstDay = tripStartDate.withTimeAtStartOfDay()
                val tripLastDay = tripEndDate.withTimeAtStartOfDay().plusHours(2)
                if (tripFirstDay == tripLastDay) {
                    duration = "0.0"
                } else {
                    duration = (Days.daysBetween(tripFirstDay, tripLastDay).days + 1).toString()
                }
            }
        }
        return duration
    }

    fun calculateDaysUntilTripStartNew(trip: Itin): String? {
        val now = DateTime.now()
        val startDate = trip.tripStartDate()
        if (startDate != null) {
            val tripStartDate = startDate.withTimeAtStartOfDay()
            var daysUntil = Days.daysBetween(now, tripStartDate).days
            if (daysUntil >= 1) {
                daysUntil += 1 //this accounts for today
                return daysUntil.toString()
            } else {
                return "0.0"
            }
        } else {
            return null
        }
    }

    fun buildOrderNumberAndItinNumberStringNew(trip: Itin): String {
        val travelRecordLocator = trip.orderNumber ?: "NA"
        val itinNumber = trip.tripNumber ?: "NA"
        val orderItinstring = StringBuilder()
        orderItinstring.append(travelRecordLocator)
                .append("|")
                .append(itinNumber)

        return orderItinstring.toString()
    }

    fun buildLOBProductString(trip: Itin, lob: LOB): String {
        var productLOBType = ""
        var productId = ""
        var numberOfUnits = ""
        var totalPrice = ""
        when (lob) {
            LOB.HOTEL ->
                trip.firstHotel()?.let { hotel ->
                    productLOBType = ";Hotel:"
                    productId = hotel.hotelId ?: ""
                    numberOfUnits = hotel.numberOfNights ?: ""
                    totalPrice = hotel.totalPriceDetails?.base ?: ""
                }
            LOB.LX ->
                trip.firstLx()?.let { lx ->
                    productLOBType = ";LX:"
                    productId = lx.activityId ?: ""
                    numberOfUnits = lx.travelerCount ?: ""
                    totalPrice = lx.price?.base ?: ""
                }
        }
        val productString = StringBuilder()
        productString.append(productLOBType)
                .append(productId)
                .append(";")
                .append(numberOfUnits)
                .append(";")
                .append(totalPrice)
        return productString.toString()
    }
}
