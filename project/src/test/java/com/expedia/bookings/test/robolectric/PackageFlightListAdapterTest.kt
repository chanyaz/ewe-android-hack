package com.expedia.bookings.test.robolectric

import android.app.Application
import android.widget.FrameLayout
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.flights.Airline
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.PointOfSaleTestConfiguration
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.packages.PackageFlightListAdapter
import com.expedia.bookings.widget.shared.AbstractFlightListAdapter
import com.expedia.vm.FlightSearchViewModel
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.util.ArrayList
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class PackageFlightListAdapterTest {

    val context: Application = RuntimeEnvironment.application
    lateinit var sut: PackageFlightListAdapter
    private lateinit var flightSelectedSubject: PublishSubject<FlightLeg>
    private lateinit var flightSearchViewModel: FlightSearchViewModel
    lateinit var flightLeg: FlightLeg

    @Before
    fun setup() {
        Ui.getApplication(context).defaultTravelerComponent()

        flightSelectedSubject = PublishSubject.create<FlightLeg>()
        flightSearchViewModel = FlightSearchViewModel(context)
    }

    fun createSystemUnderTest() {
        sut = PackageFlightListAdapter(context, flightSelectedSubject, true)
    }

    @Test
    fun allFlightsLoadingHeaderView() {
        createSystemUnderTest()
        val itemViewType = sut.getItemViewType(0)
        assertEquals(AbstractFlightListAdapter.ViewTypes.LOADING_FLIGHTS_HEADER_VIEW.ordinal, itemViewType)
    }

    @Test
    fun allFlightsLoadingView() {
        createSystemUnderTest()
        val itemViewType = sut.getItemViewType(2)
        assertEquals(AbstractFlightListAdapter.ViewTypes.LOADING_FLIGHTS_VIEW.ordinal, itemViewType)
    }

    @Test
    fun allFlightsHeaderShownForPackagesLOB() {
        createSystemUnderTest()
        sut.setNewFlights(emptyList())
        sut.shouldShowBestFlight = true
        val itemViewType = sut.getItemViewType(2)
        assertEquals(AbstractFlightListAdapter.ViewTypes.ALL_FLIGHTS_HEADER_VIEW.ordinal, itemViewType)
    }

    @Test
    fun testPricingStructureHeaderShownForPackagesLOB() {
        createSystemUnderTest()
        sut.setNewFlights(emptyList())
        sut.shouldShowBestFlight = true
        val itemViewType = sut.getItemViewType(0)
        assertEquals(AbstractFlightListAdapter.ViewTypes.PRICING_STRUCTURE_HEADER_VIEW.ordinal, itemViewType)
    }

    @Test
    fun testFlightCellViewShownForPackagesLOB() {
        createSystemUnderTest()
        sut.setNewFlights(emptyList())
        sut.shouldShowBestFlight = false
        val itemViewType = sut.getItemViewType(2)
        assertEquals(AbstractFlightListAdapter.ViewTypes.FLIGHT_CELL_VIEW.ordinal, itemViewType)
    }

    @Test
    fun testBestFlightViewShownForPackagesLOB() {
        createSystemUnderTest()
        sut.setNewFlights(emptyList())
        sut.shouldShowBestFlight = true
        val itemViewType = sut.getItemViewType(1)
        assertEquals(AbstractFlightListAdapter.ViewTypes.BEST_FLIGHT_VIEW.ordinal, itemViewType)
    }

    @Test
    fun testFlightCellViewShownWhenShowBestFlightIsTrue() {
        createSystemUnderTest()
        sut.setNewFlights(emptyList())
        sut.shouldShowBestFlight = true
        val itemViewType = sut.getItemViewType(3)
        assertEquals(AbstractFlightListAdapter.ViewTypes.FLIGHT_CELL_VIEW.ordinal, itemViewType)
    }

    @Test
    fun testLoadingFlightsViewShownWhenChangePackageSearchIsFalse() {
        sut = PackageFlightListAdapter(context, flightSelectedSubject, false)
        sut.shouldShowBestFlight = true
        val itemViewType = sut.getItemViewType(3)
        assertEquals(AbstractFlightListAdapter.ViewTypes.LOADING_FLIGHTS_VIEW.ordinal, itemViewType)
    }

    @Test
    fun testFlightCellViewShownWhenBestFlightIsFalse() {
        createSystemUnderTest()
        createExpectedFlightLeg()
        sut.setNewFlights(listOf(flightLeg))
        sut.shouldShowBestFlight = true
        val itemViewType = sut.getItemViewType(3)
        assertEquals(AbstractFlightListAdapter.ViewTypes.FLIGHT_CELL_VIEW.ordinal, itemViewType)
    }

    @Test
    fun testFlightCellViewShownWhenBestFlightIsTrue() {
        createSystemUnderTest()
        createExpectedFlightLeg()
        flightLeg.isBestFlight = true
        sut.setNewFlights(listOf(flightLeg))
        sut.shouldShowBestFlight = true
        val itemViewType = sut.getItemViewType(3)
        assertEquals(AbstractFlightListAdapter.ViewTypes.FLIGHT_CELL_VIEW.ordinal, itemViewType)
    }

    @Test
    fun testFlightCellViewShownWhenChangePackageSearchIsFalseAndBestFlightIsFalse() {
        sut = PackageFlightListAdapter(context, flightSelectedSubject, false)
        createExpectedFlightLeg()
        sut.setNewFlights(listOf(flightLeg))
        sut.shouldShowBestFlight = true
        val itemViewType = sut.getItemViewType(3)
        assertEquals(AbstractFlightListAdapter.ViewTypes.FLIGHT_CELL_VIEW.ordinal, itemViewType)
    }

    @Test
    fun testFlightCellViewShownWhenChangePackageSearchIsFalseAndBestFlightIsTrue() {
        sut = PackageFlightListAdapter(context, flightSelectedSubject, false)
        createExpectedFlightLeg()
        flightLeg.isBestFlight = true
        sut.setNewFlights(listOf(flightLeg))
        sut.shouldShowBestFlight = true
        val itemViewType = sut.getItemViewType(3)
        assertEquals(AbstractFlightListAdapter.ViewTypes.FLIGHT_CELL_VIEW.ordinal, itemViewType)
    }

    @Test
    fun testFlightCellViewShownWhenFlightsSizeIsTwoAndBestFlightIsFalse() {
        sut = PackageFlightListAdapter(context, flightSelectedSubject, false)
        createExpectedFlightLeg()
        sut.setNewFlights(arrayListOf(flightLeg, flightLeg))
        sut.shouldShowBestFlight = true
        val itemViewType = sut.getItemViewType(3)
        assertEquals(AbstractFlightListAdapter.ViewTypes.FLIGHT_CELL_VIEW.ordinal, itemViewType)
    }

    @Test
    fun testFlightCellViewShownWhenFlightsSizeIsTwoAndBestFlightIsTrue() {
        sut = PackageFlightListAdapter(context, flightSelectedSubject, false)
        createExpectedFlightLeg()
        flightLeg.isBestFlight = true
        sut.setNewFlights(arrayListOf(flightLeg, flightLeg))
        sut.shouldShowBestFlight = true
        val itemViewType = sut.getItemViewType(3)
        assertEquals(AbstractFlightListAdapter.ViewTypes.FLIGHT_CELL_VIEW.ordinal, itemViewType)
    }

    @Test
    fun getPackageFlightViewModel() {
        createSystemUnderTest()
        createExpectedFlightLeg()
        val packageFlightViewModel = sut.makeFlightViewModel(context, flightLeg)
        assertEquals(flightLeg, packageFlightViewModel.layover)
    }

    @Test
    @RunForBrands(brands = [(MultiBrand.EXPEDIA)])
    fun flightResultsHeader() {
        createSystemUnderTest()
        val headerViewHolder = createHeaderViewHolder()
        sut.onBindViewHolder(headerViewHolder, 0)
        assertEquals("Roundtrip price per person", headerViewHolder.priceHeader.text)

        val initialPOSID = PointOfSale.getPointOfSale().pointOfSaleId
        setPointOfSale(PointOfSaleId.JAPAN)
        sut.onBindViewHolder(headerViewHolder, 0)
        assertEquals("Total price roundtrip (including taxes and fees), per person - for hotel and flights", headerViewHolder.priceHeader.text)
        setPointOfSale(initialPOSID)
    }

    @Test
    @RunForBrands(brands = [(MultiBrand.EXPEDIA)])
    fun testflightResultsHeaderForUKPointOfSale() {
        createSystemUnderTest()
        val headerViewHolder = createHeaderViewHolder()
        sut.onBindViewHolder(headerViewHolder, 0)
        assertEquals("Roundtrip price per person", headerViewHolder.priceHeader.text)

        val initialPOSID = PointOfSale.getPointOfSale().pointOfSaleId
        setPointOfSale(PointOfSaleId.UNITED_KINGDOM)
        sut.onBindViewHolder(headerViewHolder, 0)
        assertEquals("Price per person for hotel and roundtrip flight from…", headerViewHolder.priceHeader.text)
        setPointOfSale(initialPOSID)
    }

    @Test
    @RunForBrands(brands = [(MultiBrand.EXPEDIA)])
    fun testWhenBestFlightViewHolder() {
        createSystemUnderTest()
        createExpectedFlightLeg()
        flightLeg.flightSegments = emptyList()
        sut.setNewFlights(listOf(flightLeg))
        val headerViewHolder = sut.onCreateViewHolder(FrameLayout(context), AbstractFlightListAdapter.ViewTypes.BEST_FLIGHT_VIEW.ordinal) as PackageFlightListAdapter.BestFlightViewHolder
        sut.onBindViewHolder(headerViewHolder, 0)
        assertEquals(-1, headerViewHolder.itemId)
    }

    private fun createHeaderViewHolder(): AbstractFlightListAdapter.HeaderViewHolder {
        return sut.onCreateViewHolder(FrameLayout(context), AbstractFlightListAdapter.ViewTypes.PRICING_STRUCTURE_HEADER_VIEW.ordinal) as AbstractFlightListAdapter.HeaderViewHolder
    }

    private fun setPointOfSale(posId: PointOfSaleId) {
        PointOfSaleTestConfiguration.configurePOS(context, "ExpediaSharedData/ExpediaPointOfSaleConfig.json", Integer.toString(posId.id), false)
    }

    private fun createExpectedFlightLeg() {
        flightLeg = FlightLeg()
        flightLeg.flightSegments = ArrayList()
        flightLeg.elapsedDays = 1
        flightLeg.durationHour = 19
        flightLeg.durationMinute = 10
        flightLeg.departureTimeShort = "1:10AM"
        flightLeg.arrivalTimeShort = "12:20PM"
        flightLeg.departureDateTimeISO = "2016-07-10T01:10:00.000-05:00"
        flightLeg.arrivalDateTimeISO = "2016-07-10T12:20:00.000-07:00"
        flightLeg.stopCount = 1
        flightLeg.packageOfferModel = PackageOfferModel()
        flightLeg.packageOfferModel.price = PackageOfferModel.PackagePrice()
        flightLeg.packageOfferModel.price.packageTotalPrice = Money("111", "USD")
        flightLeg.packageOfferModel.price.deltaPositive = true
        flightLeg.packageOfferModel.price.differentialPriceFormatted = "$11"
        flightLeg.packageOfferModel.price.pricePerPersonFormatted = "200.0"
        val airlines = ArrayList<Airline>()
        val airline1 = Airline("United", null)
        val airline2 = Airline("Delta", null)
        airlines.add(airline1)
        airlines.add(airline2)
        flightLeg.airlines = airlines
    }
}
