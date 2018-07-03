package com.expedia.bookings.test

import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.flights.RichContent
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.RichContentUtils
import org.joda.time.LocalDate

class MockFlightServiceTestRule : ServicesRule<FlightServices>(FlightServices::class.java) {

    fun flightSearchParams(roundTrip: Boolean, airportCode: String = "happy"): FlightSearchParams {
        val origin = getDummySuggestion(airportCode)
        val destination = getDummySuggestion(airportCode)
        val startDate = LocalDate.now()
        val endDate = startDate.plusDays(2)
        val paramsBuilder = FlightSearchParams.Builder(26, 500)
                .tripType(FlightSearchParams.TripType.RETURN)
                .origin(origin)
                .destination(destination)
                .startDate(startDate)
                .adults(1) as FlightSearchParams.Builder

        if (roundTrip) {
            paramsBuilder.endDate(endDate)
            paramsBuilder.tripType(FlightSearchParams.TripType.RETURN)
        }
        return paramsBuilder.build()
    }

    fun flightSearchParamsBuilder(roundTrip: Boolean, airportCode: String = "happy"): FlightSearchParams.Builder {
        val origin = getDummySuggestion(airportCode)
        val destination = getDummySuggestion(airportCode)
        val startDate = LocalDate.now()
        val endDate = startDate.plusDays(2)
        val paramsBuilder = FlightSearchParams.Builder(26, 500)
                .origin(origin)
                .destination(destination)
                .startDate(startDate)
                .adults(1) as FlightSearchParams.Builder

        if (roundTrip) {
            paramsBuilder.endDate(endDate)
        }
        return paramsBuilder
    }

    fun addRichContentToFlightLeg(flightLeg: FlightLeg) {
        flightLeg.richContent = getRichContent()
    }

    fun getRichContent(): RichContent {
        val richContent = RichContent()
        richContent.legId = ""
        richContent.score = 7.9F
        richContent.legAmenities = getRichContentAmenities()
        richContent.scoreExpression = RichContentUtils.ScoreExpression.VERY_GOOD.name
        richContent.segmentAmenitiesList = listOf(getRichContentAmenities())
        return richContent
    }

    fun getRichContentAmenities(): RichContent.RichContentAmenity {
        val amenities = RichContent.RichContentAmenity()
        amenities.wifi = true
        amenities.entertainment = false
        amenities.power = true
        return amenities
    }

    private fun getDummySuggestion(airportCode: String): SuggestionV4 {
        val suggestion = SuggestionV4()
        suggestion.gaiaId = ""
        suggestion.regionNames = SuggestionV4.RegionNames()
        suggestion.regionNames.displayName = airportCode
        suggestion.regionNames.fullName = airportCode
        suggestion.regionNames.shortName = airportCode
        suggestion.hierarchyInfo = SuggestionV4.HierarchyInfo()
        suggestion.hierarchyInfo!!.airport = SuggestionV4.Airport()
        suggestion.hierarchyInfo!!.airport!!.airportCode = airportCode
        suggestion.hierarchyInfo!!.airport!!.multicity = "happy"
        return suggestion
    }
}
