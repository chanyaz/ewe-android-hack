package com.expedia.bookings.widget

import android.content.Context
import android.support.design.widget.TextInputLayout
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.itin.activity.NewAddGuestItinActivity
import com.expedia.bookings.presenter.trips.AddGuestItinWidget
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.itin.AddGuestItinViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import com.expedia.bookings.services.TestObserver
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
@Config(sdk = intArrayOf(21))
class AddGuestItinWidgetTest {
    lateinit var sut: AddGuestItinWidget
    lateinit var viewModel: TestAddGuestItinViewModel
    lateinit var activity: NewAddGuestItinActivity

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(NewAddGuestItinActivity::class.java).create().get()
        viewModel = TestAddGuestItinViewModel(activity)
        val mockItineraryManager = viewModel.mockItineraryManager
        Mockito.doNothing().`when`(mockItineraryManager).addGuestTrip("malcolmnguyen@gmail.com", "123456789")
        sut = activity.addGuestItinWidget
        sut.viewModel = viewModel
    }

    @Test
    fun testWidgetSetup() {
        val toolBar = sut.toolbar
        assertEquals(View.VISIBLE, toolBar.visibility)
        assertEquals("Find guest booked trip", toolBar.title)

        val introText = sut.findViewById<View>(R.id.find_guest_itinerary_intro_text) as TextView
        assertEquals(View.VISIBLE, introText.visibility)
        assertEquals("Checked out without signing in? Find your trip by itinerary number. You can find the itinerary number in the confirmation email.", introText.text)

        val emailText = sut.findViewById<View>(R.id.email_edit_text_layout) as TextInputLayout
        assertEquals(View.VISIBLE, emailText.visibility)
        assertEquals("Email", emailText.hint)

        val itinNumberText = sut.findViewById<View>(R.id.itin_number_edit_text_layout) as TextInputLayout
        assertEquals(View.VISIBLE, itinNumberText.visibility)
        assertEquals("Itinerary number", itinNumberText.hint)

        assertEquals(View.VISIBLE, sut.findItinButton.visibility)
    }

    @Test
    fun testValidClick() {
        val testSubscriber = TestObserver.create<Pair<String, String>>()
        sut.viewModel.performGuestTripSearch.subscribe(testSubscriber)
        sut.guestEmailEditText.setText("malcolmnguyen@gmail.com")
        sut.itinNumberEditText.setText("123456789")
        sut.findItinButton.performClick()

        testSubscriber.assertValueCount(1)
    }

    @Test
    fun testInvalidClick() {
        val testSubscriber = TestObserver.create<Pair<String, String>>()
        sut.viewModel.performGuestTripSearch.subscribe(testSubscriber)
        sut.guestEmailEditText.setText("")
        sut.itinNumberEditText.setText("")
        sut.findItinButton.performClick()

        testSubscriber.assertNoValues()

        sut.guestEmailEditText.setText("malcolmnguyen@gmail.com")
        sut.itinNumberEditText.setText("")
        sut.findItinButton.performClick()

        testSubscriber.assertNoValues()

        sut.guestEmailEditText.setText("")
        sut.itinNumberEditText.setText("123456789")
        sut.findItinButton.performClick()

        testSubscriber.assertNoValues()

        sut.guestEmailEditText.setText("malcolmnguyen@gmail.com")
        sut.itinNumberEditText.setText("123456789")
        sut.findItinButton.performClick()

        testSubscriber.assertValueCount(1)
    }

    class TestAddGuestItinViewModel(context: Context) : AddGuestItinViewModel(context) {
        var mockItineraryManager: ItineraryManager = Mockito.spy(ItineraryManager.getInstance())

        override fun getItinManager(): ItineraryManager {
            return mockItineraryManager
        }
    }
}
