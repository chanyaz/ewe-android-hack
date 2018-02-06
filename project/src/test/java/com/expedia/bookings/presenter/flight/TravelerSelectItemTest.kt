package com.expedia.bookings.presenter.flight

import android.support.v4.app.FragmentActivity
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.enums.PassengerCategory
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.FlightTestUtil
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.traveler.TravelerSelectItem
import com.expedia.vm.test.traveler.MockTravelerProvider
import com.expedia.vm.traveler.TravelerSelectItemViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner :: class)
@RunForBrands(brands = [(MultiBrand.EXPEDIA)])
class TravelerSelectItemTest {
    private var activity: FragmentActivity by Delegates.notNull()
    lateinit var travelerSelectItemViewModel: TravelerSelectItemViewModel
    lateinit var travelerSelectItem: TravelerSelectItem
    private val mockTravelerProvider = MockTravelerProvider()

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        Ui.getApplication(activity).defaultFlightComponents()
        Ui.getApplication(activity).defaultTravelerComponent()
        travelerSelectItemViewModel = TravelerSelectItemViewModel(activity, 0, -1, PassengerCategory.ADULT)
        travelerSelectItem = TravelerSelectItem(activity, travelerSelectItemViewModel)
    }

    @Test
    fun testAccessibilityWithEmptyData() {
        mockTravelerProvider.updateDBWithMockTravelers(1, Traveler())
        travelerSelectItemViewModel.refreshStatusObservable.onNext(Unit)

        assertEquals("Edit Traveler 1 (Adult) Button", travelerSelectItem.contentDescription)
    }

    @Test
    fun testAccessibilityWithCompleteData() {
        Db.sharedInstance.resetTravelers()
        travelerSelectItemViewModel.travelerValidator.updateForNewSearch(FlightTestUtil.getFlightSearchParams(false, false))
        mockTravelerProvider.updateDBWithMockTravelers(1, mockTravelerProvider.getCompleteMockTraveler())
        travelerSelectItemViewModel.refreshStatusObservable.onNext(Unit)
        val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
        val date = LocalDate.now().minusYears(24).format(formatter)

        assertEquals("Oscar The Grouch, " + date + ", traveler details complete. Button.", travelerSelectItem.contentDescription)
    }

    @Test
    fun testAccessibilityWithInCompleteData() {
        travelerSelectItemViewModel.travelerValidator.updateForNewSearch(FlightTestUtil.getFlightSearchParams(false, false))
        mockTravelerProvider.updateDBWithMockTravelers(1, mockTravelerProvider.getCompleteMockTravelerExecptBirthday())
        travelerSelectItemViewModel.refreshStatusObservable.onNext(Unit)

        assertEquals("Oscar The Grouch Error: Enter missing traveler details. Button.", travelerSelectItem.contentDescription)
    }
}
