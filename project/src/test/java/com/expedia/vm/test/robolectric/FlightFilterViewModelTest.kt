package com.expedia.vm.test.robolectric

import android.content.Context
import android.view.LayoutInflater
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.FlightFilter
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.BaseFlightFilterWidget
import com.expedia.bookings.widget.LabeledCheckableFilter
import com.expedia.vm.BaseFlightFilterViewModel
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.util.ArrayList
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class FlightFilterViewModelTest {
    var vm: BaseFlightFilterViewModel by Delegates.notNull()
    private var widget: BaseFlightFilterWidget by Delegates.notNull()
    private lateinit var mockAnalyticsProvider: AnalyticsProvider

    @Before
    fun before() {
        widget = LayoutInflater.from(getContext()).inflate(R.layout.flight_filter_widget_test, null) as BaseFlightFilterWidget
        widget.viewModelBase = BaseFlightFilterViewModel(getContext(), LineOfBusiness.FLIGHTS_V2)
        vm = widget.viewModelBase
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
    }

    private fun getContext(): Context = RuntimeEnvironment.application

    @Test
    fun sortByPrice() {
        vm.filteredList = getFlightList()
        vm.sortObserver.onNext(FlightFilter.Sort.PRICE)

        for (i in 1 until vm.filteredList.size) {
            val current = vm.filteredList.elementAt(i).packageOfferModel.price.packageTotalPrice.amount.toInt()
            val previous = vm.filteredList.elementAt(i - 1).packageOfferModel.price.packageTotalPrice.amount.toInt()
            assertTrue(current >= previous, "Expected $current >= $previous")
        }
    }

    @Test
    fun sortByDepartureTime() {
        vm.filteredList = getFlightList()
        vm.sortObserver.onNext(FlightFilter.Sort.DEPARTURE)

        for (i in 1 until vm.filteredList.size) {
            val current = DateTime.parse(vm.filteredList.elementAt(i).departureDateTimeISO)
            val previous = DateTime.parse(vm.filteredList.elementAt(i - 1).departureDateTimeISO)
            assertTrue(previous.isBefore(current), "Expected $current >= $previous")
        }
    }

    @Test
    fun sortByArrivalTime() {
        vm.filteredList = getFlightList()
        vm.sortObserver.onNext(FlightFilter.Sort.ARRIVAL)

        for (i in 1 until vm.filteredList.size) {
            val current = DateTime.parse(vm.filteredList.elementAt(i).arrivalDateTimeISO)
            val previous = DateTime.parse(vm.filteredList.elementAt(i - 1).arrivalDateTimeISO)
            assertTrue(previous.isBefore(current), "Expected $current >= $previous")
        }
    }

    @Test
    fun testDisabledStopsOption() {
        vm.filteredList = getFlightList()
        vm.flightResultsObservable.onNext(vm.filteredList)

        assertEquals(widget.stopsContainer.childCount, 1)

        val filterOption = widget.stopsContainer.getChildAt(0) as LabeledCheckableFilter<*>

        assertFalse(filterOption.isClickable)

        val checkbox = filterOption.checkBox

        assertFalse(checkbox.isEnabled)
        assertTrue(checkbox.isChecked)
    }

    @Test
    fun testDurationFilterInteractionIsTracked() {
        vm.durationFilterInteractionFromUser.onNext(Unit)
        OmnitureTestUtils.assertLinkTracked("Search Results Filter", "App.Flight.Search.Filter.Duration", mockAnalyticsProvider)

        // Reset mock analytics provider to test that tracking is done only once.
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        vm.durationFilterInteractionFromUser.onNext(Unit)
        vm.durationFilterInteractionFromUser.onNext(Unit)
        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)
    }

    @Test
    fun testDepartureTimeFilterInteractionIsTracked() {
        vm.departureRangeChangedObserver.onNext(Pair(1, 1))
        OmnitureTestUtils.assertLinkTracked("Search Results Filter", "App.Flight.Search.Filter.Time.Departure", mockAnalyticsProvider)

        // Reset mock analytics provider to test that tracking is done only once.
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        vm.departureRangeChangedObserver.onNext(Pair(2, 2))
        vm.departureRangeChangedObserver.onNext(Pair(3, 3))
        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)
    }

    @Test
    fun testArrivalTimeFilterInteractionIsTracked() {
        vm.arrivalRangeChangedObserver.onNext(Pair(1, 1))
        OmnitureTestUtils.assertLinkTracked("Search Results Filter", "App.Flight.Search.Filter.Time.Arrival", mockAnalyticsProvider)

        // Reset mock analytics provider to test that tracking is done only once.
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        vm.arrivalRangeChangedObserver.onNext(Pair(2, 2))
        vm.arrivalRangeChangedObserver.onNext(Pair(3, 3))
        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)
    }

    @Test
    fun testZeroResultAfterFilteringIsTracked() {
        vm.filteredList = getFlightList()
        vm.durationRangeChangedObserver.onNext(1)
        OmnitureTestUtils.assertLinkTracked("Zero results", "App.Flight.Search.Filter.ZeroResult", mockAnalyticsProvider)

        // Reset mock analytics provider to test that tracking is done only once.
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        vm.durationRangeChangedObserver.onNext(1)
        vm.durationRangeChangedObserver.onNext(1)
        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)
    }

    private fun getFlightList(): List<FlightLeg> {
        val list = ArrayList<FlightLeg>()
        val flightLeg1 = FlightLeg()
        flightLeg1.carrierName = "American Airlines"
        flightLeg1.elapsedDays = 1
        flightLeg1.durationHour = 19
        flightLeg1.durationMinute = 10
        flightLeg1.flightSegments = ArrayList<FlightLeg.FlightSegment>()
        flightLeg1.departureDateTimeISO = "2016-09-07T20:20:00.000-05:00"
        flightLeg1.arrivalDateTimeISO = "2016-09-08T19:20:00.000+01:00"
        flightLeg1.stopCount = 1
        flightLeg1.packageOfferModel = PackageOfferModel()
        flightLeg1.packageOfferModel.price = PackageOfferModel.PackagePrice()
        flightLeg1.packageOfferModel.price.packageTotalPrice = Money("200", "USD")

        val flightLeg2 = FlightLeg()
        flightLeg2.carrierName = "American Airlines"
        flightLeg2.durationHour = 19
        flightLeg2.durationMinute = 0
        flightLeg2.departureDateTimeISO = "2016-09-07T01:20:00.000-05:00"
        flightLeg2.arrivalDateTimeISO = "2016-09-07T20:20:00.000+01:00"
        flightLeg2.stopCount = 1
        flightLeg2.packageOfferModel = PackageOfferModel()
        flightLeg2.packageOfferModel.price = PackageOfferModel.PackagePrice()
        flightLeg2.packageOfferModel.price.packageTotalPrice = Money("300", "USD")

        val flightLeg3 = FlightLeg()
        flightLeg3.carrierName = "American Airlines"
        flightLeg3.durationHour = 18
        flightLeg3.durationMinute = 0
        flightLeg3.departureDateTimeISO = "2016-09-07T21:20:00.000-05:00"
        flightLeg3.arrivalDateTimeISO = "2016-09-08T08:20:00.000+01:00"
        flightLeg3.stopCount = 1
        flightLeg3.packageOfferModel = PackageOfferModel()
        flightLeg3.packageOfferModel.price = PackageOfferModel.PackagePrice()
        flightLeg3.packageOfferModel.price.packageTotalPrice = Money("220", "USD")

        list.add(flightLeg1)
        list.add(flightLeg2)
        list.add(flightLeg3)
        return list
    }
}
