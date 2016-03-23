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
        val builder = FlightSearchParams.Builder(330)
        val expectedNumAdults = 2
        val expectedNumChildren = 2
		val expectedDepartureDate = LocalDate.now()
		val expectedReturnDate = LocalDate.now().plusDays(1)
        val expectedChildrenString = "3,5"
		val expectedDeparture = getDummySuggestion("San Francisco", "SFO")
		val expectedArrival = getDummySuggestion("Seattle", "SEA")

        val children = ArrayList<Int>()
        children.add(3)
        children.add(5)

        builder.children(children)
		Assert.assertEquals(false, builder.hasStart())
		Assert.assertEquals(false, builder.hasEnd())
		Assert.assertEquals(false, builder.hasDeparture())
		Assert.assertEquals(false, builder.hasArrival())
		Assert.assertEquals(false, builder.areRequiredParamsFilled())
		Assert.assertEquals(false, builder.hasValidDates())

        builder.adults(expectedNumAdults)
		Assert.assertEquals(false, builder.hasStart())
		Assert.assertEquals(false, builder.hasEnd())
		Assert.assertEquals(false, builder.hasDeparture())
		Assert.assertEquals(false, builder.hasArrival())
		Assert.assertEquals(false, builder.areRequiredParamsFilled())
		Assert.assertEquals(false, builder.hasValidDates())

		builder.checkIn(expectedDepartureDate)
		Assert.assertEquals(true, builder.hasStart())
		Assert.assertEquals(false, builder.hasEnd())
		Assert.assertEquals(false, builder.hasDeparture())
		Assert.assertEquals(false, builder.hasArrival())
		Assert.assertEquals(false, builder.areRequiredParamsFilled())
		Assert.assertEquals(true, builder.hasValidDates())

		builder.checkOut(LocalDate.now().plusDays(400))
		Assert.assertEquals(false, builder.hasValidDates())

		builder.checkOut(expectedReturnDate)
		Assert.assertEquals(true, builder.hasStart())
		Assert.assertEquals(true, builder.hasEnd())
		Assert.assertEquals(false, builder.hasDeparture())
		Assert.assertEquals(false, builder.hasArrival())
		Assert.assertEquals(false, builder.areRequiredParamsFilled())
		Assert.assertEquals(true, builder.hasValidDates())

		builder.departure(expectedDeparture)
		Assert.assertEquals(true, builder.hasStart())
		Assert.assertEquals(true, builder.hasEnd())
		Assert.assertEquals(true, builder.hasDeparture())
		Assert.assertEquals(false, builder.hasArrival())
		Assert.assertEquals(true, builder.areRequiredParamsFilled())
		Assert.assertEquals(true, builder.hasValidDates())

		builder.arrival(expectedArrival)
		Assert.assertEquals(true, builder.hasStart())
		Assert.assertEquals(true, builder.hasEnd())
		Assert.assertEquals(true, builder.hasDeparture())
		Assert.assertEquals(true, builder.hasArrival())
		Assert.assertEquals(true, builder.areRequiredParamsFilled())
		Assert.assertEquals(true, builder.hasValidDates())

		val params = builder.build()
		Assert.assertEquals(expectedNumAdults.toLong(), params.adults.toLong())
		Assert.assertEquals(expectedNumChildren.toLong(), params.children.size.toLong())
		Assert.assertEquals(expectedDepartureDate, params.departureDate)
		Assert.assertEquals(expectedReturnDate, params.returnDate)
		Assert.assertEquals(expectedChildrenString, params.getChildrenString())
		Assert.assertEquals(expectedDeparture.hierarchyInfo?.airport, params.departureAirport.hierarchyInfo?.airport)
		Assert.assertEquals(expectedArrival.hierarchyInfo?.airport, params.arrivalAirport?.hierarchyInfo?.airport)
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
