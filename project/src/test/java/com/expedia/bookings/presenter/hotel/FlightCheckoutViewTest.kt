package com.expedia.bookings.presenter.hotel

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.activity.PlaygroundActivity
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.presenter.flight.FlightPresenter
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RoboTestHelper
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.Ui
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import com.mobiata.android.util.SettingUtils
import org.joda.time.LocalDate
import org.robolectric.RuntimeEnvironment
import java.util.ArrayList
import kotlin.test.assertFalse
import kotlin.test.assertTrue


@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class))
class FlightCheckoutViewTest {
    lateinit var activity: Activity
    lateinit var flightPresenter: FlightPresenter

    @Before
    fun setup() {
        Ui.getApplication(RuntimeEnvironment.application).defaultFlightComponents()
        Ui.getApplication(RuntimeEnvironment.application).defaultTravelerComponent()
        val intent = PlaygroundActivity.createIntent(RuntimeEnvironment.application, R.layout.flight_activity)
        val styledIntent = PlaygroundActivity.addTheme(intent, R.style.V2_Theme_Packages)
        activity = Robolectric.buildActivity(PlaygroundActivity::class.java).withIntent(styledIntent).create().visible().get()
        setupDb()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testOpeningOfWebCheckoutViewFromInboundPresenter() {
        setPOSToIndia()
        turnOnABTestAndFeatureToggle()
        flightPresenter = LayoutInflater.from(activity).inflate(R.layout.flight_activity, null) as FlightPresenter
        setupTestToOpenInFlightInboundPresenter()

        flightPresenter.flightOfferViewModel.flightProductId.onNext("12345")

        assertTrue(flightPresenter.webCheckoutView.visibility == View.VISIBLE)
        assertTrue(flightPresenter.flightOverviewPresenter.visibility == View.GONE)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testOpeningOfWebCheckoutViewFromOutBoundPresenter() {
        setPOSToIndia()
        turnOnABTestAndFeatureToggle()
        flightPresenter = LayoutInflater.from(activity).inflate(R.layout.flight_activity, null) as FlightPresenter
        setupTestToOpenInFlightOutboundPresenter()
        flightPresenter.flightOfferViewModel.flightProductId.onNext("12345")

        assertTrue(flightPresenter.webCheckoutView.visibility == View.VISIBLE)
        assertTrue(flightPresenter.flightOverviewPresenter.visibility == View.GONE)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testOpeningOfFlightOverviewPresenterFromOutBoundPresenter() {
        flightPresenter = LayoutInflater.from(activity).inflate(R.layout.flight_activity, null) as FlightPresenter
        setupTestToOpenInFlightOutboundPresenter()
        flightPresenter.flightOfferViewModel.flightProductId.onNext("12345")

        assertTrue(flightPresenter.webCheckoutView.visibility == View.GONE)
        assertTrue(flightPresenter.flightOverviewPresenter.visibility == View.VISIBLE)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testOpeningOfFlightOverviewPresenterFromInboundPresenter() {
        flightPresenter = LayoutInflater.from(activity).inflate(R.layout.flight_activity, null) as FlightPresenter
        setupTestToOpenInFlightInboundPresenter()

        assertTrue(flightPresenter.webCheckoutView.visibility == View.GONE)

        flightPresenter.flightOfferViewModel.flightProductId.onNext("12345")

        assertTrue(flightPresenter.webCheckoutView.visibility == View.GONE)
        assertTrue(flightPresenter.flightOverviewPresenter.visibility == View.VISIBLE)
    }

    private fun setupTestToOpenInFlightInboundPresenter() {
        flightPresenter.flightOfferViewModel.isRoundTripSearchSubject.onNext(false)
        flightPresenter.show(flightPresenter.inboundPresenter)
    }

    private fun setupTestToOpenInFlightOutboundPresenter() {
        flightPresenter.flightOfferViewModel.isRoundTripSearchSubject.onNext(true)
        flightPresenter.show(flightPresenter.outBoundPresenter)
    }

    private fun setPOSToIndia() {
        val pointOfSale = PointOfSaleId.INDIA
        SettingUtils.save(activity, "point_of_sale_key", pointOfSale.id.toString())
        PointOfSale.onPointOfSaleChanged(activity)
    }

    private fun turnOnABTestAndFeatureToggle() {
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppShowFlightsCheckoutWebview)
        SettingUtils.save(activity.applicationContext, R.string.preference_show_flights_checkout_webview, true)
    }

    private fun setupDb() {
        Db.setFlightSearchParams(setupFlightSearchParams())
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

        return FlightSearchParams(departureSuggestion, arrivalSuggestion, checkIn, checkOut, 2, childList, false, null, null, null, null, null,null)
    }

}