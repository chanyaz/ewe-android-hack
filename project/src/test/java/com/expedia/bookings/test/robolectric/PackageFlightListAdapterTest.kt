package com.expedia.bookings.test.robolectric

import com.expedia.bookings.data.Money
import com.expedia.bookings.data.flights.Airline
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.utils.Ui
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
import java.util.ArrayList
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class PackageFlightListAdapterTest {

    val context = RuntimeEnvironment.application
    lateinit var sut: PackageFlightListAdapter
    lateinit var flightSelectedSubject: PublishSubject<FlightLeg>
    lateinit var flightSearchViewModel: FlightSearchViewModel
    lateinit var flightLeg: FlightLeg

    @Before
    fun setup() {
        Ui.getApplication(context).defaultTravelerComponent()

        flightSelectedSubject = PublishSubject.create<FlightLeg>()

        val server = MockWebServer()
        val service = FlightServices("http://localhost:" + server.port,
                OkHttpClient.Builder().build(), MockInterceptor(),
                Schedulers.immediate(), Schedulers.immediate())
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
    fun getPackageFlightViewModel() {
        createSystemUnderTest()
        createExpectedFlightLeg()
        val packageFlightViewModel = sut.makeFlightViewModel(context, flightLeg)
        assertEquals(flightLeg, packageFlightViewModel.layover)
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