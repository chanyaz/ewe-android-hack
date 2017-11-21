package com.expedia.bookings.itin.vm

import android.content.Context
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.data.FlightLeg
import com.expedia.bookings.data.trips.ItinCardDataFlight
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.data.trips.TripFlight
import com.expedia.bookings.itin.data.FlightItinLegsDetailData
import com.squareup.phrase.Phrase
import rx.subjects.PublishSubject

class FlightItinManageBookingViewModel(val context: Context, private val itinId: String) {

    lateinit var itinCardDataFlight: ItinCardDataFlight
    var itineraryManager: ItineraryManager = ItineraryManager.getInstance()

    val itinCardDataNotValidSubject: PublishSubject<Unit> = PublishSubject.create<Unit>()
    val itinCardDataFlightObservable = PublishSubject.create<ItinCardDataFlight>()
    val updateToolbarSubject = PublishSubject.create<ItinToolbarViewModel.ToolbarParams>()
    val customerSupportDetailsSubject = PublishSubject.create<ItinCustomerSupportDetailsViewModel.ItinCustomerSupportDetailsWidgetParams>()
    val flightLegDetailWidgetLegDataSubject = PublishSubject.create<ArrayList<FlightItinLegsDetailData>>()

    fun setUp() {
        updateItinCardDataFlight()
        updateToolbar()
        updateCustomerSupportDetails()
        createFlightLegDetailWidgetData()
    }

    fun updateItinCardDataFlight() {
        val freshItinCardDataFlight = itineraryManager.getItinCardDataFromItinId(itinId) as ItinCardDataFlight?
        if (freshItinCardDataFlight == null) {
            itinCardDataNotValidSubject.onNext(Unit)
        } else {
            itinCardDataFlightObservable.onNext(freshItinCardDataFlight)
            itinCardDataFlight = freshItinCardDataFlight
        }
    }

    fun updateCustomerSupportDetails() {
        val header = Phrase.from(context, R.string.itin_flight_customer_support_header_text_TEMPLATE).put("brand", BuildConfig.brand).format().toString()
        val itineraryNumb = Phrase.from(context, R.string.itin_flight_itinerary_number_TEMPLATE).put("itin_number", itinCardDataFlight.tripNumber).format().toString()
        val customerSupportNumber = itinCardDataFlight.tripComponent.parentTrip.customerSupport.supportPhoneNumberDomestic
        val customerSupportButton = Phrase.from(context, R.string.itin_flight_customer_support_site_header_TEMPLATE).put("brand", BuildConfig.brand).format().toString()
        val customerSupportURL = itinCardDataFlight.tripComponent.parentTrip.customerSupport.supportUrl
        customerSupportDetailsSubject.onNext(ItinCustomerSupportDetailsViewModel.ItinCustomerSupportDetailsWidgetParams(header, itineraryNumb, customerSupportNumber, customerSupportButton, customerSupportURL))
    }

    fun createOmnitureTrackingValues(): HashMap<String, String?> {
        return FlightItinOmnitureUtils().createOmnitureTrackingValues(itinCardDataFlight)
    }

    private fun updateToolbar() {
        val title = context.getString(R.string.itin_flight_manage_booking_header)
        val destinationCity = Phrase.from(context, R.string.itin_flight_toolbar_title_TEMPLATE).
                put("destination", itinCardDataFlight.flightLeg.lastWaypoint.airport.mCity ?: "").format().toString()
        updateToolbarSubject.onNext(ItinToolbarViewModel.ToolbarParams(title, destinationCity, false))
    }

    private fun createFlightLegDetailWidgetData() {
        val list = ArrayList<FlightItinLegsDetailData>()
        val flightLegsList = (itinCardDataFlight.tripComponent as TripFlight).flightTrip.legs

        for (leg: FlightLeg in flightLegsList) {
            val departureAirportCode = leg.firstWaypoint.mAirportCode
            val arrivalAirportCode = leg.lastWaypoint.mAirportCode
            val imgPath = leg.airlineLogoURL
            val numbOfStops = leg.numberOfStops
            val departureMonthDay = leg.legDepartureTime.localizedMediumDate
            val departureTime = leg.legDepartureTime.localizedShortTime
            val arrivalMonthDay = leg.legArrivaltime.localizedMediumDate
            val arrivalTime = leg.legArrivaltime.localizedShortTime
            val flightItinLegsDetailData = FlightItinLegsDetailData(imgPath, departureAirportCode, arrivalAirportCode, departureMonthDay, departureTime, arrivalMonthDay, arrivalTime, numbOfStops)
            list.add(flightItinLegsDetailData)
        }
        flightLegDetailWidgetLegDataSubject.onNext(list)
    }
}