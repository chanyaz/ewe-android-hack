package com.expedia.bookings.presenter.shared

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.flights.Airline
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.KongFlightServices
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.shadows.ShadowDateFormat
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.flights.DockedOutboundFlightWidgetV2
import com.expedia.vm.FlightResultsViewModel
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File
import java.math.BigDecimal
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowDateFormat::class))
class FlightResultsListViewPresenterTest {
    val context = RuntimeEnvironment.application
    lateinit var sut: FlightResultsListViewPresenter
    private lateinit var activity: Activity
    private lateinit var kongService: KongFlightServices
    var server: MockWebServer = MockWebServer()
        @Rule get

    @Before
    fun setup() {
        val interceptor = MockInterceptor()
        val root = File("../lib/mocked/templates").canonicalPath
        val opener = FileSystemOpener(root)
        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        server.setDispatcher(ExpediaDispatcher(opener))
        kongService = KongFlightServices("http://localhost:" + server.port,
                OkHttpClient.Builder().addInterceptor(logger).build(),
                listOf(interceptor), Schedulers.trampoline(), Schedulers.trampoline())
        activity = Robolectric.buildActivity(AppCompatActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
    }

    fun inflateAndSetViewModel() {
        sut = LayoutInflater.from(activity).inflate(R.layout.package_flight_results_presenter_stub, null) as FlightResultsListViewPresenter
        val flightResultsViewModel = FlightResultsViewModel(context)
        flightResultsViewModel.kongFlightServices = kongService
        sut.resultsViewModel = flightResultsViewModel
    }

    @Test @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.VOYAGES, MultiBrand.TRAVELOCITY, MultiBrand.CHEAPTICKETS))
    fun testDockedOutboundFlightV2() {
        inflateAndSetViewModel()
        val flightLeg = createFakeFlightLeg()
        sut.outboundFlightSelectedSubject.onNext(flightLeg)

        assertEquals(sut.dockedOutboundFlightSelection::class.java, DockedOutboundFlightWidgetV2::class.java)

        val dockedOutboundWidget = sut.dockedOutboundFlightSelection as DockedOutboundFlightWidgetV2
        val airlineNameTextView = dockedOutboundWidget.airlineNameTextView
        val arrivalDepartureTimeTextView = dockedOutboundWidget.arrivalDepartureTimeTextView
        val pricePerPersonTextView = dockedOutboundWidget.pricePerPersonTextView
        val outboundLabelTextView = dockedOutboundWidget.findViewById<TextView>(R.id.outbound_flight_label)

        assertEquals(View.VISIBLE, airlineNameTextView.visibility)
        assertEquals(View.VISIBLE, arrivalDepartureTimeTextView.visibility)
        assertEquals(View.VISIBLE, pricePerPersonTextView.visibility)
        assertEquals(View.VISIBLE, outboundLabelTextView.visibility)

        assertEquals("United Airlines", airlineNameTextView.text )
        assertEquals("1:10 am - 12:20 pm +1d (13h 59m)", arrivalDepartureTimeTextView.text )
        assertEquals("$1,201", pricePerPersonTextView.text )
        assertEquals("Outbound flight:", outboundLabelTextView.text )
    }

    private fun createFakeFlightLeg(): FlightLeg {
        val flightLeg = FlightLeg()
        val airline = Airline("United Airlines", "")

        flightLeg.airlines = listOf(airline)
        flightLeg.durationHour = 13
        flightLeg.durationMinute = 59
        flightLeg.stopCount = 1
        flightLeg.departureDateTimeISO = "2016-03-09T01:10:00.000-05:00"
        flightLeg.arrivalDateTimeISO = "2016-03-10T12:20:00.000-07:00"
        flightLeg.elapsedDays = 1
        flightLeg.packageOfferModel = PackageOfferModel()
        flightLeg.packageOfferModel.urgencyMessage = PackageOfferModel.UrgencyMessage()
        flightLeg.packageOfferModel.price = PackageOfferModel.PackagePrice()
        flightLeg.packageOfferModel.price.differentialPriceFormatted = "$646.00"
        flightLeg.packageOfferModel.price.pricePerPersonFormatted = "$646.00"
        flightLeg.packageOfferModel.price.averageTotalPricePerTicket = Money("1200.90", "USD")
        flightLeg.packageOfferModel.price.averageTotalPricePerTicket.roundedAmount = BigDecimal("1200.90")

        return flightLeg
    }
}
