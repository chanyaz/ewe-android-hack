package com.expedia.bookings.test

import java.util.ArrayList

import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowApplication

import android.content.Context
import android.support.v7.app.AppCompatActivity

import com.expedia.bookings.data.Codes
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.ui.CarActivity
import com.expedia.vm.packages.PackageConfirmationViewModel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.robolectric.Robolectric

@RunWith(RobolectricRunner::class)
class PackageConfirmationViewModelTest {

    private var vm: PackageConfirmationViewModel? = null
    private var shadowApplication: ShadowApplication? = null

    @Before
    fun before() {
        Ui.getApplication(getContext()).defaultHotelComponents()
        vm = PackageConfirmationViewModel(getContext())
        shadowApplication = Shadows.shadowOf(getContext()).shadowApplication
    }

    private fun getContext(): Context {
        val activity = Robolectric.buildActivity(AppCompatActivity::class.java).create().get()
        return activity
    }

    @Test
    fun  addCarToBookingHappyCase() {
        val origin = Mockito.mock(SuggestionV4::class.java)
        val destination = Mockito.mock(SuggestionV4::class.java)
        val checkInDate = LocalDate()
        val checkOutDate = LocalDate()

        val params = PackageSearchParams(origin, destination, checkInDate, checkOutDate, 1, ArrayList<Int>(), false)
        Db.setPackageParams(params)

        val leg = FlightLeg()
        leg.destinationAirportCode = "SEA"
        leg.destinationAirportLocalName = "Tacoma Intl."
        Db.setPackageFlightBundle(leg, FlightLeg())

        vm!!.searchForCarRentalsForTripObserver(getContext()).onNext(null)
        val intent = shadowApplication!!.nextStartedActivity

        assertEquals(CarActivity::class.java.name, intent.component.className)
        assertTrue(intent.getBooleanExtra(Codes.EXTRA_OPEN_SEARCH, false))
    }
}
