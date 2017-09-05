package com.expedia.bookings.utils

import android.content.Context
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.flights.Airline
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightServiceClassType
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.widget.traveler.FrequentFlyerCard
import com.mobiata.flightlib.utils.DateTimeUtils
import com.squareup.phrase.Phrase
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import java.util.Locale
import java.util.ArrayList

object FlightV2Utils {
    val TICKETS_LEFT_CUTOFF_FOR_DECIDING_URGENCY = 5

    @JvmStatic fun getFlightDurationStopString(context: Context, flight: FlightLeg): String {
        return context.resources.getString(R.string.flight_duration_description_template, getFlightDurationString(context, flight), getFlightStopString(context, flight))
    }

    @JvmStatic fun getFlightSegmentDurationString(context: Context, segment: FlightLeg.FlightSegment): String {
        return getDurationString(context, segment.durationHours, segment.durationMinutes)
    }

    @JvmStatic fun getFlightSegmentLayoverDurationString(context: Context, segment: FlightLeg.FlightSegment): String {
        return getDurationString(context, segment.layoverDurationHours, segment.layoverDurationMinutes)
    }

    @JvmStatic fun getFlightLegDurationContentDescription(context: Context, flightLeg: FlightLeg): String {
        val flightDuration = getDurationContentDesc(context, flightLeg.durationHour, flightLeg.durationMinute)
        return getTotalDurationString(context, flightDuration)
    }

    @JvmStatic fun getFlightLegDurationWithButtonInfoContentDescription(context: Context, flightLeg: FlightLeg): String {
        return Phrase.from(context.resources.getString(R.string.bundle_overview_detailed_button_description_TEMPLATE))
                .put("rowdescription", getFlightLegDurationContentDescription(context, flightLeg))
                .format().toString()
    }

    private fun getTotalDurationString(context: Context, flightDuration: String): String {
        return Phrase.from(context.resources.getString(R.string.package_flight_overview_total_duration_TEMPLATE))
                .put("duration", flightDuration)
                .format().toString()
    }

    @JvmStatic fun getFlightSegmentLayoverDurationContentDescription(context: Context, segment: FlightLeg.FlightSegment): String {
        return getDurationContentDesc(context, segment.layoverDurationHours, segment.layoverDurationMinutes)
    }

    @JvmStatic fun getFlightSegmentDurationContentDescription(context: Context, segment: FlightLeg.FlightSegment): String {
        return getDurationContentDesc(context, segment.durationHours, segment.durationMinutes)
    }

    @JvmStatic fun getStylizedFlightDurationString(context: Context, flight: FlightLeg, colorId: Int): CharSequence {
        val flightDuration = FlightV2Utils.getFlightDurationString(context, flight)
        val totalDuration = getTotalDurationString(context, flightDuration)
        val start = totalDuration.indexOf(flightDuration)
        val end = start + flightDuration.length

        val colorSpan = ForegroundColorSpan(ContextCompat.getColor(context, colorId))
        val totalDurationStyledString = SpannableStringBuilder(totalDuration)
        totalDurationStyledString.setSpan(colorSpan, start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)

        return totalDurationStyledString
    }

    @JvmStatic fun getOperatingAirlineNameString(context: Context, segment: FlightLeg.FlightSegment): String? {
        if (segment.operatingAirlineName != null) {
            if (segment.operatingAirlineCode != null) {
                return Phrase.from(context, R.string.flight_operated_by_carrier_name_and_code_TEMPLATE)
                        .put("operatingairlinename", segment.operatingAirlineName)
                        .put("operatingairlinecode", segment.operatingAirlineCode)
                        .format().toString()
            }
            return Phrase.from(context, R.string.flight_operated_by_carrier_name_only_TEMPLATE)
                    .put("operatingairlinename", segment.operatingAirlineName)
                    .format().toString()
        }
        return null
    }

    @JvmStatic fun getFlightDurationString(context: Context, flight: FlightLeg): String {
        return getDurationString(context, flight.durationHour, flight.durationMinute)
    }

    private fun getDurationString(context: Context, durationHour: Int, durationMinute: Int): String {
        if (durationHour > 0) {
            return context.resources.getString(R.string.flight_hour_min_duration_template, durationHour, durationMinute)
        }
        return context.resources.getString(R.string.flight_min_duration_template, durationMinute)
    }

    private fun getDurationContentDesc(context: Context, durationHour: Int, durationMinute: Int): String {
        if (durationHour > 0) {
            return Phrase.from(context, R.string.flight_hour_min_duration_template_cont_desc)
                    .put("h", durationHour)
                    .put("m", durationMinute)
                    .format().toString()
        }
        return Phrase.from(context, R.string.flight_min_duration_template_cont_desc)
                .put("m", durationMinute)
                .format().toString()
    }

    @JvmStatic fun getFlightStopString(context: Context, flight: FlightLeg): String {
        val numOfStops = flight.stopCount
        if (numOfStops == 0) {
            return context.resources.getString(R.string.flight_nonstop_description)
        } else {
            return context.resources.getQuantityString(R.plurals.x_Stops_TEMPLATE, numOfStops, numOfStops)
        }
    }

    @JvmStatic fun getFlightDepartureArrivalTimeAndDays(context: Context, flight: FlightLeg): String {
        return getFlightDepartureArrivalTimeAndDays(context, flight.departureDateTimeISO, flight.arrivalDateTimeISO, flight.elapsedDays)
    }

    @JvmStatic fun getFlightDepartureArrivalTimeAndDays(context: Context, departureTime: String, arrivalTime: String, elapsedDays: Int): String {

        if (elapsedDays != 0) {
            val displayStringTemplate: Int
            if (elapsedDays < 0) {
                displayStringTemplate = R.string.flight_departure_arrival_time_negative_days_TEMPLATE
            } else {
                displayStringTemplate = R.string.departure_arrival_time_multi_day_TEMPLATE
            }
            return Phrase.from(context, displayStringTemplate)
                    .put("departuretime", formatTimeShort(context, departureTime))
                    .put("arrivaltime", formatTimeShort(context, arrivalTime))
                    .put("elapseddays", Math.abs(elapsedDays)).format().toString()
        }
        return getFlightDepartureArrivalTime(context, formatTimeShort(context, departureTime), formatTimeShort(context, arrivalTime))
    }

    @JvmStatic fun getAccessibleDepartArrivalTime(context: Context, flight: FlightLeg): String {
        val departureTime = formatTimeShort(context, flight.departureDateTimeISO)
        val arrivalTime = formatTimeShort(context, flight.arrivalDateTimeISO)
        val elapsedDays = flight.elapsedDays
        if (elapsedDays != 0) {
            val displayStringTemplate: Int
            if (elapsedDays < 0) {
                displayStringTemplate = R.string.flight_departure_arrival_time_negative_days_cont_desc_TEMPLATE
            } else {
                displayStringTemplate = R.string.flight_departure_arrival_time_multi_day_cont_desc_TEMPLATE
            }
            return Phrase.from(context, displayStringTemplate)
                    .put("departuretime", departureTime)
                    .put("arrivaltime", arrivalTime)
                    .put("elapseddays", Math.abs(elapsedDays)).format().toString()
        }

        return Phrase.from(context, R.string.flight_departure_arrival_time_cont_desc_TEMPLATE)
                .put("departuretime", departureTime)
                .put("arrivaltime", arrivalTime).format().toString()
    }

    @JvmStatic fun getFlightDepartureArrivalTime(context: Context, departureTime: String, arrivalTime: String): String {
        return context.resources.getString(R.string.flight_departure_arrival_time_template, departureTime, arrivalTime)
    }

    @JvmStatic fun getFlightDepartureArrivalCityAirport(context: Context, flightSegment: FlightLeg.FlightSegment): String {
        return getFlightDepartureArrivalCityAirportString(context, flightSegment, R.string.package_flight_overview_departure_arrival_TEMPLATE)
    }

    @JvmStatic fun getFlightDepartureArrivalCityAirportContDesc(context: Context, flightSegment: FlightLeg.FlightSegment): String {
        return getFlightDepartureArrivalCityAirportString(context, flightSegment, R.string.package_flight_overview_departure_arrival_cont_desc_TEMPLATE)
    }

    @JvmStatic fun getFlightDepartureArrivalCityAirportString(context: Context, flightSegment: FlightLeg.FlightSegment, stringResID: Int): String {
        return Phrase.from(context.resources.getString(stringResID))
                .put("departurecity", flightSegment.departureCity)
                .put("departureairportcode", flightSegment.departureAirportCode)
                .put("arrivalcity", flightSegment.arrivalCity)
                .put("arrivalairportcode", flightSegment.arrivalAirportCode)
                .format().toString()
    }

    @JvmStatic fun getFlightAirlineAndAirplaneType(context: Context, flightSegment: FlightLeg.FlightSegment): String {
        return Phrase.from(context.resources.getString(R.string.package_flight_overview_airline_airplane_TEMPLATE))
                .put("carrier", flightSegment.carrier)
                .put("flightnumber", flightSegment.flightNumber)
                .put("airplanetype", Strings.capitalize(flightSegment.airplaneType, Locale.US))
                .format().toString()
    }

    @JvmStatic fun getDistinctiveAirline(airlines: List<Airline>): List<Airline> {
        if (airlines.all { it.airlineName == airlines[0].airlineName }) {
            return airlines.subList(0, 1)
        }
        return airlines
    }

    fun getAirlineNames(flightLegs: List<FlightLeg>): List<FrequentFlyerCard> {
        val frequentFlyerCards = ArrayList<FrequentFlyerCard>()
        val seenAirlines = ArrayList<String>()
        flightLegs.forEach {
            it.segments.forEach {
                if (!seenAirlines.contains(it.airlineName)) {
                    frequentFlyerCards.add(FrequentFlyerCard(it.airlineName, it.airlineCode))
                    seenAirlines.add(it.airlineName)
                }
            }
        }
        return frequentFlyerCards
    }

    @JvmStatic fun isFlightMerchant(flightLeg: FlightLeg?): Boolean {
        // https://confluence/display/Omniture/Products+String+and+Events#ProductsStringandEvents-Flights
        when (flightLeg?.flightFareTypeString?.toUpperCase()) {
            "M", "SN", "N", "WP", "WPNS", "W", "SM" -> return true
            "C", "L", "CN", "PP", "P" -> return false
        }
        return false
    }

    @JvmStatic fun getAirlinesList(airlines: List<Airline>): String {
        val airlineList = StringBuilder()
        for (i in 0..airlines.size - 1) {
            airlineList.append(airlines[i].airlineName)
        }
        return airlineList.toString()
    }

    @JvmStatic fun formatTimeShort(context: Context, timeStr: String): String {
        if (timeStr.isEmpty()) return ""
        val fmt = ISODateTimeFormat.dateTime().withOffsetParsed()
        val time = DateTime.parse(timeStr, fmt)
        val dateFormat = DateTimeUtils.getDeviceTimeFormat(context)
        return JodaUtils.format(time, dateFormat).toLowerCase(Locale.getDefault())
    }

    @JvmStatic fun getSeatsLeftUrgencyMessage(context: Context, flightLeg: FlightLeg): String {
        if (flightLeg.packageOfferModel.urgencyMessage != null) {
            val seatsLeft = flightLeg.packageOfferModel.urgencyMessage.ticketsLeft
            if (seatsLeft in 1..TICKETS_LEFT_CUTOFF_FOR_DECIDING_URGENCY)
                return Phrase.from(context.resources.getQuantityString(R.plurals.flight_seats_left_urgency_message_TEMPLATE, seatsLeft))
                        .put("seats", seatsLeft)
                        .format().toString()
            else
                return ""
        } else
            return ""
    }

    @JvmStatic fun getFlightCabinPreferences(context: Context, flightLeg: FlightLeg): String {
        if (flightLeg.isBasicEconomy) {
            return context.resources.getString(R.string.cabin_code_basic_economy)
        }
        else if (flightLeg.packageOfferModel?.segmentsSeatClassAndBookingCode != null &&
                flightLeg.packageOfferModel.segmentsSeatClassAndBookingCode.size > 0) {
            var flightCabinPreferences = ""
            val seatClassAndBookingCodeList = flightLeg.packageOfferModel.segmentsSeatClassAndBookingCode
            if (seatClassAndBookingCodeList.size == 1) {
                flightCabinPreferences = context.resources.getString(FlightServiceClassType.getCabinCodeResourceId(seatClassAndBookingCodeList[0].seatClass))
            } else if (seatClassAndBookingCodeList.size == 2) {
                flightCabinPreferences = Phrase.from(context, R.string.flight_cabin_class_for_two_segment_TEMPLATE)
                        .put("cabin_class_one", context.resources.getString(FlightServiceClassType.getCabinCodeResourceId(seatClassAndBookingCodeList[0].seatClass)))
                        .put("cabin_class_second", context.resources.getString(FlightServiceClassType.getCabinCodeResourceId(seatClassAndBookingCodeList[1].seatClass)))
                        .format()
                        .toString()
            } else if (isAllFlightCabinPreferencesSame(seatClassAndBookingCodeList)) {
                flightCabinPreferences = context.resources.getString(FlightServiceClassType.getCabinCodeResourceId(seatClassAndBookingCodeList[0].seatClass))
            } else {
                flightCabinPreferences = context.resources.getString(R.string.flight_cabin_mixed_classes)
            }

            return flightCabinPreferences
        } else {
            return ""
        }
    }

    @JvmStatic fun getAirlineUrl(flightLeg: FlightLeg) : String? {
            return when {
                flightLeg.airlines.size == 1 -> flightLeg.airlines.first().airlineLogoUrl
                flightLeg.airlines.size > 1 && getDistinctiveAirline(flightLeg.airlines).size == 1 ->
                    getDistinctiveAirline(flightLeg.airlines).first().airlineLogoUrl
                else -> null
            }
        }

    @JvmStatic fun getDepartureOnDateString(context: Context, flightLeg: FlightLeg) : String {
        val date = LocaleBasedDateFormatUtils.localDateToMMMd(DateTime.parse(flightLeg.segments.first().departureTimeRaw).toLocalDate())
        return " " + Phrase.from(context.getString(R.string.flight_confirmation_crystal_title_on_date_TEMPLATE))
                .put("date", date)
                .format().toString()
    }

    @JvmStatic fun getAdvanceSearchFilterHeaderString(context: Context, isNonStopFilterSelected: Boolean, isRefundableFilterSelected: Boolean, priceHeaderText: String): CharSequence? {
        val headerText = StringBuilder()
        if (isNonStopFilterSelected && isRefundableFilterSelected) {
            headerText.append(context.getString(R.string.flight_nonstop_refundable_search_header))
        } else if (isNonStopFilterSelected) {
            headerText.append(context.getString(R.string.flight_nonstop_search_header))
        } else if (isRefundableFilterSelected) {
            headerText.append(context.getString(R.string.flight_refundable_search_header))
        }
        if (headerText.isNotEmpty())
            return  HtmlCompat.fromHtml(headerText.append(priceHeaderText).toString())
        else return null
    }

    @JvmStatic fun getSelectedClassesString(context: Context, flightTripDetails: FlightTripDetails): String {
        var selectedSeatClassList: MutableList<String> = ArrayList()
        val basicEconomyAvailableAndCorrespondingLeg: Pair<Boolean, Int> = getBasicEconomyLeg(flightTripDetails.legs)

        flightTripDetails.offer.offersSeatClassAndBookingCode.forEachIndexed { index, seatClass ->
            if (flightTripDetails.legs[index].isBasicEconomy) {
                selectedSeatClassList.add(context.resources.getString(R.string.cabin_code_basic_economy))
            } else {
                selectedSeatClassList.addAll(seatClass.map { seatClass -> context.getString(FlightServiceClassType.getCabinCodeResourceId(seatClass.seatClass)) })
            }
        }
        selectedSeatClassList = selectedSeatClassList.distinct().toMutableList()

        var selectedClassText = ""
        if (selectedSeatClassList.size == 1) {
            selectedClassText = selectedSeatClassList[0]
        } else if (selectedSeatClassList.size == 2) {
            selectedClassText = Phrase.from(context.getString(R.string.flight_selected_classes_two_class_TEMPLATE))
                    .put("class_one", selectedSeatClassList[0]).put("class_two", selectedSeatClassList[1])
                    .format().toString()
        } else if (basicEconomyAvailableAndCorrespondingLeg.first) {
            if (basicEconomyAvailableAndCorrespondingLeg.second == 0) {
                selectedClassText = Phrase.from(context.getString(R.string.flight_selected_classes_two_class_TEMPLATE))
                        .put("class_one", context.resources.getString(R.string.cabin_code_basic_economy))
                        .put("class_two", context.resources.getString(R.string.flight_cabin_mixed_classes))
                        .format().toString()
            } else {
                selectedClassText = Phrase.from(context.getString(R.string.flight_selected_classes_two_class_TEMPLATE))
                        .put("class_one", context.resources.getString(R.string.flight_cabin_mixed_classes))
                        .put("class_two", context.resources.getString(R.string.cabin_code_basic_economy))
                        .format().toString()
            }
        } else {
            selectedClassText = context.getString(R.string.flight_cabin_mixed_classes)
        }
        return selectedClassText
    }

    private fun getBasicEconomyLeg(flightLegs: List<FlightLeg>): Pair<Boolean, Int> {
        flightLegs.forEachIndexed { index, flightLeg ->
            if (flightLeg.isBasicEconomy) {
                return Pair(true, index)
            }
        }
        return Pair(false, -1)
    }

    private fun isAllFlightCabinPreferencesSame(seatClassAndBookingCodeList: List<FlightTripDetails.SeatClassAndBookingCode>): Boolean {
        val previousCabinVal = seatClassAndBookingCodeList[0].seatClass
        for (seatClassAndBookingCode in seatClassAndBookingCodeList) {
            val cabinVal = seatClassAndBookingCode.seatClass
            if (cabinVal != previousCabinVal) {
                return false
            }
        }
        return true
    }

    fun getDeltaPricing (money: Money, deltaPositive: Boolean): String {
        val deltaPrice = StringBuilder()
        if (deltaPositive) {
            deltaPrice.append("+")
        }
        deltaPrice.append(Money.getFormattedMoneyFromAmountAndCurrencyCode(money.amount, money.currencyCode))
        return deltaPrice.toString()

    }
}
