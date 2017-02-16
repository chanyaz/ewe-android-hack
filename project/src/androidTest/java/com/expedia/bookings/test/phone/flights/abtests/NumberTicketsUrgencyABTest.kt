package com.expedia.bookings.test.phone.flights.abtests


import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.test.espresso.AbacusTestUtils
import com.expedia.bookings.test.espresso.EspressoUtils
import com.expedia.bookings.test.espresso.FlightTestCase
import com.expedia.bookings.test.phone.pagemodels.flights.FlightsSearchResultsScreen
import com.expedia.bookings.test.phone.pagemodels.flights.FlightsSearchScreen
import org.joda.time.LocalDate
import org.junit.Test

class NumberTicketsUrgencyABTest : FlightTestCase() {

    override fun runTest() {
        AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppFlightsNumberOfTicketsUrgencyTest, AbacusUtils.DefaultVariant.BUCKETED.ordinal)
        super.runTest()
    }

    @Test
    fun testTicketsRemainingUrgencyMessagingShown() {
        FlightsSearchScreen.clickDepartureAirportField()
        FlightsSearchScreen.enterDepartureAirport("SFO")
        FlightsSearchScreen.clickArrivalAirportField()
        FlightsSearchScreen.enterArrivalAirport("SEA")
        FlightsSearchScreen.clickSelectDepartureButton()
        val startDate = LocalDate.now().plusDays(35)
        val endDate = startDate.plusDays(3)
        FlightsSearchScreen.clickDate(startDate, endDate)
        FlightsSearchScreen.clickSearchButton()

        val firstOutboundFlightItem = FlightsSearchResultsScreen.listItem().atPosition(1)
        val numberTicketsLeftString = EspressoUtils.getListItemValues(firstOutboundFlightItem, R.id.number_tickets_left_view)
        assertEquals("1 left at", numberTicketsLeftString)
    }
}
