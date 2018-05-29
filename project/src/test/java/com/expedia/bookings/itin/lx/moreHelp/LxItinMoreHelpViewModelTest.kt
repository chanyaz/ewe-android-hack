package com.expedia.bookings.itin.lx.moreHelp

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.LifecycleOwner
import com.expedia.bookings.R
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.helpers.MockLifecycleOwner
import com.expedia.bookings.itin.helpers.MockLxRepo
import com.expedia.bookings.itin.helpers.MockStringProvider
import com.expedia.bookings.itin.helpers.MockTripsTracking
import com.expedia.bookings.itin.lx.ItinLxRepoInterface
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasLxRepo
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasTripsTracking
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.services.TestObserver
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LxItinMoreHelpViewModelTest {
    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    private lateinit var phoneNumberTestObserver: TestObserver<String>
    private lateinit var callButtonContentDescriptionTestObserver: TestObserver<String>
    private lateinit var helpTextTestObserver: TestObserver<String>
    private lateinit var confirmationNumberTestObserver: TestObserver<String>
    private lateinit var confirmationNumberTextVisiblityTestObserver: TestObserver<Boolean>
    private lateinit var confirmationNumberContentDescriptionTestObserver: TestObserver<String>

    private lateinit var vm: LxItinMoreHelpViewModel<MockLxItinMoreHelpScope>

    @Before
    fun setup() {
        phoneNumberTestObserver = TestObserver()
        callButtonContentDescriptionTestObserver = TestObserver()
        helpTextTestObserver = TestObserver()
        confirmationNumberTestObserver = TestObserver()
        confirmationNumberTextVisiblityTestObserver = TestObserver()
        confirmationNumberContentDescriptionTestObserver = TestObserver()
        setupViewModel()
    }

    @Test
    fun testFullMoreHelpData() {
        vm.itinObserver.onChanged(ItinMocker.lxDetailsAlsoHappy)
        vm.itinLxObserver.onChanged(ItinMocker.lxDetailsAlsoHappy.activities?.first())

        phoneNumberTestObserver.assertValue("+1 (415) 379 8000")

        val callButtonContDesc = (R.string.itin_lx_call_vendor_button_content_description_TEMPLATE).toString().plus(
                mapOf("phonenumber" to "+1 (415) 379 8000"))
        callButtonContentDescriptionTestObserver.assertValue(callButtonContDesc)

        val helpText = (R.string.itin_more_help_text_TEMPLATE)
                .toString().plus(mapOf("supplier" to "California Academy of Sciences Customer Service"))
        helpTextTestObserver.assertValue(helpText)

        confirmationNumberTestObserver.assertValue("8160334389745")
        confirmationNumberTextVisiblityTestObserver.assertValue(true)

        val confirmationNumberContDesc = (R.string.itin_more_help_confirmation_number_content_description_TEMPLATE)
                .toString().plus(mapOf("number" to "8160334389745"))
        confirmationNumberContentDescriptionTestObserver.assertValue(confirmationNumberContDesc)
    }

    @Test
    fun testPartialMoreHelpData() {
        vm.itinObserver.onChanged(ItinMocker.lxDetailsHappy)
        vm.itinLxObserver.onChanged(ItinMocker.lxDetailsHappy.activities?.first())

        phoneNumberTestObserver.assertEmpty()
        callButtonContentDescriptionTestObserver.assertEmpty()

        helpTextTestObserver.assertEmpty()

        confirmationNumberTestObserver.assertValue("8104062917948")
        confirmationNumberTextVisiblityTestObserver.assertValue(true)

        val confirmationNumberContDesc = (R.string.itin_more_help_confirmation_number_content_description_TEMPLATE)
                .toString().plus(mapOf("number" to "8104062917948"))
        confirmationNumberContentDescriptionTestObserver.assertValue(confirmationNumberContDesc)
    }

    @Test
    fun testNoConfirmationNumber() {
        vm.itinObserver.onChanged(ItinMocker.lxDetailsNoOrderNumber)

        confirmationNumberTestObserver.assertEmpty()
        confirmationNumberTextVisiblityTestObserver.assertValue(false)
    }

    @Test
    fun testPhoneNumberClickTracking() {
        assertFalse(vm.scope.tripsTracking.trackItinLxCallSupportClicked)
        vm.phoneNumberClickSubject.onNext(Unit)
        assertTrue(vm.scope.tripsTracking.trackItinLxCallSupportClicked)
    }

    @After
    fun tearDown() {
        phoneNumberTestObserver.dispose()
        callButtonContentDescriptionTestObserver.dispose()
        helpTextTestObserver.dispose()
        confirmationNumberTestObserver.dispose()
        confirmationNumberContentDescriptionTestObserver.dispose()
    }

    private fun setupViewModel() {
        vm = LxItinMoreHelpViewModel(MockLxItinMoreHelpScope())

        vm.phoneNumberSubject.subscribe(phoneNumberTestObserver)
        vm.callButtonContentDescriptionSubject.subscribe(callButtonContentDescriptionTestObserver)
        vm.helpTextSubject.subscribe(helpTextTestObserver)
        vm.confirmationNumberSubject.subscribe(confirmationNumberTestObserver)
        vm.confirmationTitleVisibilitySubject.subscribe(confirmationNumberTextVisiblityTestObserver)
        vm.confirmationNumberContentDescriptionSubject.subscribe(confirmationNumberContentDescriptionTestObserver)
    }

    private class MockLxItinMoreHelpScope : HasStringProvider, HasLxRepo, HasLifecycleOwner, HasTripsTracking {
        override val strings: StringSource = MockStringProvider()
        override val itinLxRepo: ItinLxRepoInterface = MockLxRepo()
        override val lifecycleOwner: LifecycleOwner = MockLifecycleOwner()
        override val tripsTracking = MockTripsTracking()
    }
}
