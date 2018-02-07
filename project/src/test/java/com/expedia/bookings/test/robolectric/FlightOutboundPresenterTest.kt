package com.expedia.bookings.test.robolectric

import android.support.v4.app.FragmentActivity
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.presenter.flight.FlightOutboundPresenter
import com.expedia.bookings.utils.Ui
import com.expedia.util.Optional
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightOutboundPresenterTest {
    private val context = RuntimeEnvironment.application
    private lateinit var flightOutboundPresenter: FlightOutboundPresenter

    @Before
    fun setup() {
        val activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        Ui.getApplication(activity).defaultTravelerComponent()
        Ui.getApplication(activity).defaultFlightComponents()
        flightOutboundPresenter = LayoutInflater.from(activity).inflate(R.layout.flight_outbound_stub, null) as FlightOutboundPresenter
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
}
