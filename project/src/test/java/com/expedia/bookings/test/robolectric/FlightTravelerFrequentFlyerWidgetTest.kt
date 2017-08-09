package com.expedia.bookings.test.robolectric

import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightLeg.FlightSegment
import com.expedia.bookings.enums.TravelerCheckoutStatus
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.FlightTravelerEntryWidget
import com.expedia.vm.traveler.FlightTravelerEntryWidgetViewModel
import com.mobiata.android.util.SettingUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import rx.subjects.BehaviorSubject
import java.util.ArrayList
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))

class FlightTravelerFrequentFlyerWidgetTest {
    private lateinit var widget: FlightTravelerEntryWidget
    private val context = RuntimeEnvironment.application

    val testRegionName = "Chicago"
    val testAirportCode = "ORD"
    val traveler = Traveler()

    @Before
    fun setup() {
        val activity = Robolectric.buildActivity(android.support.v4.app.FragmentActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        Ui.getApplication(context).defaultTravelerComponent()
        Ui.getApplication(context).defaultFlightComponents()


        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms, AbacusUtils.EBAndroidAppFlightFrequentFlyerNumber)
        SettingUtils.save(activity, R.string.preference_enable_flights_frequent_flyer_number, true)
        widget = LayoutInflater.from(activity).inflate(R.layout.test_flight_entry_widget, null) as FlightTravelerEntryWidget

        Db.getTravelers().add(traveler)
        widget.viewModel = FlightTravelerEntryWidgetViewModel(activity, 0, BehaviorSubject.create(false), TravelerCheckoutStatus.CLEAN)
    }

    @Test
    fun testFFNCardVisibility() {
        (widget.viewModel as FlightTravelerEntryWidgetViewModel).flightLegsObservable.onNext(listOf(buildMockFlight(3)))
        widget.frequentFlyerButton?.performClick()
        assertEquals(View.VISIBLE, widget.frequentFlyerRecycler?.visibility)
    }

    @Test
    fun testCorrectFFNCardsDuplicateAirlines() {
        (widget.viewModel as FlightTravelerEntryWidgetViewModel).flightLegsObservable.onNext(listOf(buildMockFlight(3)))
        assertEquals(2, widget.frequentFlyerRecycler?.adapter?.itemCount)
    }

    @Test
    fun testCorrectFFNCardsNullAirlines() {
        (widget.viewModel as FlightTravelerEntryWidgetViewModel).flightLegsObservable.onNext(listOf(buildMockFlight(null)))
        assertEquals(0, widget.frequentFlyerRecycler?.adapter?.itemCount)
    }

    @Test
    fun testCorrectFFNCardsNoAirlines() {
        (widget.viewModel as FlightTravelerEntryWidgetViewModel).flightLegsObservable.onNext(listOf(buildMockFlight(0)))
        assertEquals(0, widget.frequentFlyerRecycler?.adapter?.itemCount)
    }

    private fun buildMockFlight(airlineListSize: Int?) : FlightLeg {
        val flight = Mockito.mock(FlightLeg::class.java)
        flight.destinationAirportCode = testAirportCode
        flight.destinationCity = testRegionName

        val airlineNames = listOf("Alaska Airlines", "Delta", "Alaska Airlines")

        createFlightSegments(flight, airlineNames, airlineListSize)
        return flight
    }

    private fun createFlightSegments(flight: FlightLeg, airlineNames: List<String>, size: Int?) {
        flight.segments = ArrayList<FlightSegment>()
        if (size != null) {
            for (i in 0 until size!!) {
                flight.segments.add(FlightSegment())
                flight.segments.get(i).airlineName = airlineNames.get(i)
                flight.segments.get(i).airlineCode = "AA"
            }
        }
    }
}
