package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.flights.FlightServiceClassType
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.data.flights.RichContentRequestInfo
import com.expedia.bookings.data.flights.RichContentFlightCriteria
import com.expedia.bookings.data.flights.RichContentFlightLegDetail
import com.expedia.bookings.data.flights.RichContentFlightLegDetail.RichContentFlightLeg
import com.expedia.bookings.data.flights.RichContentFlightOfferDetail
import com.expedia.bookings.data.flights.RichContentFlightOfferDetail.RichContentFlightOffer
import com.expedia.bookings.data.flights.RichContentFlightSearch
import com.expedia.bookings.data.flights.RichContentFlightSegmentCriteria
import com.expedia.bookings.data.flights.RichContentFlightSegmentDetail
import com.expedia.bookings.data.flights.RichContentFlightSegmentDetail.RichContentFlightSegment
import com.expedia.bookings.data.flights.RichContentFlightTravelerCategory
import com.expedia.bookings.data.flights.RichContentFlightTravelerDetail
import com.expedia.bookings.data.flights.RichContentRequest
import com.expedia.bookings.data.flights.RichContentDetail
import com.expedia.bookings.data.flights.RichContentDetail.RichContentInfo
import com.expedia.bookings.data.flights.TravelerCode
import com.expedia.bookings.data.flights.TripType
import com.expedia.bookings.data.pos.PointOfSale
import java.util.ArrayList

object RichContentUtils {

    enum class ScoreExpression(val stringResId: Int) {
        EXCELLENT(R.string.route_score_excellent_superlative_TEMPLATE),
        VERY_GOOD(R.string.route_score_very_good_superlative_TEMPLATE),
        GOOD(R.string.route_score_good_superlative_TEMPLATE),
        OKAY(R.string.route_score_okay_superlative_TEMPLATE),
        FAIR(R.string.route_score_fair_superlative_TEMPLATE),
        POOR(R.string.route_score_poor_superlative_TEMPLATE)
    }

    @JvmStatic
    fun getRichContentRequestPayload(context: Context, flightLegList: List<FlightLeg>): RichContentRequest {
        val richContentRequestPayload = RichContentRequest()
        richContentRequestPayload.requestInfo = getRequestInfo(context)
        richContentRequestPayload.richInfoDetail = getRichInfoDetail(flightLegList)
        return richContentRequestPayload
    }

    private fun getRequestInfo(context: Context): RichContentRequestInfo {
        val pointOfSale = PointOfSale.getPointOfSale()
        val requestInfo = RichContentRequestInfo()
        requestInfo.tpid = pointOfSale.tpid.toString()
        requestInfo.eapid = pointOfSale.eapid.toString()

        val userStateManager = Ui.getApplication(context).appComponent().userStateManager()
        if (userStateManager.isUserAuthenticated()) {
            requestInfo.tuid = userStateManager.userSource.tuid ?: -1L
        }
        return requestInfo
    }

    private fun getRichInfoDetail(flightLegList: List<FlightLeg>): RichContentDetail {
        val richInfoDetail = RichContentDetail()
        richInfoDetail.richInfoList = getRichInfoList(flightLegList)
        return richInfoDetail
    }

    private fun getRichInfoList(flightLegList: List<FlightLeg>): List<RichContentInfo> {
        val richInfo = RichContentInfo()
        richInfo.flightSearch = getSearchContext()
        richInfo.flightOfferDetail = getFlightOfferDetail(flightLegList)

        val richInfoList = ArrayList<RichContentInfo>()
        richInfoList.add(richInfo)
        return richInfoList
    }

    private fun getSearchContext(): RichContentFlightSearch {
        val flightSearchParams = Db.getFlightSearchParams()
        val searchContext = RichContentFlightSearch()
        if (flightSearchParams != null) {
            searchContext.tripType = TripType.ONEWAY.type
            searchContext.flightCriteria = getFlightCriteria(flightSearchParams)
        }
        return searchContext
    }

    private fun getFlightCriteria(flightSearchParams: FlightSearchParams): RichContentFlightCriteria {
        val flightCriteria = RichContentFlightCriteria()
        flightCriteria.travelerDetail = getTravelDetail(flightSearchParams)
        return flightCriteria
    }

    private fun getTravelDetail(flightSearchParams: FlightSearchParams): RichContentFlightTravelerDetail {
        val travelerDetail = RichContentFlightTravelerDetail()
        travelerDetail.travelerCategoryList = getTravelerList(flightSearchParams)
        return travelerDetail
    }

    private fun getTravelerList(flightSearchParams: FlightSearchParams): List<RichContentFlightTravelerCategory> {
        val travelerList = ArrayList<RichContentFlightTravelerCategory>()
        travelerList.add(getTravelerCategory(TravelerCode.ADULT.code, flightSearchParams.adults))
        travelerList.add(getTravelerCategory(TravelerCode.CHILD.code, flightSearchParams.children.size))
        return travelerList
    }

    private fun getTravelerCategory(travelerCode: String, travelerCount: Int): RichContentFlightTravelerCategory {
        val travelerDetail = RichContentFlightTravelerCategory()
        travelerDetail.travelerCode = travelerCode
        travelerDetail.travelerCount = travelerCount
        return travelerDetail
    }

    private fun getFlightOfferDetail(flightLegList: List<FlightLeg>): RichContentFlightOfferDetail {
        val flightOfferDetail = RichContentFlightOfferDetail()
        flightOfferDetail.flightOfferList = getFlightOfferList(flightLegList)
        return flightOfferDetail
    }

    private fun getFlightOfferList(flightLegList: List<FlightLeg>): List<RichContentFlightOffer> {
        val flightOfferList = ArrayList<RichContentFlightOffer>()
        for (flightLeg in flightLegList) {
            val flightOffer = RichContentFlightOffer()
            flightOffer.naturalKey = flightLeg.naturalKey
            flightOffer.flightLegDetail = getFlightLegDetail(flightLeg)
            flightOfferList.add(flightOffer)
        }
        return flightOfferList
    }

    private fun getFlightLegDetail(flightLeg: FlightLeg): RichContentFlightLegDetail {
        val flightLegDetail = RichContentFlightLegDetail()
        flightLegDetail.flightLegList = getFlightLegList(flightLeg)
        return flightLegDetail
    }

    private fun getFlightLegList(flightLeg: FlightLeg): List<RichContentFlightLeg> {
        val richContentFlightLegList = ArrayList<RichContentFlightLeg>()
        val richContentFlightLeg = RichContentFlightLeg()
        richContentFlightLeg.id = flightLeg.legId
        if (CollectionUtils.isNotEmpty(flightLeg.segments)) {
            richContentFlightLeg.flightSegmentDetail = getFlightSegmentDetail(flightLeg.segments, flightLeg.seatClassAndBookingCodeList)
        }
        richContentFlightLegList.add(richContentFlightLeg)
        return richContentFlightLegList
    }

    private fun getFlightSegmentDetail(flightSegmentList: List<FlightLeg.FlightSegment>,
                                       seatClassBookingCodeList: List<FlightTripDetails.SeatClassAndBookingCode>): RichContentFlightSegmentDetail {
        val flightSegmentDetail = RichContentFlightSegmentDetail()
        flightSegmentDetail.flightSegmentList = getFlightSegmentList(flightSegmentList, seatClassBookingCodeList)
        return flightSegmentDetail
    }

    private fun getFlightSegmentList(flightSegmentList: List<FlightLeg.FlightSegment>,
                                     seatClassBookingCodeList: List<FlightTripDetails.SeatClassAndBookingCode>): List<RichContentFlightSegment> {
        val richContentFlightSegmentList = ArrayList<RichContentFlightSegment>()
        for ((index, flightSegment) in flightSegmentList.withIndex()) {
            val richContentFlightSegment = RichContentFlightSegment()
            richContentFlightSegment.id = index.toString()
            richContentFlightSegment.carrierCode = flightSegment.airlineCode
            richContentFlightSegment.flightNumber = flightSegment.flightNumber
            richContentFlightSegment.bookingCode = seatClassBookingCodeList[index].bookingCode
            richContentFlightSegment.flightCriteria = getFlightCriteria(index, flightSegment, seatClassBookingCodeList)
            richContentFlightSegmentList.add(richContentFlightSegment)
        }
        return richContentFlightSegmentList
    }

    private fun getFlightCriteria(index: Int, flightSegment: FlightLeg.FlightSegment,
                                  seatClassBookingCodeList: List<FlightTripDetails.SeatClassAndBookingCode>): RichContentFlightSegmentCriteria {
        val flightCriteria = RichContentFlightSegmentCriteria()
        flightCriteria.origin = flightSegment.departureAirportCode
        flightCriteria.destination = flightSegment.arrivalAirportCode
        flightCriteria.date = flightSegment.departureTimeRaw.split("T")[0]
        flightCriteria.cabinClass = FlightServiceClassType.getCabinCodeForRichContent(seatClassBookingCodeList[index].seatClass)
        return flightCriteria
    }
}
