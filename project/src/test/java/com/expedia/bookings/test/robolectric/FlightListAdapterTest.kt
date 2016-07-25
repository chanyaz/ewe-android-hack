package com.expedia.bookings.test.robolectric


import android.widget.FrameLayout
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.flights.Airline
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.test.PointOfSaleTestConfiguration
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.flights.FlightListAdapter
import com.expedia.bookings.widget.shared.AbstractFlightListAdapter
import com.expedia.vm.FlightSearchViewModel
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject
import java.util.ArrayList
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightListAdapterTest {

    val context = RuntimeEnvironment.application
    lateinit var sut: FlightListAdapter
    lateinit var flightSelectedSubject: PublishSubject<FlightLeg>
    lateinit var flightSearchViewModel: FlightSearchViewModel
    lateinit var flightLeg: FlightLeg

    @Before
    fun setup() {
        flightSelectedSubject = PublishSubject.create<FlightLeg>()

        val server = MockWebServer()
        val service = FlightServices("http://localhost:" + server.port,
                OkHttpClient.Builder().build(), MockInterceptor(),
                Schedulers.immediate(), Schedulers.immediate())
        Ui.getApplication(context).defaultTravelerComponent()
        flightSearchViewModel = FlightSearchViewModel(context, service)
    }

    fun createSystemUnderTest() {
        sut = FlightListAdapter(context, flightSelectedSubject, flightSearchViewModel)
    }

    @Test
    fun allFlightsHeaderNotShownForFlightsLOB() {
        sut = FlightListAdapter(context, flightSelectedSubject, flightSearchViewModel)
        sut.setNewFlights(emptyList())

        val itemViewType = sut.getItemViewType(1)
        assertEquals(AbstractFlightListAdapter.ViewTypes.FLIGHT_CELL_VIEW.ordinal, itemViewType)
    }

    @Test
    fun flightResultsHeaderRoundTrip() {
        createSystemUnderTest()
        val headerViewHolder = createHeaderViewHolder()
        sut.onBindViewHolder(headerViewHolder, 0)
        assertEquals("Prices roundtrip per person", headerViewHolder.title.text)
    }

    @Test
    fun flightResultsHeaderOneWay() {
        createSystemUnderTest()
        flightSearchViewModel.isRoundTripSearchObservable.onNext(false)
        val headerViewHolder = createHeaderViewHolder()
        sut.onBindViewHolder(headerViewHolder, 0)
        assertEquals("Prices one-way per person", headerViewHolder.title.text)
    }

    @Test
    fun flightResultsHeaderOneWayMinPrice() {
        configurePointOfSale()
        createSystemUnderTest()
        flightSearchViewModel.isRoundTripSearchObservable.onNext(false)
        val headerViewHolder = createHeaderViewHolder()
        sut.onBindViewHolder(headerViewHolder, 0)
        assertEquals("Prices one-way, per person, from", headerViewHolder.title.text)
    }

    @Test
    fun flightResultsHeaderReturnMinPrice() {
        configurePointOfSale()
        createSystemUnderTest()
        val headerViewHolder = createHeaderViewHolder()
        sut.onBindViewHolder(headerViewHolder, 0)
        assertEquals("Prices roundtrip, per person, from", headerViewHolder.title.text)
    }

    @Test
    fun getFlightViewModel() {
        createSystemUnderTest()
        createExpectedFlightLeg()
        val flightViewModel = sut.makeFlightViewModel(context, flightLeg)
        assertEquals(flightLeg, flightViewModel.layover)
    }

    @Test
    fun adjustPosition() {
        createSystemUnderTest()
        assertEquals(1, sut.adjustPosition())
    }

    private fun configurePointOfSale() {
        PointOfSaleTestConfiguration.configurePointOfSale(context, "MockSharedData/pos_with_airline_payment_fees.json")
    }

    private fun createHeaderViewHolder(): AbstractFlightListAdapter.HeaderViewHolder {
        return sut.onCreateViewHolder(FrameLayout(context), AbstractFlightListAdapter.ViewTypes.PRICING_STRUCTURE_HEADER_VIEW.ordinal) as AbstractFlightListAdapter.HeaderViewHolder
    }

    private fun createExpectedFlightLeg() {
        flightLeg = FlightLeg()
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
