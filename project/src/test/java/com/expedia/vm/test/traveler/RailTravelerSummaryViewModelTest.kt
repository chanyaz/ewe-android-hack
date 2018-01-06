package com.expedia.vm.test.traveler

import android.app.Activity
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.TravelerName
import com.expedia.bookings.enums.TravelerCheckoutStatus
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.vm.traveler.RailTravelerSummaryViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class RailTravelerSummaryViewModelTest {
    var activity: Activity by Delegates.notNull()

    lateinit var testViewModel: RailTravelerSummaryViewModel

    val testFullName = "Oscar Grouch"
    val testEmail = "grouchyOscar@seasme.com"

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        Ui.getApplication(activity).defaultTravelerComponent()
    }

    @Test
    fun testGetTitleNoName() {
        addTravelerToDb(Traveler())
        testViewModel = RailTravelerSummaryViewModel(activity)

        assertEquals(activity.getString(R.string.checkout_enter_traveler_details), testViewModel.getTitle())
    }

    @Test
    fun testGetTitleWithName() {
        val mockTraveler = Mockito.mock(Traveler::class.java)
        Mockito.`when`(mockTraveler.fullName).thenReturn(testFullName)
        Mockito.`when`(mockTraveler.name).thenReturn(TravelerName())
        addTravelerToDb(mockTraveler)

        testViewModel = RailTravelerSummaryViewModel(activity)

        assertEquals(testFullName, testViewModel.getTitle())
    }

    @Test
    fun testGetSubTitleNoEmail() {
        addTravelerToDb(Traveler())

        testViewModel = RailTravelerSummaryViewModel(activity)

        assertEquals(activity.getString(R.string.enter_missing_traveler_details), testViewModel.getSubtitle())
    }

    @Test
    fun testGetSubTitleWithEmailButIncomplete() {
        val mockTraveler = Mockito.mock(Traveler::class.java)
        Mockito.`when`(mockTraveler.email).thenReturn(testEmail)
        Mockito.`when`(mockTraveler.name).thenReturn(TravelerName())
        addTravelerToDb(mockTraveler)

        testViewModel = RailTravelerSummaryViewModel(activity)

        assertEquals(activity.getString(R.string.enter_missing_traveler_details), testViewModel.getSubtitle())
    }

    @Test
    fun testGetSubTitleWithEmailComplete() {
        val mockTraveler = Mockito.mock(Traveler::class.java)
        Mockito.`when`(mockTraveler.email).thenReturn(testEmail)
        Mockito.`when`(mockTraveler.name).thenReturn(TravelerName())
        addTravelerToDb(mockTraveler)

        testViewModel = RailTravelerSummaryViewModel(activity)
        testViewModel.travelerStatusObserver.onNext(TravelerCheckoutStatus.COMPLETE)

        assertEquals(testEmail, testViewModel.getSubtitle())
    }


    fun addTravelerToDb(traveler: Traveler) {
        val travelers = Db.sharedInstance.travelers
        travelers.clear()
        travelers.add(traveler)
    }
}
