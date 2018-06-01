package com.expedia.bookings.presenter.shared

import android.app.Activity
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.Airline
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.services.FlightRichContentService
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.shadows.ShadowDateFormat
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.RichContentUtils
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.flights.DockedOutboundFlightWidgetV2
import com.expedia.bookings.widget.shared.AbstractFlightListAdapter
import com.expedia.vm.AbstractFlightViewModel
import com.expedia.vm.FlightResultsViewModel
import com.expedia.vm.flights.FlightViewModel
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockWebServer
import org.joda.time.LocalDate
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
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowDateFormat::class))
class FlightResultsListViewPresenterTest {
    val context = RuntimeEnvironment.application
    lateinit var sut: FlightResultsListViewPresenter
    lateinit var testFlightAdapter: AbstractFlightListAdapter
    private lateinit var flightSelectedSubject: PublishSubject<FlightLeg>
    private lateinit var isRoundTripSubject: BehaviorSubject<Boolean>
    private lateinit var activity: Activity
    private lateinit var kongServiceFlight: FlightRichContentService
    var server: MockWebServer = MockWebServer()
        @Rule get
    val FLIGHT_LEG_ID = "ab64aefca28e772ca024d4a00e6ae131"

    @Before
    fun setup() {
        val interceptor = MockInterceptor()
        val root = File("../lib/mocked/templates").canonicalPath
        val opener = FileSystemOpener(root)
        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        server.setDispatcher(ExpediaDispatcher(opener))
        kongServiceFlight = FlightRichContentService("http://localhost:" + server.port,
                OkHttpClient.Builder().addInterceptor(logger).build(),
                listOf(interceptor), Schedulers.trampoline(), Schedulers.trampoline())
        activity = Robolectric.buildActivity(AppCompatActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)

        flightSelectedSubject = PublishSubject.create<FlightLeg>()
        isRoundTripSubject = BehaviorSubject.create()
    }

    fun inflateAndSetViewModel() {
        sut = LayoutInflater.from(activity).inflate(R.layout.package_flight_results_presenter_stub, null) as FlightResultsListViewPresenter
        val flightResultsViewModel = FlightResultsViewModel(context)
        flightResultsViewModel.flightRichContentService = kongServiceFlight
        sut.resultsViewModel = flightResultsViewModel
        createTestFlightListAdapter()
        sut.setAdapter(testFlightAdapter)
    }

    private fun createTestFlightListAdapter() {
        isRoundTripSubject.onNext(false)
        testFlightAdapter = TestFlightListAdapter(activity, flightSelectedSubject, isRoundTripSubject)
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

    @Test @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testRichContentInFlightLeg() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppFlightsRichContent)
        inflateAndSetViewModel()
        val flightLeg = createFakeFlightLeg()
        sut.resultsViewModel.isOutboundResults.onNext(true)
        sut.resultsViewModel.flightResultsObservable.onNext(listOf(flightLeg))
        val processedFlightLeg = sut.resultsViewModel.flightResultsObservable.value[0]
        assertEquals(FLIGHT_LEG_ID, processedFlightLeg.legId)
        val flightRichContent = processedFlightLeg.richContent
        assertNotNull(flightRichContent)
        assertEquals(8.1F, flightRichContent.score)
        assertEquals(RichContentUtils.ScoreExpression.VERY_GOOD.name, flightRichContent.scoreExpression)

        val flightLegAmenities = flightRichContent.legAmenities
        assertTrue(flightLegAmenities!!.wifi)
        assertTrue(flightLegAmenities.entertainment)
        assertTrue(flightLegAmenities.power)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testProgressBarStatusForRichContent() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppFlightsRichContent)
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppFLightLoadingStateV1)
        inflateAndSetViewModel()
        addFlightSearchParams()
        val flightLeg = createFakeFlightLeg()
        sut.resultsViewModel.isOutboundResults.onNext(true)
        sut.setLoadingState()
        sut.resultsViewModel.flightResultsObservable.onNext(listOf(flightLeg))

        assertEquals(600, sut.flightProgressBar.progress)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testInboundFlightRichContent() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppFlightsRichContent, 3)
        inflateAndSetViewModel()

        val flightLeg = createFakeFlightLeg()
        sut.resultsViewModel.isOutboundResults.onNext(false)
        sut.resultsViewModel.flightResultsObservable.onNext(listOf(flightLeg))

        val processedFlightLeg = sut.resultsViewModel.flightResultsObservable.value[0]
        assertEquals(FLIGHT_LEG_ID, processedFlightLeg.legId)
    }

    private fun addFlightSearchParams() {
        val origin = getFakeSuggestion("SFO")
        val destination = getFakeSuggestion("SEA")
        val params = FlightSearchParams.Builder(100, 500)
                .origin(origin)
                .destination(destination)
                .startDate(LocalDate.now().withYear(2019).withMonthOfYear(9).withDayOfMonth(6))
                .endDate(LocalDate.now().withYear(2021).withMonthOfYear(9).withDayOfMonth(6))
                .adults(1).build() as FlightSearchParams
        Db.setFlightSearchParams(params)
    }

    private fun getFakeSuggestion(airportCode: String): SuggestionV4 {
        val suggestion = SuggestionV4()
        val hierarchyInfo = SuggestionV4.HierarchyInfo()
        val airport = SuggestionV4.Airport()
        airport.airportCode = airportCode
        hierarchyInfo.airport = airport
        val country = SuggestionV4.Country()
        country.name = ""
        hierarchyInfo.country = country
        suggestion.hierarchyInfo = hierarchyInfo

        val regionName = SuggestionV4.RegionNames()
        regionName.shortName = "San Francisco, CA (SFO-San Francisco Intl.)"
        regionName.displayName = "San Francisco, CA (<B>SFO</B>-San Francisco Intl.)"
        regionName.fullName = "San Francisco, CA, United States (<B>SFO</B>-San Francisco Intl.)"
        suggestion.regionNames = regionName
        return suggestion
    }

    private fun createFakeFlightLeg(): FlightLeg {
        val flightLeg = FlightLeg()
        val airline = Airline("United Airlines", "")

        flightLeg.legId = FLIGHT_LEG_ID
        flightLeg.naturalKey = ""
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

    private class TestFlightListAdapter(context: Context, flightSelectedSubject: PublishSubject<FlightLeg>, isRoundTripSearchSubject: BehaviorSubject<Boolean>) :
            AbstractFlightListAdapter(context, flightSelectedSubject, isRoundTripSearchSubject) {
        override fun getPriceDescriptorMessageIdForFSR(): Int? = null

        override fun isShowOnlyNonStopSearch(): Boolean = false

        override fun isShowOnlyRefundableSearch(): Boolean = false

        override fun showAllFlightsHeader(): Boolean = false

        override fun adjustPosition(): Int = 1

        override fun makeFlightViewModel(context: Context, flightLeg: FlightLeg): AbstractFlightViewModel {
            return FlightViewModel(context, flightLeg)
        }

        override fun showAdvanceSearchFilterHeader(): Boolean = true

        override fun getRoundTripStringResourceId(): Int = R.string.prices_roundtrip_label
    }
}
