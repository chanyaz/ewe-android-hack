package com.expedia.bookings.itin.vm

import com.expedia.bookings.data.trips.ItinCardDataFlight
import com.expedia.bookings.utils.JodaUtils
import org.joda.time.DateTime
import org.joda.time.Days


class FlightItinOmnitureUtils {

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
            val tripEndDate = trip.tripEndDate.withTimeAtStartOfDay()
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
        }
        else {
            return "ST"
        }
    }

}