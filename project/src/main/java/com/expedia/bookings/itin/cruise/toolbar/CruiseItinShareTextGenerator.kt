package com.expedia.bookings.itin.cruise.toolbar

import com.expedia.bookings.R
import com.expedia.bookings.itin.common.TripProducts
import com.expedia.bookings.itin.tripstore.data.ItinCruise
import com.expedia.bookings.itin.utils.ItinShareTextGenerator
import com.expedia.bookings.itin.utils.StringSource

class CruiseItinShareTextGenerator(private val tripTitle: String, private val itinNumber: String, itinCruise: ItinCruise, private val stringSource: StringSource) : ItinShareTextGenerator {

    private val cruiseLine = itinCruise.cruiseLineName ?: ""
    private val shipName = itinCruise.shipName ?: ""
    private val embarkTime = itinCruise.startTime?.localizedShortTime ?: ""
    private val fullEmbarkDate = itinCruise.startTime?.localizedFullDate ?: ""
    private val shortEmbarkDate = itinCruise.startTime?.localizedShortDate ?: ""
    private val departurePort = itinCruise.departurePort?.portName ?: ""
    private val disembarkTime = itinCruise.endTime?.localizedShortTime ?: ""
    private val fullDisembarkDate = itinCruise.endTime?.localizedFullDate ?: ""
    private val shortDisembarkDate = itinCruise.endTime?.localizedShortDate ?: ""
    private val disembarkPort = itinCruise.disembarkationPort?.portName ?: ""

    override fun getEmailSubject(): String {
        return stringSource.fetch(R.string.itin_cruise_share_email_subject)
    }

    override fun getEmailBody(): String {
        return stringSource.fetchWithPhrase(R.string.itin_cruise_share_email_body_TEMPLATE,
                mapOf("reservation" to tripTitle, "itin_number" to itinNumber, "cruise_line" to cruiseLine,
                        "ship_name" to shipName, "embark_time" to embarkTime, "embark_date" to fullEmbarkDate,
                        "departure_port" to departurePort, "disembark_time" to disembarkTime,
                        "disembark_date" to fullDisembarkDate, "disembark_port" to disembarkPort))
    }

    override fun getSmsBody(): String {
        return stringSource.fetchWithPhrase(R.string.itin_cruise_share_sms_TEMPLATE,
                mapOf("reservation" to tripTitle, "depart_time" to embarkTime, "depart_date" to shortEmbarkDate,
                        "arrive_time" to disembarkTime, "arrive_date" to shortDisembarkDate, "itin_number" to itinNumber,
                        "cruise_line" to cruiseLine, "ship_name" to shipName))
    }

    override fun getLOBTypeString(): String {
        return TripProducts.CRUISE.name.toLowerCase().capitalize()
    }
}
