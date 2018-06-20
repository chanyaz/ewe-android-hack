package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.flights.utils.FlightServicesManager
import com.expedia.bookings.interceptors.MockInterceptor
import com.expedia.bookings.presenter.flight.FlightOutboundPresenter
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import com.expedia.util.Optional
import com.expedia.bookings.flights.vm.FlightOffersViewModel
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import io.reactivex.schedulers.Schedulers
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockWebServer
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import java.io.File
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightOutboundPresenterTest {
    private lateinit var activity: Activity
    private lateinit var flightOutboundPresenter: FlightOutboundPresenter
    private lateinit var service: FlightServices
    var server: MockWebServer = MockWebServer()
        @Rule get
    val FLIGHT_LEG_ID = "ab64aefca28e772ca024d4a00e6ae131"
    private lateinit var mockAnalyticsProvider: AnalyticsProvider

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        Ui.getApplication(activity).defaultTravelerComponent()
        Ui.getApplication(activity).defaultFlightComponents()

        val logger = HttpLoggingInterceptor()
        val root = File("../lib/mocked/templates").canonicalPath
        val opener = FileSystemOpener(root)
        val interceptor = MockInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        server.setDispatcher(ExpediaDispatcher(opener))
        service = FlightServices("http://localhost:" + server.port,
                okhttp3.OkHttpClient.Builder().addInterceptor(logger).build(),
                listOf(interceptor), Schedulers.trampoline(), Schedulers.trampoline())

        flightOutboundPresenter = LayoutInflater.from(activity).inflate(R.layout.flight_outbound_stub, null) as FlightOutboundPresenter
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
    }

    @Test
    fun widgetVisibilityTest() {
        val toolbar = flightOutboundPresenter.findViewById<View>(R.id.flights_toolbar) as Toolbar
        assertEquals(toolbar.visibility, View.VISIBLE)
    }

    @Test
    fun testFlightOutboundTitle() {
        flightOutboundPresenter.toolbarViewModel.refreshToolBar.onNext(true)
        flightOutboundPresenter.toolbarViewModel.isOutboundSearch.onNext(true)
        flightOutboundPresenter.toolbarViewModel.travelers.onNext(1)
        flightOutboundPresenter.toolbarViewModel.date.onNext(LocalDate.now())
        val regionName = SuggestionV4.RegionNames()
        regionName.shortName = "Bengaluru, India (BLR - Kempegowda Intl.)"
        regionName.displayName = "Bengaluru, India (BLR - Kempegowda Intl.)<I><B> near </B></I>Bangalore Palace, Bengaluru, India"
        flightOutboundPresenter.toolbarViewModel.regionNames.onNext(Optional(regionName))
        flightOutboundPresenter.toolbarViewModel.country.onNext(Optional("India"))
        flightOutboundPresenter.toolbarViewModel.airport.onNext(Optional("BLR"))
        assertEquals("Select flight to Bengaluru, India", flightOutboundPresenter.toolbar.title.toString())
    }

    @Test
    fun testPaymentFeeMayApplyVisibility() {
        invokeSetupComplete()
        assertEquals(View.GONE, flightOutboundPresenter.detailsPresenter.paymentFeesMayApplyTextView.visibility)
    }

    @Test
    fun testAbortRichContentOnBack() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(activity, AbacusUtils.EBAndroidAppFlightsRichContent)
        invokeSetupComplete()
        val testSubscriber = TestObserver<Unit>()
        flightOutboundPresenter.resultsPresenter.resultsViewModel.abortRichContentOutboundObservable.subscribe(testSubscriber)
        flightOutboundPresenter.back()
    }

    @Test
    fun testOmnitureForRichContentAmenities() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(activity, AbacusUtils.EBAndroidAppFlightsRichContent)
        val flightLeg = FlightTestUtil.getFlightLeg(FLIGHT_LEG_ID)
        flightLeg.richContent = FlightTestUtil.getRichContent(FLIGHT_LEG_ID, true, false, true)
        Db.setFlightSearchParams(FlightTestUtil.getFlightSearchParams(true, true))
        invokeSetupComplete()
        flightOutboundPresenter.trackFlightOverviewLoad(flightLeg)
        OmnitureTestUtils.assertStateTracked("App.Flight.Search.Roundtrip.Out.Details", OmnitureMatchers.withProps(mapOf(45 to "Wifi,Power")), mockAnalyticsProvider)
    }

    @Test
    fun testOmnitureForRichContentAmenitiesEnterOnly() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(activity, AbacusUtils.EBAndroidAppFlightsRichContent)
        val flightLeg = FlightTestUtil.getFlightLeg(FLIGHT_LEG_ID)
        flightLeg.richContent = FlightTestUtil.getRichContent(FLIGHT_LEG_ID, false, true, false)
        Db.setFlightSearchParams(FlightTestUtil.getFlightSearchParams(true, true))
        invokeSetupComplete()
        flightOutboundPresenter.trackFlightOverviewLoad(flightLeg)
        OmnitureTestUtils.assertStateTracked("App.Flight.Search.Roundtrip.Out.Details", OmnitureMatchers.withProps(mapOf(45 to "Enter")), mockAnalyticsProvider)
    }

    @Test
    fun testOmnitureForRichContentNoAmenities() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(activity, AbacusUtils.EBAndroidAppFlightsRichContent)
        val flightLeg = FlightTestUtil.getFlightLeg(FLIGHT_LEG_ID)
        flightLeg.richContent = FlightTestUtil.getRichContent(FLIGHT_LEG_ID, false, false, false)
        Db.setFlightSearchParams(FlightTestUtil.getFlightSearchParams(true, true))
        invokeSetupComplete()
        flightOutboundPresenter.trackFlightOverviewLoad(flightLeg)
        OmnitureTestUtils.assertStateTracked("App.Flight.Search.Roundtrip.Out.Details", OmnitureMatchers.withProps(mapOf(45 to "None")), mockAnalyticsProvider)
    }

    private fun invokeSetupComplete() {
        flightOutboundPresenter.flightOfferViewModel = FlightOffersViewModel(activity, FlightServicesManager(service))
        flightOutboundPresenter.flightOfferViewModel.searchParamsObservable.onNext(getSearchParams(true).build())
        flightOutboundPresenter.setupComplete()
    }

    private fun getSearchParams(roundTrip: Boolean): FlightSearchParams.Builder {
        val origin = getDummySuggestion()
        val destination = getDummySuggestion()
        val startDate = LocalDate.now()
        val endDate = startDate.plusDays(2)
        val paramsBuilder = FlightSearchParams.Builder(26, 500)
                .origin(origin)
                .destination(destination)
                .startDate(startDate)
                .adults(1) as FlightSearchParams.Builder
        paramsBuilder.flightCabinClass("coach")

        if (roundTrip) {
            paramsBuilder.endDate(endDate)
        }
        return paramsBuilder
    }

    private fun getDummySuggestion(): SuggestionV4 {
        val suggestion = SuggestionV4()
        suggestion.gaiaId = ""
        suggestion.regionNames = SuggestionV4.RegionNames()
        suggestion.regionNames.displayName = ""
        suggestion.regionNames.fullName = ""
        suggestion.regionNames.shortName = ""
        suggestion.hierarchyInfo = SuggestionV4.HierarchyInfo()
        suggestion.hierarchyInfo!!.airport = SuggestionV4.Airport()
        suggestion.hierarchyInfo!!.airport!!.airportCode = ""
        return suggestion
    }
}
