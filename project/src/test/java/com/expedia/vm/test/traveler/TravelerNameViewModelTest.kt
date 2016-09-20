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
import com.jakewharton.rxbinding.widget.TextViewAfterTextChangeEvent
import org.junit.Before
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import kotlin.properties.Delegates

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

        val testSubscriber = TestSubscriber<String>(1)
        nameVM.firstNameSubject.subscribe(testSubscriber)
        nameVM.middleNameSubject.subscribe(testSubscriber)
        nameVM.lastNameSubject.subscribe(testSubscriber)

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

        val testSubscriber = TestSubscriber<String>(1)
        nameVM.firstNameSubject.subscribe(testSubscriber)
        nameVM.middleNameSubject.subscribe(testSubscriber)
        nameVM.lastNameSubject.subscribe(testSubscriber)

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

        nameVM.firstNameObserver.onNext(TEST_FIRST)
        assertEquals(TEST_FIRST, travelerName.firstName)

        nameVM.middleNameObserver.onNext(TEST_MIDDLE)
        assertEquals(TEST_MIDDLE, travelerName.middleName)

        nameVM.lastNameObserver.onNext(TEST_LAST)
        assertEquals(TEST_LAST, travelerName.lastName)
    }

    @Test
    fun validateAllNamesTriggersAllErrors() {
        val expectedErrorCount = 3
        var name = TravelerName()
        name.middleName = "@!$%"
        nameVM = TravelerNameViewModel(activity)
        nameVM.updateTravelerName(name)

        val testSubscriber = TestSubscriber<Boolean>(1)
        nameVM.firstNameErrorSubject.subscribe(testSubscriber)
        nameVM.middleNameErrorSubject.subscribe(testSubscriber)
        nameVM.lastNameErrorSubject.subscribe(testSubscriber)

        val valid = nameVM.validate()
        assertFalse(valid)
        // All 3 must trigger for proper error states
        testSubscriber.assertValueCount(expectedErrorCount)
    }

    fun assertNamesEqual(expectedFirst: String, expectedMiddle: String, expectedLast: String) {
        nameVM.firstNameSubject.subscribe { name ->
            assertEquals(expectedFirst, name)
        }
        nameVM.middleNameSubject.subscribe { name ->
            assertEquals(expectedMiddle, name)
        }
        nameVM.lastNameSubject.subscribe { name ->
            assertEquals(expectedLast, name)
        }
    }
}
