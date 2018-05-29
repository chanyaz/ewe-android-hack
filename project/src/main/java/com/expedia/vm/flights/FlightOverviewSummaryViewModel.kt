package com.expedia.vm.flights

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.FlightTripResponse
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.extensions.withLatestFrom
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.utils.isRichContentEnabled
import com.squareup.phrase.Phrase
import io.reactivex.subjects.PublishSubject
import org.joda.time.format.DateTimeFormat

class FlightOverviewSummaryViewModel(val context: Context) {

    val params = PublishSubject.create<FlightSearchParams>()
    val outboundFlightTitle = PublishSubject.create<String>()
    val inboundFlightTitle = PublishSubject.create<String>()
    val tripResponse = PublishSubject.create<FlightTripResponse>()
    val outboundBundleBaggageUrlSubject = PublishSubject.create<String>()
    val updatedOutboundFlightLegSubject = PublishSubject.create<FlightLeg>()
    val updatedInboundFlightLegSubject = PublishSubject.create<FlightLeg>()
    val updateOutboundSeatClassAndCodeSubject = PublishSubject.create<List<FlightTripDetails.SeatClassAndBookingCode>>()
    val updateInboundSeatClassAndCodeSubject = PublishSubject.create<List<FlightTripDetails.SeatClassAndBookingCode>>()
    val inboundBundleBaggageUrlSubject = PublishSubject.create<String>()
    val freeCancellationInfoClickSubject = PublishSubject.create<Unit>()
    val freeCancellationInfoSubject = PublishSubject.create<Boolean>()
    val isFareFamilyUpgraded = PublishSubject.create<Boolean>()
    val refreshOutboundBundleWidgetStream = PublishSubject.create<Unit>()

    init {
        freeCancellationInfoClickSubject
                .withLatestFrom(freeCancellationInfoSubject, { _, visibility -> !visibility })
                .subscribe(freeCancellationInfoSubject)
        freeCancellationInfoSubject.onNext(false)
        params.subscribe { params ->
            val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
            val departureAirport = params.departureAirport.hierarchyInfo!!.airport!!.airportCode
            val arrivalAirport = params.arrivalAirport.hierarchyInfo!!.airport!!.airportCode

            outboundFlightTitle.onNext(Phrase.from(context, R.string.flight_overview_header_TEMPLATE)
                    .put("departureairportcode", departureAirport)
                    .put("arrivalairportcode", arrivalAirport)
                    .put("dateoftravelling", LocaleBasedDateFormatUtils.yyyyMMddStringToEEEMMMddyyyy(params?.departureDate?.toString(formatter)))
                    .format().toString())
            inboundFlightTitle.onNext(
                    if (params?.returnDate != null) {
                        Phrase.from(context, R.string.flight_overview_header_TEMPLATE)
                                .put("departureairportcode", arrivalAirport)
                                .put("arrivalairportcode", departureAirport)
                                .put("dateoftravelling", LocaleBasedDateFormatUtils.yyyyMMddStringToEEEMMMddyyyy(params.returnDate?.toString(formatter)))
                                .format().toString()
                    } else {
                        ""
                    })
        }
        tripResponse.withLatestFrom(params, {
            tripResponse, params ->
            object {
                val params = params
                val trip = tripResponse
            }
        }).subscribe {
            outboundBundleBaggageUrlSubject.onNext(it.trip.details.getLegs()[0].baggageFeesUrl)
            updateOutboundSeatClassAndCodeSubject.onNext(it.trip.details.offer.offersSeatClassAndBookingCode.first())
            updatedOutboundFlightLegSubject.onNext(it.trip.details.getLegs()[0])
            if (it.params?.returnDate != null && it.trip.details.getLegs().size > 1) {
                inboundBundleBaggageUrlSubject.onNext(it.trip.details.getLegs()[1].baggageFeesUrl)
                updatedInboundFlightLegSubject.onNext(it.trip.details.getLegs()[1])
                updateInboundSeatClassAndCodeSubject.onNext(it.trip.details.offer.offersSeatClassAndBookingCode.last())
            }
            isFareFamilyUpgraded.onNext(it.trip.isFareFamilyUpgraded)
            if (isRichContentEnabled(context)) {
                if (!it.trip.isFareFamilyUpgraded && it.params.isRoundTrip()) {
                    refreshOutboundBundleWidgetStream.onNext(Unit)
                }
            }
        }
    }
}
