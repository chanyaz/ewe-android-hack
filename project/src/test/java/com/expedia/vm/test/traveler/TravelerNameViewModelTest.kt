package com.expedia.vm.test.traveler

import android.app.Activity
import com.expedia.bookings.data.TravelerName
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.vm.traveler.TravelerNameViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import rx.observers.TestSubscriber
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class TravelerNameViewModelTest {
    lateinit var nameVM: TravelerNameViewModel
    val TEST_FIRST = "Oscar"
    val TEST_MIDDLE = "The"
    val TEST_LAST = "Grouch"

    var activity: Activity by Delegates.notNull()

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        Ui.getApplication(activity).defaultTravelerComponent()
    }

    @Test
    fun emptyTraveler() {
        nameVM = TravelerNameViewModel()
        nameVM.updateTravelerName(TravelerName())

        val testSubscriber = TestSubscriber<String>()
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

        nameVM = TravelerNameViewModel()
        nameVM.updateTravelerName(name)

        val testSubscriber = TestSubscriber<String>()
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
        nameVM = TravelerNameViewModel()
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
        nameVM = TravelerNameViewModel()
        nameVM.updateTravelerName(name)

        val testSubscriber = TestSubscriber<Boolean>()
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
