package com.expedia.bookings.test

import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.flights.FlightSearchParams
import org.joda.time.LocalDate
import org.junit.Assert
import org.junit.Test
import java.util.ArrayList

class NewFlightSearchParamsTest {

    @Test
    fun testParamsValidation() {
        val builder = FlightSearchParams.Builder(26, 329)
        val expectedNumAdults = 2
        val expectedNumChildren = 2
		val expectedDepartureDate = LocalDate.now()
		val expectedReturnDate = LocalDate.now().plusDays(1)
        val expectedChildrenString = "3,5"
		val expectedOrigin = getDummySuggestion("San Francisco", "SFO")
		val expectedDestination = getDummySuggestion("Seattle", "SEA")

        val children = ArrayList<Int>()
        children.add(3)
        children.add(5)

        builder.children(children)
		Assert.assertEquals(false, builder.hasStart())
		Assert.assertEquals(false, builder.hasEnd())
		Assert.assertEquals(false, builder.hasOriginLocation())
		Assert.assertEquals(false, builder.hasDestinationLocation())
		Assert.assertEquals(false, builder.areRequiredParamsFilled())
		Assert.assertEquals(false, builder.hasValidDateDuration())

        builder.adults(expectedNumAdults)
		Assert.assertEquals(false, builder.hasStart())
		Assert.assertEquals(false, builder.hasEnd())
		Assert.assertEquals(false, builder.hasOriginLocation())
		Assert.assertEquals(false, builder.hasDestinationLocation())
		Assert.assertEquals(false, builder.areRequiredParamsFilled())
		Assert.assertEquals(false, builder.hasValidDateDuration())

		builder.startDate(expectedDepartureDate)
		Assert.assertEquals(true, builder.hasStart())
		Assert.assertEquals(false, builder.hasEnd())
		Assert.assertEquals(false, builder.hasOriginLocation())
		Assert.assertEquals(false, builder.hasDestinationLocation())
		Assert.assertEquals(false, builder.areRequiredParamsFilled())
		Assert.assertEquals(true, builder.hasValidDateDuration())

		builder.endDate(LocalDate.now().plusDays(400))
		Assert.assertEquals(false, builder.hasValidDateDuration())

		builder.endDate(expectedReturnDate)
		Assert.assertEquals(true, builder.hasStart())
		Assert.assertEquals(true, builder.hasEnd())
		Assert.assertEquals(false, builder.hasOriginLocation())
		Assert.assertEquals(false, builder.hasDestinationLocation())
		Assert.assertEquals(false, builder.areRequiredParamsFilled())
		Assert.assertEquals(true, builder.hasValidDateDuration())

		builder.origin(expectedOrigin)
		Assert.assertEquals(true, builder.hasStart())
		Assert.assertEquals(true, builder.hasEnd())
		Assert.assertEquals(true, builder.hasOriginLocation())
		Assert.assertEquals(false, builder.hasDestinationLocation())
		Assert.assertEquals(true, builder.areRequiredParamsFilled())
		Assert.assertEquals(true, builder.hasValidDateDuration())

		builder.destination(expectedDestination)
		Assert.assertEquals(true, builder.hasStart())
		Assert.assertEquals(true, builder.hasEnd())
		Assert.assertEquals(true, builder.hasOriginLocation())
		Assert.assertEquals(true, builder.hasDestinationLocation())
		Assert.assertEquals(true, builder.areRequiredParamsFilled())
		Assert.assertEquals(true, builder.hasValidDateDuration())

		val params = builder.build()
		Assert.assertEquals(expectedNumAdults.toLong(), params.adults.toLong())
		Assert.assertEquals(expectedNumChildren.toLong(), params.children.size.toLong())
		Assert.assertEquals(expectedDepartureDate, params.departureDate)
		Assert.assertEquals(expectedReturnDate, params.returnDate)
		Assert.assertEquals(expectedChildrenString, params.childrenString)
		Assert.assertEquals(expectedOrigin.hierarchyInfo?.airport, params.departureAirport.hierarchyInfo?.airport)
		Assert.assertEquals(expectedDestination.hierarchyInfo?.airport, params.arrivalAirport?.hierarchyInfo?.airport)
	}

	private fun getDummySuggestion(city: String, airport: String): SuggestionV4 {
		val suggestion = SuggestionV4()
		suggestion.gaiaId = ""
		suggestion.regionNames = SuggestionV4.RegionNames()
		suggestion.regionNames.displayName = city
		suggestion.regionNames.fullName = city
		suggestion.regionNames.shortName = city
		suggestion.hierarchyInfo = SuggestionV4.HierarchyInfo()
		suggestion.hierarchyInfo!!.airport = SuggestionV4.Airport()
		suggestion.hierarchyInfo!!.airport!!.airportCode = airport
		return suggestion
	}
}
