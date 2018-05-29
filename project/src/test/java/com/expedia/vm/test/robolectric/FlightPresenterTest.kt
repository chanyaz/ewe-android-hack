package com.expedia.vm.test.robolectric

import android.app.Activity
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.activity.PlaygroundActivity
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.presenter.flight.FlightPresenter
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.tracking.AbstractSearchTrackingData
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.tracking.flight.FlightSearchTrackingData
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.flights.FlightListAdapter
import org.hamcrest.Matchers
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class))
class FlightPresenterTest {

    lateinit var activity: Activity
    private lateinit var flightPresenter: FlightPresenter
    private lateinit var mockAnalyticsProvider: AnalyticsProvider

    @Before
    fun setup() {
        Ui.getApplication(RuntimeEnvironment.application).defaultFlightComponents()
        Ui.getApplication(RuntimeEnvironment.application).defaultTravelerComponent()
        val intent = PlaygroundActivity.createIntent(RuntimeEnvironment.application, R.layout.flight_activity)
        val styledIntent = PlaygroundActivity.addTheme(intent, R.style.V2_Theme_Packages)
        activity = Robolectric.buildActivity(PlaygroundActivity::class.java, styledIntent).create().visible().get()
        Db.setFlightSearchParams(setupFlightSearchParams())
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testPageLoadTrackingForInboundSearchCall() {
        val mockPerfData = Mockito.mock(AbstractSearchTrackingData.PerformanceData::class.java)
        Mockito.`when`(mockPerfData.getPageLoadTime()).thenReturn("1.0")
        flightPresenter = LayoutInflater.from(activity).inflate(R.layout.flight_activity, null) as FlightPresenter
        flightPresenter.flightOfferViewModel.isRoundTripSearchSubject.onNext(false)

        val flightLeg = FlightLeg()
        flightLeg.legId = "leg1"
        flightPresenter.outBoundPresenter.detailsPresenter.vm.selectedFlightClickedSubject.onNext(flightLeg)
        assertEquals(flightPresenter.searchTrackingBuilder.paramsPopulated, true)

        flightPresenter.inboundPresenter.flightOfferViewModel.inboundResultsObservable.onNext(listOf(flightLeg))
        assertEquals(flightPresenter.searchTrackingBuilder.responsePopulated, true)

        (flightPresenter.inboundPresenter.resultsPresenter.recyclerView.adapter as FlightListAdapter).allViewsLoadedTimeObservable.onNext(Unit)
        OmnitureTestUtils.assertStateTracked("App.Flight.Search.Roundtrip.In", Matchers.allOf(), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testResultsTrackEventsPageUsable() {
        val trackingData = FlightSearchTrackingData()
        val mockPerfData = Mockito.mock(AbstractSearchTrackingData.PerformanceData::class.java)
        Mockito.`when`(mockPerfData.getPageLoadTime()).thenReturn("1.0")
        trackingData.performanceData = mockPerfData

        OmnitureTracking.trackResultInBoundFlights(trackingData, Pair(1, 2))
        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withEventsString("event220,event221=1.0"), mockAnalyticsProvider)
    }

    private fun setupFlightSearchParams(): FlightSearchParams {
        val departureSuggestion = SuggestionV4()
        departureSuggestion.gaiaId = "1234"
        val departureRegionNames = SuggestionV4.RegionNames()
        departureRegionNames.displayName = "San Francisco"
        departureRegionNames.shortName = "SFO"
        departureRegionNames.fullName = "SFO - San Francisco"
        departureSuggestion.regionNames = departureRegionNames

        val testDepartureCoordinates = SuggestionV4.LatLng()
        testDepartureCoordinates.lat = 600.5
        testDepartureCoordinates.lng = 300.3
        departureSuggestion.coordinates = testDepartureCoordinates

        val arrivalSuggestion = SuggestionV4()
        arrivalSuggestion.gaiaId = "5678"
        val arrivalRegionNames = SuggestionV4.RegionNames()
        arrivalRegionNames.displayName = "Los Angeles"
        arrivalRegionNames.shortName = "LAX"
        arrivalRegionNames.fullName = "LAX - Los Angeles"
        arrivalSuggestion.regionNames = arrivalRegionNames

        val testArrivalCoordinates = SuggestionV4.LatLng()
        testArrivalCoordinates.lat = 100.00
        testArrivalCoordinates.lng = 500.00
        arrivalSuggestion.coordinates = testArrivalCoordinates

        val childList = ArrayList<Int>()
        childList.add(4)
        val checkIn = LocalDate().plusDays(2)
        val checkOut = LocalDate().plusDays(3)

        return FlightSearchParams(departureSuggestion, arrivalSuggestion, checkIn, checkOut, 2, childList, false, null, null, null, null, null, null, null, null, null)
    }
}
