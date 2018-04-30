package com.expedia.bookings.utils

import android.app.Activity
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.data.flights.TravelerCode
import com.expedia.bookings.data.flights.TripType
import com.expedia.bookings.test.robolectric.FlightTestUtil
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.squareup.phrase.Phrase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class RichContentUtilsTest {
    private lateinit var activity: Activity

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        Ui.getApplication(activity).defaultTravelerComponent()
        Ui.getApplication(activity).defaultFlightComponents()
    }

    @Test
    fun testRichContentResponse() {
        setupDb()
        val richContentRequest = RichContentUtils.getRichContentRequestPayload(activity, getFlightLegs())

        val richInfoDetail = richContentRequest.richInfoDetail!!.richInfoList[0]
        assertEquals(TripType.ONEWAY.type, richInfoDetail.flightSearch!!.tripType)

        val travelCategoryList = richInfoDetail.flightSearch!!.flightCriteria!!.travelerDetail!!.travelerCategoryList
        assertEquals(TravelerCode.ADULT.code, travelCategoryList[0].travelerCode)
        assertEquals(2, travelCategoryList[0].travelerCount)
        assertEquals(TravelerCode.CHILD.code, travelCategoryList[1].travelerCode)
        assertEquals(1, travelCategoryList[1].travelerCount)

        val flightOfferDetail = richInfoDetail.flightOfferDetail!!.flightOfferList[0]
        assertEquals("2018-04-25t21:37:00-07:00-coach-sfo-sea-as-303;2018-04-26t22:30:00-07:00-coach-sea-sfo-as-316;",
                flightOfferDetail.naturalKey)

        val flightLegDetail = flightOfferDetail.flightLegDetail!!.flightLegList[0]
        assertEquals("ab64aefca28e772ca024d4a00e6ae131", flightLegDetail.id)

        val flightSegmentDetail = flightLegDetail.flightSegmentDetail!!.flightSegmentList[0]
        assertEquals("AS", flightSegmentDetail.carrierCode)
        assertEquals("303", flightSegmentDetail.flightNumber)
        assertEquals("K", flightSegmentDetail.bookingCode)

        val flightCriteria = flightSegmentDetail.flightCriteria
        assertEquals("SEA", flightCriteria!!.origin)
        assertEquals("SFO", flightCriteria.destination)
        assertEquals("2018-04-25", flightCriteria.date)
        assertEquals("ECONOMY", flightCriteria.cabinClass)
    }

    @Test
    fun testScoreExpression() {
        assertEquals("9.5/10 - Excellent!", getRouteScoreExpression(RichContentUtils.ScoreExpression.EXCELLENT.stringResId))
        assertEquals("8.2/10 - Very Good!", getRouteScoreExpression(RichContentUtils.ScoreExpression.VERY_GOOD.stringResId))
        assertEquals("7.8/10 - Good!", getRouteScoreExpression(RichContentUtils.ScoreExpression.GOOD.stringResId))
        assertEquals("7/10 - Fair!", getRouteScoreExpression(RichContentUtils.ScoreExpression.FAIR.stringResId))
        assertEquals("6.8/10 - Okay!", getRouteScoreExpression(RichContentUtils.ScoreExpression.OKAY.stringResId))
        assertEquals("5/10 - Poor!", getRouteScoreExpression(RichContentUtils.ScoreExpression.POOR.stringResId))
    }

    private fun setupDb() {
        Db.setFlightSearchParams(FlightTestUtil.getFlightSearchParams(true))
        Db.sharedInstance.travelers = arrayListOf(Traveler())
    }

    private fun getFlightLegs(): List<FlightLeg> {
        val flightLeg = FlightLeg()
        flightLeg.naturalKey = "2018-04-25t21:37:00-07:00-coach-sfo-sea-as-303;2018-04-26t22:30:00-07:00-coach-sea-sfo-as-316;"
        flightLeg.legId = "ab64aefca28e772ca024d4a00e6ae131"

        val flightSegment = FlightLeg.FlightSegment()
        flightSegment.airlineCode = "AS"
        flightSegment.flightNumber = "303"
        flightSegment.departureAirportCode = "SEA"
        flightSegment.arrivalAirportCode = "SFO"
        flightSegment.departureTimeRaw = "2018-04-25TT06:40:00.000-07:00"
        val segments = listOf<FlightLeg.FlightSegment>(flightSegment)
        flightLeg.segments = segments

        val seatClassBookingCode = FlightTripDetails.SeatClassAndBookingCode()
        seatClassBookingCode.bookingCode = "K"
        seatClassBookingCode.seatClass = "coach"
        val seatClassAndBookingCodeList = listOf<FlightTripDetails.SeatClassAndBookingCode>(seatClassBookingCode)
        flightLeg.seatClassAndBookingCodeList = seatClassAndBookingCodeList

        val flightLegs = listOf<FlightLeg>(flightLeg)
        return flightLegs
    }

    private fun getRouteScoreExpression(stringResId: Int): String {
        val scoreExpPhrase = Phrase.from(activity, stringResId)
        when (stringResId) {
            R.string.route_score_very_good_superlative_TEMPLATE -> scoreExpPhrase.put("route_score", "8.2")
            R.string.route_score_good_superlative_TEMPLATE -> scoreExpPhrase.put("route_score", "7.8")
            R.string.route_score_fair_superlative_TEMPLATE -> scoreExpPhrase.put("route_score", "7")
            R.string.route_score_okay_superlative_TEMPLATE -> scoreExpPhrase.put("route_score", "6.8")
            R.string.route_score_poor_superlative_TEMPLATE -> scoreExpPhrase.put("route_score", "5")
            else -> scoreExpPhrase.put("route_score", "9.5")
        }
        return scoreExpPhrase.format().toString()
    }
}
