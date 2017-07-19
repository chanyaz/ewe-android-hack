package com.expedia.bookings.widget

import android.content.Context
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.itin.activity.NewAddGuestItinActivity
import com.expedia.bookings.presenter.trips.AddGuestItinWidget
import com.expedia.bookings.presenter.trips.ItinSignInPresenter
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.itin.AddGuestItinViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import rx.observers.TestSubscriber
import java.util.concurrent.TimeUnit

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
    fun testValidClick() {
        val testSubscriber = TestSubscriber.create<Pair<String, String>>()
        sut.viewModel.performGuestTripSearch.subscribe(testSubscriber)
        sut.guestEmailEditText.setText("malcolmnguyen@gmail.com")
        sut.itinNumberEditText.setText("123456789")
        sut.findItinButton.performClick()

        testSubscriber.assertValueCount(1)
    }

    @Test
    fun testInvalidClick() {
        val testSubscriber = TestSubscriber.create<Pair<String, String>>()
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
