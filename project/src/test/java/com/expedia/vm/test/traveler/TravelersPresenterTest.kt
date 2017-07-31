package com.expedia.vm.test.traveler

import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.presenter.packages.AbstractTravelersPresenter
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.vm.traveler.FlightTravelersViewModel
import com.expedia.vm.traveler.TravelersViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import java.util.ArrayList

@RunWith(RobolectricRunner::class)
class TravelersPresenterTest {

    lateinit var travelersPresenter: AbstractTravelersPresenter

    @Before
    fun setUp() {
        val activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        Ui.getApplication(activity).defaultTravelerComponent()

        Db.setTravelers(getMockTravelers(5))
        travelersPresenter = LayoutInflater.from(activity).inflate(R.layout.flight_travelers_presenter_view_stub, null) as AbstractTravelersPresenter
        travelersPresenter.viewModel = FlightTravelersViewModel(activity, LineOfBusiness.FLIGHTS, false)
        travelersPresenter.travelerPickerWidget.lineOfBusiness = LineOfBusiness.FLIGHTS
        travelersPresenter.resetTravelers()
        travelersPresenter.updateAllTravelerStatuses()
    }

    private fun getMockTravelers(numberOfTravelers: Int): ArrayList<Traveler> {
        val listOfTravelers = ArrayList<Traveler>()
        for (i in 0..numberOfTravelers) {
            listOfTravelers.add(Traveler())
        }
        return listOfTravelers
    }

    @Test
    fun testChangedTravelersDoesntCrash() {
        Db.setTravelers(getMockTravelers(1))
        travelersPresenter.resetTravelers()
        travelersPresenter.updateAllTravelerStatuses()
        Db.setTravelers(getMockTravelers(2))
        travelersPresenter.resetTravelers()
        travelersPresenter.updateAllTravelerStatuses()
    }
}