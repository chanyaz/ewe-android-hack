package com.expedia.bookings.test.robolectric


import android.widget.FrameLayout
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.test.PointOfSaleTestConfiguration
import com.expedia.bookings.widget.flights.FlightListAdapter
import com.expedia.bookings.widget.packages.PackageFlightListAdapter
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
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightListAdapterTest {

    val context = RuntimeEnvironment.application
    lateinit var sut: AbstractFlightListAdapter
    lateinit var flightSelectedSubject: PublishSubject<FlightLeg>
    lateinit var flightSearchViewModel: FlightSearchViewModel

    @Before
    fun setup() {
        flightSelectedSubject = PublishSubject.create<FlightLeg>()

        val server = MockWebServer()
        val service = FlightServices("http://localhost:" + server.port,
                OkHttpClient.Builder().build(), MockInterceptor(),
                Schedulers.immediate(), Schedulers.immediate())
        flightSearchViewModel = FlightSearchViewModel(context, service)
    }

    fun createSystemUnderTest() {
        sut = FlightListAdapter(context, flightSelectedSubject, flightSearchViewModel)
    }

    @Test
    fun allFlightsHeaderShownForPackagesLOB() {
        val sut = PackageFlightListAdapter(context, flightSelectedSubject, true)
        sut.shouldShowBestFlight = true

        val itemViewType = sut.getItemViewType(2)
        assertEquals(AbstractFlightListAdapter.ViewTypes.ALL_FLIGHTS_HEADER_VIEW.ordinal, itemViewType)
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

    private fun configurePointOfSale() {
        PointOfSaleTestConfiguration.configurePointOfSale(context, "MockSharedData/pos_with_airline_payment_fees.json")
    }

    private fun createHeaderViewHolder(): AbstractFlightListAdapter.HeaderViewHolder {
        return sut.onCreateViewHolder(FrameLayout(context), AbstractFlightListAdapter.ViewTypes.PRICING_STRUCTURE_HEADER_VIEW.ordinal) as AbstractFlightListAdapter.HeaderViewHolder
    }
}
