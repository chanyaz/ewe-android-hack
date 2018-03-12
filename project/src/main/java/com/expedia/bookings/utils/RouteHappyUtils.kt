package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.flights.FlightServiceClassType
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.data.flights.RouteHappyRequestInfo
import com.expedia.bookings.data.flights.RouteHappyFlightCriteria
import com.expedia.bookings.data.flights.RouteHappyFlightLegDetail
import com.expedia.bookings.data.flights.RouteHappyFlightLegDetail.RouteHappyFlightLeg
import com.expedia.bookings.data.flights.RouteHappyFlightOfferDetail
import com.expedia.bookings.data.flights.RouteHappyFlightOfferDetail.RouteHappyFlightOffer
import com.expedia.bookings.data.flights.RouteHappyFlightSearch
import com.expedia.bookings.data.flights.RouteHappyFlightSegmentCriteria
import com.expedia.bookings.data.flights.RouteHappyFlightSegmentDetail
import com.expedia.bookings.data.flights.RouteHappyFlightSegmentDetail.RouteHappyFlightSegment
import com.expedia.bookings.data.flights.RouteHappyFlightTravelerCategory
import com.expedia.bookings.data.flights.RouteHappyFlightTravelerDetail
import com.expedia.bookings.data.flights.RouteHappyRequest
import com.expedia.bookings.data.flights.RouteHappyRichInfoDetail
import com.expedia.bookings.data.flights.RouteHappyRichInfoDetail.RouteHappyRichInfo
import com.expedia.bookings.data.flights.TravelerCode
import com.expedia.bookings.data.flights.TripType
import com.expedia.bookings.data.pos.PointOfSale
import java.util.ArrayList

object RouteHappyUtils {

    enum class ScoreExpression(val stringResId: Int) {
        EXCELLENT(R.string.flight_score_excellent_superlative_TEMPLATE),
        VERY_GOOD(R.string.flight_score_very_good_superlative_TEMPLATE),
        GOOD(R.string.flight_score_good_superlative_TEMPLATE),
        OKAY(R.string.flight_score_okay_superlative_TEMPLATE),
        FAIR(R.string.flight_score_fair_superlative_TEMPLATE),
        POOR(R.string.flight_score_poor_superlative_TEMPLATE)
    }

    @JvmStatic
    fun getRouteHappyRequestPayload(context: Context, flightLegList: List<FlightLeg>): RouteHappyRequest {
        val routeHappyRequestPayload = RouteHappyRequest()
        routeHappyRequestPayload.requestInfo = getRequestInfo(context)
        routeHappyRequestPayload.richInfoDetail = getRichInfoDetail(context, flightLegList)
        return routeHappyRequestPayload
    }

    private fun getRequestInfo(context: Context): RouteHappyRequestInfo {
        val pointOfSale = PointOfSale.getPointOfSale()
        val requestInfo = RouteHappyRequestInfo()
        requestInfo.tpid = pointOfSale.tpid.toString()
        requestInfo.eapid = pointOfSale.eapid.toString()

        val userStateManager = Ui.getApplication(context).appComponent().userStateManager()
        if (userStateManager.isUserAuthenticated()) {
            requestInfo.tuid = userStateManager.userSource.tuid ?: -1L
        }
        return requestInfo
    }

    private fun getRichInfoDetail(context: Context, flightLegList: List<FlightLeg>): RouteHappyRichInfoDetail {
        val richInfoDetail = RouteHappyRichInfoDetail()
        richInfoDetail.richInfoList = getRichInfoList(context, flightLegList)
        return richInfoDetail
    }

    private fun getRichInfoList(context: Context, flightLegList: List<FlightLeg>): List<RouteHappyRichInfo> {
        val richInfo = RouteHappyRichInfo()
        richInfo.flightSearch = getSearchContext()
        richInfo.flightOfferDetail = getFlightOfferDetail(context, flightLegList)

        val richInfoList = ArrayList<RouteHappyRichInfo>()
        richInfoList.add(richInfo)
        return richInfoList
    }

    private fun getSearchContext(): RouteHappyFlightSearch {
        val flightSearchParams = Db.getFlightSearchParams()
        val searchContext = RouteHappyFlightSearch()
        if (flightSearchParams != null) {
            searchContext.tripType = if (flightSearchParams.isRoundTrip()) TripType.ROUND_TRIP.type else TripType.ONEWAY.type
            searchContext.flightCriteria = getFlightCriteria(flightSearchParams)
        }
        return searchContext
    }

    private fun getFlightCriteria(flightSearchParams: FlightSearchParams): RouteHappyFlightCriteria {
        val flightCriteria = RouteHappyFlightCriteria()
        flightCriteria.travelerDetail = getTravelDetail(flightSearchParams)
        return flightCriteria
    }

    private fun getTravelDetail(flightSearchParams: FlightSearchParams): RouteHappyFlightTravelerDetail {
        val travelerDetail = RouteHappyFlightTravelerDetail()
        travelerDetail.travelerCategoryList = getTravelerList(flightSearchParams)
        return travelerDetail
    }

    private fun getTravelerList(flightSearchParams: FlightSearchParams): List<RouteHappyFlightTravelerCategory> {
        val travelerList = ArrayList<RouteHappyFlightTravelerCategory>()
        travelerList.add(getTravelerCategory(TravelerCode.ADULT.code, flightSearchParams.adults))
        travelerList.add(getTravelerCategory(TravelerCode.CHILD.code, flightSearchParams.children.size))
        return travelerList
    }

    private fun getTravelerCategory(travelerCode: String, travelerCount: Int): RouteHappyFlightTravelerCategory {
        val travelerDetail = RouteHappyFlightTravelerCategory()
        travelerDetail.travelerCode = travelerCode
        travelerDetail.travelerCount = travelerCount
        return travelerDetail
    }

    private fun getFlightOfferDetail(context: Context, flightLegList: List<FlightLeg>): RouteHappyFlightOfferDetail {
        val flightOfferDetail = RouteHappyFlightOfferDetail()
        flightOfferDetail.flightOfferList = getFlightOfferList(context, flightLegList)
        return flightOfferDetail
    }

    private fun getFlightOfferList(context: Context, flightLegList: List<FlightLeg>): List<RouteHappyFlightOffer> {
        val flightOfferList = ArrayList<RouteHappyFlightOffer>()
        for (flightLeg in flightLegList) {
            val flightOffer = RouteHappyFlightOffer()
            flightOffer.naturalKey = flightLeg.naturalKey
            flightOffer.flightLegDetail = getFlightLegDetail(context, flightLeg)
            flightOfferList.add(flightOffer)
        }
        return flightOfferList
    }

    private fun getFlightLegDetail(context: Context, flightLeg: FlightLeg): RouteHappyFlightLegDetail {
        val flightLegDetail = RouteHappyFlightLegDetail()
        flightLegDetail.flightLegList = getFlightLegList(context, flightLeg)
        return flightLegDetail
    }

    private fun getFlightLegList(context: Context, flightLeg: FlightLeg): List<RouteHappyFlightLeg> {
        val routeFlightLegList = ArrayList<RouteHappyFlightLeg>()
        val routeFlightLeg = RouteHappyFlightLeg()
        routeFlightLeg.id = flightLeg.legId
        if (CollectionUtils.isNotEmpty(flightLeg.segments)) {
            routeFlightLeg.flightSegmentDetail = getFlightSegmentDetail(context, flightLeg.segments, flightLeg.seatClassAndBookingCodeList)
        }
        routeFlightLegList.add(routeFlightLeg)
        return routeFlightLegList
    }

    private fun getFlightSegmentDetail(context: Context, flightSegmentList: List<FlightLeg.FlightSegment>,
                                       seatClassBookingCodeList: List<FlightTripDetails.SeatClassAndBookingCode>): RouteHappyFlightSegmentDetail {
        val flightSegmentDetail = RouteHappyFlightSegmentDetail()
        flightSegmentDetail.flightSegmentList = getFlightSegmentList(context, flightSegmentList, seatClassBookingCodeList)
        return flightSegmentDetail
    }

    private fun getFlightSegmentList(context: Context, flightSegmentList: List<FlightLeg.FlightSegment>,
                                     seatClassBookingCodeList: List<FlightTripDetails.SeatClassAndBookingCode>): List<RouteHappyFlightSegment> {
        val routeFlightSegmentList = ArrayList<RouteHappyFlightSegment>()
        for ((index, flightSegment) in flightSegmentList.withIndex()) {
            val routeFlightSegment = RouteHappyFlightSegment()
            routeFlightSegment.id = index.toString()
            routeFlightSegment.carrierCode = flightSegment.airlineCode
            routeFlightSegment.flightNumber = flightSegment.flightNumber
            routeFlightSegment.bookingCode = seatClassBookingCodeList[index].bookingCode
            routeFlightSegment.flightCriteria = getFlightCriteria(context, index, flightSegment, seatClassBookingCodeList)
            routeFlightSegmentList.add(routeFlightSegment)
        }
        return routeFlightSegmentList
    }

    private fun getFlightCriteria(context: Context, index: Int, flightSegment: FlightLeg.FlightSegment,
                                  seatClassBookingCodeList: List<FlightTripDetails.SeatClassAndBookingCode>): RouteHappyFlightSegmentCriteria {
        val flightCriteria = RouteHappyFlightSegmentCriteria()
        flightCriteria.origin = flightSegment.departureAirportCode
        flightCriteria.destination = flightSegment.arrivalAirportCode
        flightCriteria.date = flightSegment.departureTimeRaw.split("T")[0]
        flightCriteria.cabinClass = context.resources.getString(
                FlightServiceClassType.getCabinCodeResourceId(seatClassBookingCodeList[index].seatClass)).toUpperCase()
        return flightCriteria
    }
}
