package com.expedia.bookings.itin.flight.manageBooking

import android.content.Context
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.data.FlightLeg
import com.expedia.bookings.data.trips.ItinCardDataFlight
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.data.trips.TripFlight
import com.expedia.bookings.itin.flight.common.ItinOmnitureUtils
import com.expedia.bookings.itin.common.ItinCustomerSupportDetailsViewModel
import com.expedia.bookings.itin.common.ItinToolbarViewModel
import com.expedia.bookings.itin.tripstore.utils.IJsonToItinUtil
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.Ui
import com.squareup.phrase.Phrase
import io.reactivex.subjects.PublishSubject

class FlightItinManageBookingViewModel(val context: Context, private val itinId: String) {

    private val REFUNDABILITY_TEXT = "refundabilityText"
    private val CANCEL_CHANGE_INTRODUCTION_TEXT = "cancelChangeIntroductionText"
    private val COMPLETE_PENALTY_RULES = "completePenaltyRules"
    private val AIRLINE_LIABILITY_LIMITATIONS = "airlineLiabilityLimitations"

    lateinit var itinCardDataFlight: ItinCardDataFlight
    var itineraryManager: ItineraryManager = ItineraryManager.getInstance()
    var readJsonUtil: IJsonToItinUtil = Ui.getApplication(context).tripComponent().jsonUtilProvider()

    val itinCardDataNotValidSubject: PublishSubject<Unit> = PublishSubject.create<Unit>()
    val itinCardDataFlightObservable = PublishSubject.create<ItinCardDataFlight>()
    val updateToolbarSubject = PublishSubject.create<ItinToolbarViewModel.ToolbarParams>()
    val customerSupportDetailsSubject = PublishSubject.create<ItinCustomerSupportDetailsViewModel.ItinCustomerSupportDetailsWidgetParams>()
    val flightLegDetailWidgetLegDataSubject = PublishSubject.create<ArrayList<FlightItinLegsDetailData>>()
    val flightLegDetailRulesAndRegulationSubject = PublishSubject.create<String>()
    val flightSplitTicketVisibilitySubject = PublishSubject.create<Boolean>()
    val flightItinAirlineSupportDetailsSubject = PublishSubject.create<FlightItinAirlineSupportDetailsViewModel.FlightItinAirlineSupportDetailsWidgetParams>()

    init {
        itinCardDataFlightObservable.subscribe {
            updateToolbar()
            updateCustomerSupportDetails()
            createFlightLegDetailWidgetData()
            rulesAndRestrictionText()
            flightSplitTicketText()
            airlineSupportDetailsData()
        }
    }

    fun setUp() {
        updateItinCardDataFlight()
    }

    fun updateItinCardDataFlight() {
        val freshItinCardDataFlight = itineraryManager.getItinCardDataFromItinId(itinId) as ItinCardDataFlight?
        if (freshItinCardDataFlight == null) {
            itinCardDataNotValidSubject.onNext(Unit)
        } else {
            itinCardDataFlight = freshItinCardDataFlight
            itinCardDataFlightObservable.onNext(freshItinCardDataFlight)
        }
    }

    fun updateCustomerSupportDetails() {
        val header = Phrase.from(context, R.string.itin_flight_customer_support_header_text_TEMPLATE).put("brand", BuildConfig.brand).format().toString()
        val itineraryNumb = itinCardDataFlight.tripNumber ?: ""
        val customerSupportNumber = itinCardDataFlight.tripComponent.parentTrip.customerSupport.supportPhoneNumberDomestic
        val customerSupportButton = Phrase.from(context, R.string.itin_flight_customer_support_site_header_TEMPLATE).put("brand", BuildConfig.brand).format().toString()
        val customerSupportURL = itinCardDataFlight.tripComponent.parentTrip.customerSupport.supportUrl
        val nullableIsGuest = readJsonUtil.getItin(itinCardDataFlight.tripId)?.isGuest
        var isGuest = false
        if (nullableIsGuest != null) {
            isGuest = nullableIsGuest
        }
        customerSupportDetailsSubject.onNext(ItinCustomerSupportDetailsViewModel.ItinCustomerSupportDetailsWidgetParams(header, itineraryNumb, customerSupportNumber, customerSupportButton, customerSupportURL, isGuest))
    }

    fun createOmnitureTrackingValues(): HashMap<String, String?> {
        return ItinOmnitureUtils.createOmnitureTrackingValues(itinCardDataFlight)
    }

    private fun updateToolbar() {
        val title = context.getString(R.string.itin_flight_manage_booking_header)
        val destinationCity = Phrase.from(context, R.string.itin_flight_toolbar_title_TEMPLATE)
                .put("destination", itinCardDataFlight.flightLeg.lastWaypoint?.airport?.mCity ?: "").format().toString()
        updateToolbarSubject.onNext(ItinToolbarViewModel.ToolbarParams(title, destinationCity, false))
    }

    private fun createFlightLegDetailWidgetData() {
        val list = ArrayList<FlightItinLegsDetailData>()
        val flightLegsList = (itinCardDataFlight.tripComponent as TripFlight).flightTrip.legs

        for (leg: FlightLeg in flightLegsList) {
            val departureAirportCode = leg.firstWaypoint?.mAirportCode ?: ""
            val arrivalAirportCode = leg.lastWaypoint?.mAirportCode ?: ""
            val imgPath = leg.airlineLogoURL
            val numbOfStops = leg.numberOfStops
            val departureMonthDay = leg.legDepartureTime?.localizedMediumDate ?: ""
            val departureTime = leg.legDepartureTime?.localizedShortTime ?: ""
            val arrivalMonthDay = leg.legArrivalTime?.localizedMediumDate ?: ""
            val arrivalTime = leg.legArrivalTime?.localizedShortTime ?: ""
            val flightItinLegsDetailData = FlightItinLegsDetailData(imgPath, departureAirportCode, arrivalAirportCode, departureMonthDay, departureTime, arrivalMonthDay, arrivalTime, numbOfStops)
            list.add(flightItinLegsDetailData)
        }
        flightLegDetailWidgetLegDataSubject.onNext(list)
    }

    private fun rulesAndRestrictionText() {
        val stringBuilder = StringBuilder()
        val flightTrip = (itinCardDataFlight.tripComponent as TripFlight).flightTrip
        val cancelChangeIntroductionText: String? = flightTrip.getRule(CANCEL_CHANGE_INTRODUCTION_TEXT)?.text
        appendStringWithBreak(stringBuilder, cancelChangeIntroductionText)
        val refundabilityText: String? = flightTrip.getRule(REFUNDABILITY_TEXT)?.text
        appendBoldStringWithBreak(stringBuilder, refundabilityText)
        val completePenaltyRulesText: String? = flightTrip.getRule(COMPLETE_PENALTY_RULES)?.textAndURL
        appendStringWithBreak(stringBuilder, completePenaltyRulesText)
        val airlineLiabilityLimitationsText: String? = flightTrip.getRule(AIRLINE_LIABILITY_LIMITATIONS)?.textAndURL
        if (Strings.isNotEmpty(airlineLiabilityLimitationsText)) {
            stringBuilder.append(airlineLiabilityLimitationsText)
        }
        flightLegDetailRulesAndRegulationSubject.onNext(removeSpanTag(stringBuilder))
    }

    private fun appendStringWithBreak(builder: StringBuilder, text: String?) {
        if (Strings.isNotEmpty(text)) {
            builder.append(text)
            builder.append("<br><br>")
        }
    }

    private fun appendBoldStringWithBreak(builder: StringBuilder, text: String?) {
        if (Strings.isNotEmpty(text)) {
            builder.append("<b>")
            builder.append(text)
            builder.append("</b>")
            builder.append("<br><br>")
        }
    }

    private fun removeSpanTag(htmlString: StringBuilder): String {
        val stringBuilder = StringBuilder()
        if (htmlString.contains("<span") && htmlString.contains("</span>")) {
            val arr = htmlString.split("<")
            for (value: String in arr) {
                if (!value.contains("span")) {
                    if (value.contains(">")) {
                        stringBuilder.append("<")
                    }
                    stringBuilder.append(value)
                }
            }
            return stringBuilder.toString()
        }
        return htmlString.toString()
    }

    private fun flightSplitTicketText() {
        val flightTrip = (itinCardDataFlight.tripComponent as TripFlight).flightTrip
        flightSplitTicketVisibilitySubject.onNext(flightTrip.isSplitTicket)
    }

    fun airlineSupportDetailsData() {
        val tripFlight = itinCardDataFlight.tripComponent as TripFlight
        val ticketValue = itinCardDataFlight.getTicketNumbers()
        val confirmationValue = itinCardDataFlight.getSpannedConfirmationNumbers(context).toString()
        val airlineSupportUrlValue = tripFlight.flightTrip.airlineManageBookingURL ?: ""
        val itineraryNumber = itinCardDataFlight.tripNumber ?: ""
        val airlineName = itinCardDataFlight.getAirlineName()

        var title: String
        var airlineSupport: String
        var customerSupportSiteText: String

        if (Strings.isEmpty(airlineName)) {
            title = Phrase.from(context, R.string.itin_flight_airline_support_widget_airlines_for_help_TEMPLATE).put("airline_name", context.getString(R.string.itin_flight_airline_support_widget_the_airline_text)).format().toString()
            airlineSupport = Phrase.from(context, R.string.itin_flight_airline_support_widget_airlines_support_TEMPLATE).put("airline_name", context.getString(R.string.itin_flight_airline_support_widget_airline_text)).format().toString()
            customerSupportSiteText = Phrase.from(context, R.string.itin_flight_airline_support_widget_customer_support_TEMPLATE).put("airline_name", context.getString(R.string.itin_flight_airline_support_widget_airline_text)).format().toString()
        } else {
            title = Phrase.from(context, R.string.itin_flight_airline_support_widget_airlines_for_help_TEMPLATE).put("airline_name", airlineName).format().toString()
            airlineSupport = Phrase.from(context, R.string.itin_flight_airline_support_widget_airlines_support_TEMPLATE).put("airline_name", airlineName).format().toString()
            customerSupportSiteText = Phrase.from(context, R.string.itin_flight_airline_support_widget_customer_support_TEMPLATE).put("airline_name", airlineName).format().toString()
        }
        val callSupportNumber = ""
        val nullableIsGuest = readJsonUtil.getItin(itinCardDataFlight.tripId)?.isGuest
        var isGuest = false
        if (nullableIsGuest != null && nullableIsGuest) {
            isGuest = nullableIsGuest
        }
        flightItinAirlineSupportDetailsSubject.onNext(FlightItinAirlineSupportDetailsViewModel.FlightItinAirlineSupportDetailsWidgetParams(title, airlineSupport, ticketValue, confirmationValue, itineraryNumber, callSupportNumber, customerSupportSiteText, airlineSupportUrlValue, isGuest))
    }
}
