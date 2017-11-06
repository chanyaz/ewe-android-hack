package com.expedia.bookings.utils

import android.content.Context
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import com.expedia.bookings.R
import com.expedia.bookings.data.FlightItinDetailsResponse
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.flights.Airline
import com.expedia.bookings.data.flights.AmenityResourceType
import com.expedia.bookings.data.flights.FlightAmenityCategory
import com.expedia.bookings.data.flights.FlightBagAmenity
import com.expedia.bookings.data.flights.FlightCarryOnBagAmenity
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightSeatReservationAmenity
import com.expedia.bookings.data.flights.FlightServiceClassType
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.data.flights.FrequentFlyerCard
import com.expedia.bookings.text.HtmlCompat
import com.mobiata.flightlib.utils.DateTimeUtils
import com.squareup.phrase.Phrase
import org.joda.time.DateTime
import java.util.ArrayList
import java.util.HashMap
import java.util.Locale

object FlightV2Utils {
    val TICKETS_LEFT_CUTOFF_FOR_DECIDING_URGENCY = 5

    @JvmStatic fun getFlightDurationStopString(context: Context, flight: FlightLeg): String {
        return context.resources.getString(R.string.flight_duration_description_template, getFlightDurationString(context, flight), getStopsStringFromCheckoutResponseLeg(context, flight))
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

    @JvmStatic fun getStopsStringFromCheckoutResponseLeg(context: Context, flight: FlightLeg): String {
        val numOfStops = flight.stopCount
        if (numOfStops == 0) {
            return context.resources.getString(R.string.flight_nonstop_description)
        } else {
            return context.resources.getQuantityString(R.plurals.x_Stops_TEMPLATE, numOfStops, numOfStops)
        }
    }

    @JvmStatic fun getStopsStringFromCheckoutItinResponseLeg(context: Context, flightLeg: FlightItinDetailsResponse.Flight.Leg?): String {
        val numOfStops = flightLeg?.numberOfStops ?: 0
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
        val time = DateTime.parse(timeStr)
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
        } else if (flightLeg.packageOfferModel?.segmentsSeatClassAndBookingCode != null &&
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

    @JvmStatic fun getAirlineUrlFromCheckoutResponseLeg(flightLeg: FlightLeg): String? {
        return when {
            flightLeg.airlines.size == 1 -> flightLeg.airlines.first().airlineLogoUrl
            flightLeg.airlines.size > 1 && getDistinctiveAirline(flightLeg.airlines).size == 1 ->
                getDistinctiveAirline(flightLeg.airlines).first().airlineLogoUrl
            else -> null
        }
    }

    @JvmStatic fun getDepartureOnDateStringFromCheckoutResponseLeg(context: Context, flightLeg: FlightLeg): String {
        val date = LocaleBasedDateFormatUtils.localDateToMMMd(DateTime.parse(flightLeg.segments.first().departureTimeRaw).toLocalDate())
        return " " + Phrase.from(context.getString(R.string.flight_confirmation_crystal_title_on_date_TEMPLATE))
                .put("date", date)
                .format().toString()
    }

    @JvmStatic fun getDepartureOnDateStringFromItinResponseLeg(context: Context, flightLeg: FlightItinDetailsResponse.Flight.Leg?): String {
        val date = LocaleBasedDateFormatUtils.localDateToMMMd(DateTime.parse(flightLeg?.segments?.first()?.departureTime?.raw).toLocalDate())
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
            return HtmlCompat.fromHtml(headerText.append(priceHeaderText).toString())
        else return null
    }

    @JvmStatic fun getSelectedClassesString(context: Context, flightTripDetails: FlightTripDetails, isContDesc: Boolean): String {
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
        var resId = 0

        if (selectedSeatClassList.size == 1) {
            selectedClassText = if (isContDesc) selectedSeatClassList[0] else Phrase.from(context.getString(R.string.flight_selected_classes_one_class_TEMPLATE))
                    .put("class", selectedSeatClassList[0])
                    .format().toString()
        } else if (selectedSeatClassList.size == 2) {
            resId = if (isContDesc) R.string.flight_selected_classes_two_class_cont_desc_TEMPLATE else R.string.flight_selected_classes_two_class_TEMPLATE
            selectedClassText = Phrase.from(context.getString(resId))
                    .put("class_one", selectedSeatClassList[0]).put("class_two", selectedSeatClassList[1])
                    .format().toString()
        } else if (basicEconomyAvailableAndCorrespondingLeg.first) {
            resId = if (isContDesc) R.string.flight_selected_classes_two_class_cont_desc_TEMPLATE else R.string.flight_selected_classes_two_class_TEMPLATE
            if (basicEconomyAvailableAndCorrespondingLeg.second == 0) {
                selectedClassText = Phrase.from(context.getString(resId))
                        .put("class_one", context.resources.getString(R.string.cabin_code_basic_economy))
                        .put("class_two", context.resources.getString(R.string.flight_cabin_mixed_classes))
                        .format().toString()
            } else {
                resId = if (isContDesc) R.string.flight_selected_classes_two_class_cont_desc_TEMPLATE else R.string.flight_selected_classes_two_class_TEMPLATE
                selectedClassText = Phrase.from(context.getString(resId))
                        .put("class_one", context.resources.getString(R.string.flight_cabin_mixed_classes))
                        .put("class_two", context.resources.getString(R.string.cabin_code_basic_economy))
                        .format().toString()
            }
        } else {
            selectedClassText = context.getString(if (isContDesc) R.string.flight_cabin_mixed_classes else R.string.flight_selected_classes_mixed_classes)
        }

        return selectedClassText
    }

    @JvmStatic fun getDepartureToArrivalTitleFromCheckoutResponseLeg(context: Context, flightLeg: FlightLeg) : String {
        val departureAirportCode = flightLeg.segments.first().departureAirportCode
        val arrivalAirportCode = flightLeg.segments.last().arrivalAirportCode
        return context.getString(R.string.SharedItin_Title_Flight_TEMPLATE, departureAirportCode, arrivalAirportCode)
    }

    @JvmStatic fun getDepartureToArrivalSubtitleFromCheckoutResponseLeg(context: Context, flightLeg: FlightLeg): String {
        val departureDateTime = flightLeg.segments.first().departureTimeRaw
        val departureTime = FlightV2Utils.formatTimeShort(context, departureDateTime ?: "")
        val arrivalTime = FlightV2Utils.formatTimeShort(context, flightLeg.segments.last().arrivalTimeRaw ?: "")
        val stops = FlightV2Utils.getStopsStringFromCheckoutResponseLeg(context, flightLeg)

        return Phrase.from(context.getString(R.string.flight_to_card_crystal_subtitle_TEMPLATE))
                .put("departuretime", departureTime)
                .put("arrivaltime", arrivalTime)
                .put("stops", stops)
                .format().toString()
    }

    @JvmStatic
    fun getDepartureToArrivalTitleFromItinResponseLeg(context: Context, flightLeg: FlightItinDetailsResponse.Flight.Leg?) : String {
        val departureAirportCode = flightLeg?.segments?.first()?.departureLocation?.airportCode ?: ""
        val arrivalAirportCode = flightLeg?.segments?.last()?.arrivalLocation?.airportCode ?: ""
        return context.getString(R.string.SharedItin_Title_Flight_TEMPLATE, departureAirportCode, arrivalAirportCode)
    }

    @JvmStatic
    fun getDepartureToArrivalSubtitleFromItinResponseLeg(context: Context, flightLeg: FlightItinDetailsResponse.Flight.Leg?): String {
        val departureTime = FlightV2Utils.formatTimeShort(context, flightLeg?.segments?.first()?.departureTime?.raw ?: "")
        val arrivalTime = FlightV2Utils.formatTimeShort(context, flightLeg?.segments?.last()?.arrivalTime?.raw ?: "")
        val stops = FlightV2Utils.getStopsStringFromCheckoutItinResponseLeg(context, flightLeg)

        return Phrase.from(context.getString(R.string.flight_to_card_crystal_subtitle_TEMPLATE))
                .put("departuretime", departureTime)
                .put("arrivaltime", arrivalTime)
                .put("stops", stops)
                .format().toString()
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

    @JvmStatic fun getDeltaPricing(money: Money, deltaPositive: Boolean): String {
        val deltaPrice = StringBuilder()
        if (deltaPositive) {
            deltaPrice.append("+")
        }
        deltaPrice.append(Money.getFormattedMoneyFromAmountAndCurrencyCode(money.amount, money.currencyCode))
        return deltaPrice.toString()

    }

    @JvmStatic fun hasMoreAmenities(fareFamilyComponents: HashMap<String, HashMap<String, String>>): Boolean {
        var hasMoreAmenities = false
        for (amenityCategory in fareFamilyComponents.values) {
            if (amenityCategory.size > 0) {
                hasMoreAmenities = true
                break
            }
        }
        return hasMoreAmenities
    }

    @JvmStatic fun getBagsAmenityResource(context: Context, fareFamilyComponents: HashMap<String, HashMap<String, String>>): AmenityResourceType {
        var resourceId = R.drawable.flight_upsell_cross_icon
        var dispVal = ""
        var amenityCategory : FlightAmenityCategory? = null
        var amenityValue = context.resources.getString(R.string.amenity_checked_bags)
        val bagAmenities = FlightBagAmenity.values()
        for (bagAmenity in bagAmenities) {
            amenityCategory = getAmenityCategory(context.resources.getString(bagAmenity.key), fareFamilyComponents)
            if (amenityCategory != null) {
                if (amenityCategory == FlightAmenityCategory.CHARGEABLE) {
                    dispVal = context.resources.getString(R.string.filter_price_cheap)
                } else if (amenityCategory == FlightAmenityCategory.INCLUDED) {
                    dispVal = getBagsAmenityCount(context, bagAmenity)
                    if (dispVal.isNullOrBlank()) {
                        resourceId = R.drawable.flight_upsell_tick_icon
                    } else {
                        amenityValue = Phrase.from(context.resources.getQuantityString(R.plurals.checked_bags_template, dispVal.toInt()))
                                .put("number", dispVal.toInt()).format().toString()
                    }
                } else {
                    resourceId = R.drawable.flight_upsell_cross_icon
                }
                break
            }
        }
        return AmenityResourceType(resourceId, dispVal, getAmenityContentDesc(context, amenityValue, amenityCategory))
    }

    @JvmStatic fun getCarryOnBagAmenityResource(context: Context, fareFamilyComponents: HashMap<String, HashMap<String, String>>): AmenityResourceType {
        var resourceId = 0
        var dispVal = ""
        val amenities = FlightCarryOnBagAmenity.values()
        var amenityCategory : FlightAmenityCategory? = null
        for (amenity in amenities) {
            amenityCategory = getAmenityCategory(context.resources.getString(amenity.key), fareFamilyComponents)
            if (amenityCategory != null) {
                val resourceType = getAmenityDrawable(context, amenityCategory)
                resourceId = resourceType.resourceId
                dispVal = resourceType.dispVal
                break
            }
        }
        return AmenityResourceType(resourceId, dispVal, getAmenityContentDesc(context, context.resources.getString(R.string.amenity_carry_on_bag), amenityCategory))
    }

    @JvmStatic fun getSeatSelectionAmenityResource(context: Context, fareFamilyComponents: HashMap<String, HashMap<String, String>>): AmenityResourceType {
        var resourceId = R.drawable.flight_upsell_cross_icon
        var dispVal = ""
        val seatSelectionAmenities = FlightSeatReservationAmenity.values()
        var amenityCategory : FlightAmenityCategory? = null
        for (seatSelectionAmenity in seatSelectionAmenities) {
            amenityCategory = getAmenityCategory(context.resources.getString(seatSelectionAmenity.key), fareFamilyComponents)
            if (amenityCategory != null) {
                val resourceType = getAmenityDrawable(context, amenityCategory)
                resourceId = resourceType.resourceId
                dispVal = resourceType.dispVal
                break
            }
        }
        return AmenityResourceType(resourceId, dispVal, getAmenityContentDesc(context, context.resources.getString(R.string.amenity_seat_choice), amenityCategory))
    }

    @JvmStatic fun getAmenityResourceType(context: Context, amenityKey: String, amenityValue: String, fareFamilyComponents: HashMap<String, HashMap<String, String>>): AmenityResourceType {
        var resourceId = R.drawable.flight_upsell_cross_icon
        var dispVal = ""
        val amenityCategory = getAmenityCategory(amenityKey, fareFamilyComponents)
        if (amenityCategory != null) {
            val resourceType = getAmenityDrawable(context, amenityCategory)
            resourceId = resourceType.resourceId
            dispVal = resourceType.dispVal
        }
        return AmenityResourceType(resourceId, dispVal, getAmenityContentDesc(context, amenityValue, amenityCategory))
    }

    private fun getAmenityContentDesc(context: Context, amenityValue: String, amenityCategory: FlightAmenityCategory?): String{
        val stringResId: Int
        when (amenityCategory) {
            FlightAmenityCategory.CHARGEABLE ->
                stringResId = R.string.amenity_chargeable_cont_desc_TEMPLATE
            FlightAmenityCategory.INCLUDED ->
                stringResId = R.string.amenity_included_cont_desc_TEMPLATE
            else ->
                stringResId = R.string.amenity_not_available_cont_desc_TEMPLATE
        }
        return Phrase.from(context.getString(stringResId)).put("amenity_name", amenityValue).format().toString()
    }

    private fun getBagsAmenityCount(context: Context, bagAmenity: FlightBagAmenity): String {
        return when (bagAmenity) {
            FlightBagAmenity.ONE_LUGGAGE -> context.resources.getString(R.string.one)
            FlightBagAmenity.TWO_LUGGAGE -> context.resources.getString(R.string.two)
            FlightBagAmenity.THREE_LUGGAGE -> context.resources.getString(R.string.three)
            FlightBagAmenity.FOUR_LUGGAGE -> context.resources.getString(R.string.four)
            else -> ""
        }
    }

    private fun getAmenityDrawable(context: Context, amenityCategory: FlightAmenityCategory): AmenityResourceType {
        var resourceId = 0
        var strVal = ""
        if (amenityCategory == FlightAmenityCategory.CHARGEABLE) {
            strVal = context.resources.getString(R.string.filter_price_cheap)
        } else {
            resourceId = when (amenityCategory) {
                FlightAmenityCategory.INCLUDED -> R.drawable.flight_upsell_tick_icon
                else -> R.drawable.flight_upsell_cross_icon
            }
        }
        return AmenityResourceType(resourceId, strVal, "")
    }

    private fun getAmenityCategory(amenityKey: String, fareFamilyComponents: HashMap<String, HashMap<String, String>>): FlightAmenityCategory? {
        var amenityCategory: FlightAmenityCategory? = null
        val amenityGroups = fareFamilyComponents

        for (amenityGroup in amenityGroups) {
            if (amenityGroup.value.containsKey(amenityKey)) {
                amenityCategory = FlightAmenityCategory.valueOf(amenityGroup.key.toUpperCase(Locale.US))
            }
        }
        return amenityCategory
    }
}
