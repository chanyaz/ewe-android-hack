package com.expedia.vm.test.traveler

import android.app.Activity
import android.text.Editable
import com.expedia.bookings.data.TravelerName
import com.expedia.vm.traveler.TravelerNameViewModel
import org.junit.runner.RunWith;
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertEquals

import com.expedia.bookings.test.robolectric.RobolectricRunner;
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.TextView
import org.junit.Before
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import com.expedia.bookings.services.TestObserver
import kotlin.properties.Delegates
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class TravelerNameViewModelTest {
    lateinit var nameVM: TravelerNameViewModel
    val TEST_FIRST = "Oscar"
    val TEST_MIDDLE = "The"
    val TEST_LAST = "Grouch"

    val TEST_FIRST_EDITABLE = Editable.Factory().newEditable(TEST_FIRST)
    val TEST_MID_EDITABLE = Editable.Factory().newEditable(TEST_MIDDLE)
    val TEST_LAST_EDITABLE = Editable.Factory().newEditable(TEST_LAST)

    val TEST_TEXT_VIEW = TextView(RuntimeEnvironment.application)
    var activity: Activity by Delegates.notNull()

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        Ui.getApplication(activity).defaultTravelerComponent()
    }

    @Test
    fun emptyTraveler() {
        nameVM = TravelerNameViewModel(activity)
        nameVM.updateTravelerName(TravelerName())

        val testSubscriber = TestObserver<String>(1)
        nameVM.firstNameViewModel.textSubject.subscribe(testSubscriber)
        nameVM.middleNameViewModel.textSubject.subscribe(testSubscriber)
        nameVM.lastNameViewModel.textSubject.subscribe(testSubscriber)

        assertEquals("", testSubscriber.onNextEvents[0])
        assertEquals("", testSubscriber.onNextEvents[1])
        assertEquals("", testSubscriber.onNextEvents[2])
        testSubscriber.assertValueCount(3)
    }

    @Test
    fun travelerWithNames() {
        val name = TravelerName()
        name.firstName = TEST_FIRST
        name.middleName = TEST_MIDDLE
        name.lastName = TEST_LAST

        nameVM = TravelerNameViewModel(activity)
        nameVM.updateTravelerName(name)

        val testSubscriber = TestObserver<String>(1)
        nameVM.firstNameViewModel.textSubject.subscribe(testSubscriber)
        nameVM.middleNameViewModel.textSubject.subscribe(testSubscriber)
        nameVM.lastNameViewModel.textSubject.subscribe(testSubscriber)

        assertEquals(TEST_FIRST, testSubscriber.onNextEvents[0])
        assertEquals(TEST_MIDDLE, testSubscriber.onNextEvents[1])
        assertEquals(TEST_LAST, testSubscriber.onNextEvents[2])
        testSubscriber.assertValueCount(3)
    }

    @Test
    fun travelerNameChange() {
        val travelerName = TravelerName()
        nameVM = TravelerNameViewModel(activity)
        nameVM.updateTravelerName(travelerName)

        nameVM.firstNameViewModel.textSubject.onNext(TEST_FIRST)
        assertEquals(TEST_FIRST, travelerName.firstName)

        nameVM.middleNameViewModel.textSubject.onNext(TEST_MIDDLE)
        assertEquals(TEST_MIDDLE, travelerName.middleName)

        nameVM.lastNameViewModel.textSubject.onNext(TEST_LAST)
        assertEquals(TEST_LAST, travelerName.lastName)
    }

    @Test
    fun validateAllNamesTriggersAllErrors() {
        val expectedErrorCount = 3
        val name = TravelerName()
        name.middleName = "@!$%"
        nameVM = TravelerNameViewModel(activity)
        nameVM.updateTravelerName(name)

        val testSubscriber = TestObserver<Boolean>(1)
        nameVM.firstNameViewModel.errorSubject.subscribe(testSubscriber)
        nameVM.middleNameViewModel.errorSubject.subscribe(testSubscriber)
        nameVM.lastNameViewModel.errorSubject.subscribe(testSubscriber)

        val valid = nameVM.validate()
        assertFalse(valid)
        assertTrue(nameVM.numberOfInvalidFields.value == 3)
        // All 3 must trigger for proper error states
        testSubscriber.assertValueCount(expectedErrorCount)
    }

    fun assertNamesEqual(expectedFirst: String, expectedMiddle: String, expectedLast: String) {
        nameVM.firstNameViewModel.textSubject.subscribe { name ->
            assertEquals(expectedFirst, name)
        }
        nameVM.middleNameViewModel.textSubject.subscribe { name ->
            assertEquals(expectedMiddle, name)
        }
        nameVM.lastNameViewModel.textSubject.subscribe { name ->
            assertEquals(expectedLast, name)
        }
    }
}
