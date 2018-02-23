package com.expedia.bookings.test.robolectric

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.widget.BaseFlightFilterWidget
import com.expedia.bookings.widget.LabeledCheckableFilter
import com.expedia.bookings.widget.PriceProminenceFilterWithLogoAndCount
import com.expedia.vm.BaseFlightFilterViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.math.BigDecimal
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightFilterWidgetTest {
    var vm: BaseFlightFilterViewModel by Delegates.notNull()
    private var widget: BaseFlightFilterWidget by Delegates.notNull()

    @Before
    fun setup() {
        widget = LayoutInflater.from(getContext()).inflate(R.layout.flight_filter_widget_test, null) as BaseFlightFilterWidget
        setViewModel()
    }

    private fun getContext(): Context = RuntimeEnvironment.application

    @Test
    fun testStopsContainer() {
        vm.flightResultsObservable.onNext(getFlightList())
        val stopsContainer = widget.stopsContainer
        val firstStop = stopsContainer.getChildAt(0) as LabeledCheckableFilter<*>

        assertEquals(1, stopsContainer.childCount)
        assertEquals("1 Stop", firstStop.stopsLabel.text)
        assertEquals("3", firstStop.resultsLabel.text)
    }

    @Test
    fun testAirlineContainer() {
        vm.flightResultsObservable.onNext(getFlightList())
        val airlineContainer = widget.airlinesContainer
        val firstAirlineFilter = airlineContainer.getChildAt(0) as LabeledCheckableFilter<*>

        assertEquals(1, airlineContainer.childCount)
        assertEquals("American Airlines", firstAirlineFilter.stopsLabel.text)
        assertEquals("3", firstAirlineFilter.resultsLabel.text)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testStopsContainerForPriceVariant() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(getContext(), AbacusUtils.EBAndroidAppFlightsFiltersPriceAndLogo, 1)
        setViewModel()
        vm.flightResultsObservable.onNext(getFlightList())
        val stopsContainer = widget.stopsContainer
        val firstStop = stopsContainer.getChildAt(0) as PriceProminenceFilterWithLogoAndCount<*>

        assertEquals(1, stopsContainer.childCount)
        assertEquals(View.GONE, firstStop.logoImage.visibility)
        assertEquals("1 Stop", firstStop.stopsLabel.text)
        assertEquals("$200", firstStop.resultsLabel.text)
    }

    @Test
    fun testDynamicFeedbackWidget() {
        vm.flightResultsObservable.onNext(getFlightList())
        val dynamicFeedbackWidget = widget.dynamicFeedbackWidget
        assertEquals(View.GONE, dynamicFeedbackWidget.visibility)

        val dynamicCounter = dynamicFeedbackWidget.findViewById<TextView>(R.id.dynamic_feedback_counter)
        vm.selectStop.onNext(0)
        assertEquals(View.VISIBLE, dynamicFeedbackWidget.visibility)
        assertEquals("0 Results", dynamicCounter.text.toString())

        vm.selectStop.onNext(1)
        assertEquals(View.VISIBLE, dynamicFeedbackWidget.visibility)
        assertEquals("3 Results", dynamicCounter.text.toString())
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testDynamicFeedbackWidgetForPriceVariant() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(getContext(), AbacusUtils.EBAndroidAppFlightsFiltersPriceAndLogo, 1)
        setViewModel()
        vm.flightResultsObservable.onNext(getFlightList())
        val dynamicFeedbackWidget = widget.dynamicFeedbackWidget
        assertEquals(View.GONE, dynamicFeedbackWidget.visibility)

        val dynamicCounter = dynamicFeedbackWidget.findViewById<TextView>(R.id.dynamic_feedback_counter)
        vm.selectStop.onNext(0)
        assertEquals(View.VISIBLE, dynamicFeedbackWidget.visibility)
        assertEquals("0 Results", dynamicCounter.text.toString())

        vm.selectStop.onNext(1)
        assertEquals(View.VISIBLE, dynamicFeedbackWidget.visibility)
        assertEquals("3 Results from $200", dynamicCounter.text.toString())
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testAirlineContainerForPriceVariant() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(getContext(), AbacusUtils.EBAndroidAppFlightsFiltersPriceAndLogo, 1)
        setViewModel()
        vm.flightResultsObservable.onNext(getFlightList())
        val airlineContainer = widget.airlinesContainer
        val firstAirlineFilter = airlineContainer.getChildAt(0) as PriceProminenceFilterWithLogoAndCount<*>

        assertEquals(1, airlineContainer.childCount)
        assertEquals(View.VISIBLE, firstAirlineFilter.logoImage.visibility)
        assertEquals("American Airlines", firstAirlineFilter.stopsLabel.text)
        assertEquals("$200", firstAirlineFilter.resultsLabel.text)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testStopsContainerForPriceWithCountVariant() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(getContext(), AbacusUtils.EBAndroidAppFlightsFiltersPriceAndLogo, 2)
        setViewModel()
        vm.flightResultsObservable.onNext(getFlightList())
        val stopsContainer = widget.stopsContainer
        val firstStop = stopsContainer.getChildAt(0) as PriceProminenceFilterWithLogoAndCount<*>

        assertEquals(1, stopsContainer.childCount)
        assertEquals(View.GONE, firstStop.logoImage.visibility)
        assertEquals("1 Stop", firstStop.stopsLabel.text)
        assertEquals("$200", firstStop.resultsLabel.text)
        assertEquals(View.VISIBLE, firstStop.countLabel.visibility)
        assertEquals("3 results", firstStop.countLabel.text)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testAirlineContainerForPriceWithCountVariant() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(getContext(), AbacusUtils.EBAndroidAppFlightsFiltersPriceAndLogo, 2)
        setViewModel()
        vm.flightResultsObservable.onNext(getFlightList())
        val airlineContainer = widget.airlinesContainer
        val firstAirlineFilter = airlineContainer.getChildAt(0) as PriceProminenceFilterWithLogoAndCount<*>

        assertEquals(1, airlineContainer.childCount)
        assertEquals(View.VISIBLE, firstAirlineFilter.logoImage.visibility)
        assertEquals("American Airlines", firstAirlineFilter.stopsLabel.text)
        assertEquals("$200", firstAirlineFilter.resultsLabel.text)
        assertEquals(View.VISIBLE, firstAirlineFilter.countLabel.visibility)
        assertEquals("3 results", firstAirlineFilter.countLabel.text)
    }

    fun setViewModel() {
        widget.viewModelBase = BaseFlightFilterViewModel(getContext(), LineOfBusiness.FLIGHTS_V2)
        vm = widget.viewModelBase
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
        flightLeg1.packageOfferModel.price.averageTotalPricePerTicket = Money("200", "USD")
        flightLeg1.packageOfferModel.price.averageTotalPricePerTicket.roundedAmount = BigDecimal(220)

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
        flightLeg2.packageOfferModel.price.averageTotalPricePerTicket = Money("300", "USD")
        flightLeg2.packageOfferModel.price.averageTotalPricePerTicket.roundedAmount = BigDecimal(300)

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
        flightLeg3.packageOfferModel.price.averageTotalPricePerTicket = Money("220", "USD")
        flightLeg3.packageOfferModel.price.averageTotalPricePerTicket.roundedAmount = BigDecimal(200)

        list.add(flightLeg1)
        list.add(flightLeg2)
        list.add(flightLeg3)
        return list
    }
}
