package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.extension.getEarnMessage
import com.expedia.bookings.utils.FlightV2Utils
import com.expedia.bookings.utils.SpannableBuilder
import com.squareup.phrase.Phrase
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

abstract class AbstractFlightViewModel(protected val context: Context, protected val flightLeg: FlightLeg) {
    val resources = context.resources
    val flightTime = FlightV2Utils.getFlightDepartureArrivalTimeAndDays(context, flightLeg)
    val asscesibleFlightTime = FlightV2Utils.getAccessibleDepartArrivalTime(context, flightLeg)
    val airline = FlightV2Utils.getDistinctiveAirline(flightLeg.airlines)
    val duration = FlightV2Utils.getFlightDurationStopString(context, flightLeg)
    val layover = flightLeg
    var flightSegments = flightLeg.flightSegments
    val earnMessage = flightLeg.packageOfferModel?.loyaltyInfo?.earn?.getEarnMessage(context, false) ?: ""
    var seatsLeftUrgencyMessage = FlightV2Utils.getSeatsLeftUrgencyMessage(context, flightLeg)
    val updateflightCabinPreferenceObservable = BehaviorSubject.create<String>(FlightV2Utils.getFlightCabinPreferences(context, flightLeg))

    abstract fun price(): String
    abstract fun getUrgencyMessageVisibility(seatsLeft: String): Boolean
    abstract fun getFlightCabinPreferenceVisibility(): Boolean
    abstract fun isEarnMessageVisible(earnMessage: String): Boolean
    abstract fun getFlightDetailCardContDescriptionStringID(): Int
    abstract fun isUserBucketedInHideFSRInfographicTest() : Boolean

    fun getFlightContentDesc(isBestFlightVisible: Boolean): CharSequence {
        val result = SpannableBuilder()

        if (isBestFlightVisible) {
            result.append(context.getString(R.string.best_flight_detail_card_cont_desc))
        }
        result.append(Phrase.from(context, getFlightDetailCardContDescriptionStringID())
                .put("time", asscesibleFlightTime)
                .putOptional("price", price())
                .putOptional("pricedifference", price())
                .put("airline", FlightV2Utils.getAirlinesList(airline))
                .put("hours", getHourTimeContDesc(flightLeg.durationHour))
                .put("minutes", getMinuteTimeContDesc(flightLeg.durationMinute))
                .put("stops", flightLeg.stopCount)
                .format()
                .toString())
        if (flightSegments != null) {
            for (segment in flightSegments) {
                result.append(Phrase.from(context, R.string.flight_detail_flight_duration_card_cont_desc_TEMPLATE).
                        put("departureairport", segment.departureAirportCode).
                        put("arrivalairport", segment.arrivalAirportCode).
                        put("durationhours", getHourTimeContDesc(segment.durationHours)).
                        put("durationmins", getMinuteTimeContDesc(segment.durationMinutes)).format().toString())
                if (segment.layoverDurationHours != 0 || segment.layoverDurationMinutes != 0) {
                    result.append(Phrase.from(context, R.string.flight_detail_layover_duration_card_cont_desc_TEMPLATE).
                            put("layoverhours", getHourTimeContDesc(segment.layoverDurationHours)).
                            put("layovermins", getMinuteTimeContDesc(segment.layoverDurationMinutes)).format().toString())
                }
            }
        }
        if (getFlightCabinPreferenceVisibility()) {
            result.append(Phrase.from(context, R.string.flight_detail_cabin_class_desc_TEMPLATE).
                    put("class", updateflightCabinPreferenceObservable.value).format().toString())
        }
        if (getUrgencyMessageVisibility(seatsLeftUrgencyMessage)) {
            val seatsLeft = flightLeg.packageOfferModel.urgencyMessage.ticketsLeft
            result.append(Phrase.from(context.resources.getQuantityString(R.plurals.flight_detail_urgency_message_cont_desc_TEMPLATE, seatsLeft))
                    .put("seatsleft", seatsLeft).format().toString())
        }
        result.append(appendAccessibilityContentDescription())

        return result.build()
    }

    fun getHourTimeContDesc(hours: Int): CharSequence {
        return Phrase.from(context.resources.getQuantityString(R.plurals.hours_from_now, hours)).put("hours", hours).format().toString()
    }

    fun getMinuteTimeContDesc(minutes: Int): CharSequence {
        return Phrase.from(context.resources.getQuantityString(R.plurals.minutes_from_now, minutes)).put("minutes", minutes).format().toString()
    }

    open fun appendAccessibilityContentDescription(): String{
        return context.getString(R.string.accessibility_cont_desc_role_button);
    }
}