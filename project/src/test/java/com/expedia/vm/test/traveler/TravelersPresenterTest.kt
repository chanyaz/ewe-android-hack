package com.expedia.vm.test.traveler

import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.enums.PassengerCategory
import com.expedia.bookings.presenter.packages.AbstractTravelersPresenter
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.FlightTravelerEntryWidget
import com.expedia.vm.traveler.FlightTravelersViewModel
import com.expedia.vm.traveler.TravelerSelectItemViewModel
import com.mobiata.android.util.SettingUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import java.util.ArrayList
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class TravelersPresenterTest {

    lateinit var travelersPresenter: AbstractTravelersPresenter
    lateinit var activity : FragmentActivity

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        Ui.getApplication(activity).defaultTravelerComponent()

        Db.setTravelers(getMockTravelers(5))
    }

    private fun getMockTravelers(numberOfTravelers: Int): ArrayList<Traveler> {
        val listOfTravelers = ArrayList<Traveler>()
        for (i in 0..numberOfTravelers) {
            listOfTravelers.add(Traveler())
        }
        return listOfTravelers
    }

    @Test
    fun testChangedMoreTravelersDoesntCrash() {
        setupPresenterAndViewModel(LineOfBusiness.FLIGHTS_V2)
        Db.setTravelers(getMockTravelers(1))
        resetAndUpdateTravelers()
        Db.setTravelers(getMockTravelers(2))
        resetAndUpdateTravelers()
    }

    @Test
    fun testChangedLessTravelersDoesntCrash() {
        setupPresenterAndViewModel(LineOfBusiness.FLIGHTS_V2)
        Db.setTravelers(getMockTravelers(2))
        resetAndUpdateTravelers()
        travelersPresenter.travelerPickerWidget.refresh(getMockTravelers(2))

        Db.setTravelers(getMockTravelers(1))
        travelersPresenter.travelerPickerWidget.refresh(getMockTravelers(1))
        resetAndUpdateTravelers()
        travelersPresenter.viewModel.passportRequired.onNext(true)
        travelersPresenter.updateAllTravelerStatuses()
    }

    @Test
    fun testFrequentFlyerWidgetHiddenForPackages() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppFlightFrequentFlyerNumber)
        Db.setTravelers(getMockTravelers(0))
        setupPresenterAndViewModel(LineOfBusiness.PACKAGES)
        resetAndUpdateTravelers()

        (travelersPresenter.viewModel as FlightTravelersViewModel).flightLegs = listOf(FlightLeg())
        (travelersPresenter.viewModel as FlightTravelersViewModel).frequentFlyerPlans = FlightCreateTripResponse.FrequentFlyerPlans()
        travelersPresenter.showSelectOrEntryState()
        val flightTravelerEntryWidget = (travelersPresenter.travelerEntryWidget as FlightTravelerEntryWidget)

        assertTrue(flightTravelerEntryWidget.frequentflyerTestEnabled)
        assertEquals(View.VISIBLE, flightTravelerEntryWidget.advancedButton.visibility)
        assertEquals(View.GONE, flightTravelerEntryWidget.frequentFlyerButton?.visibility)
        assertEquals(View.GONE, flightTravelerEntryWidget.frequentFlyerRecycler?.visibility)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testRightDataShownInAdvancedOptionsWhenMultipleTravelerIsPresent() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppFlightFrequentFlyerNumber)
        var travelers = getMockTravelers(2)
        Db.setTravelers(travelers)
        setupPresenterAndViewModel(LineOfBusiness.FLIGHTS_V2)
        resetAndUpdateTravelers()

        travelersPresenter.travelerPickerWidget.refresh(Db.getTravelers())
        var travelerToSelect = TravelerSelectItemViewModel(activity, 0, travelers[0].age, PassengerCategory.ADULT)
        travelersPresenter.travelerPickerWidget.viewModel.selectedTravelerSubject.onNext(travelerToSelect)
        var travelerEntryWidget = (travelersPresenter.travelerEntryWidget as FlightTravelerEntryWidget)

        assertEquals(View.VISIBLE, travelerEntryWidget.visibility)
        assertEquals("", travelerEntryWidget.advancedOptionsWidget.travelerNumber.text.toString())
        assertEquals("", travelerEntryWidget.advancedOptionsWidget.redressNumber.text.toString())

        travelerEntryWidget.advancedOptionsWidget.travelerNumber.viewModel.textSubject.onNext("123456")
        travelerEntryWidget.advancedOptionsWidget.redressNumber.viewModel.textSubject.onNext("456")

        assertEquals("123456", travelers[0].knownTravelerNumber)
        assertEquals("456", travelers[0].redressNumber)
        assertEquals("123456", travelerEntryWidget.advancedOptionsWidget.travelerNumber.text.toString())
        assertEquals("456", travelerEntryWidget.advancedOptionsWidget.redressNumber.text.toString())


        travelerToSelect = TravelerSelectItemViewModel(activity, 1, travelers[1].age, PassengerCategory.ADULT)
        travelersPresenter.travelerPickerWidget.viewModel.selectedTravelerSubject.onNext(travelerToSelect)

        assertEquals(View.VISIBLE, travelerEntryWidget.visibility)
        assertEquals("", travelerEntryWidget.advancedOptionsWidget.travelerNumber.text.toString())
        assertEquals("", travelerEntryWidget.advancedOptionsWidget.redressNumber.text.toString())
        assertEquals("", travelerEntryWidget.advancedOptionsWidget.travelerNumber.text.toString())
        assertEquals("", travelerEntryWidget.advancedOptionsWidget.redressNumber.text.toString())
    }

    private fun resetAndUpdateTravelers() {
        travelersPresenter.resetTravelers()
        travelersPresenter.updateAllTravelerStatuses()
    }

    private fun setupPresenterAndViewModel(lob: LineOfBusiness) {
        travelersPresenter = LayoutInflater.from(activity).inflate(R.layout.flight_travelers_presenter_view_stub, null) as AbstractTravelersPresenter
        travelersPresenter.viewModel = FlightTravelersViewModel(activity, lob, false)
    }
}