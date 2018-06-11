package com.expedia.bookings.itin.car.details

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.LifecycleOwner
import com.expedia.bookings.R
import com.expedia.bookings.itin.cars.ItinCarRepoInterface
import com.expedia.bookings.itin.cars.details.CarItinMoreHelpViewModel
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.helpers.MockCarRepo
import com.expedia.bookings.itin.helpers.MockLifecycleOwner
import com.expedia.bookings.itin.helpers.MockLxRepo
import com.expedia.bookings.itin.helpers.MockStringProvider
import com.expedia.bookings.itin.helpers.MockTripsTracking
import com.expedia.bookings.itin.lx.ItinLxRepoInterface
import com.expedia.bookings.itin.scopes.HasCarRepo
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

class CarItinMoreHelpViewModelTest {
    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    private lateinit var phoneNumberTestObserver: TestObserver<String>
    private lateinit var callButtonContentDescriptionTestObserver: TestObserver<String>
    private lateinit var helpTextTestObserver: TestObserver<String>
    private lateinit var confirmationNumberTestObserver: TestObserver<String>
    private lateinit var confirmationNumberTextVisiblityTestObserver: TestObserver<Boolean>
    private lateinit var confirmationNumberContentDescriptionTestObserver: TestObserver<String>

    private lateinit var vm: CarItinMoreHelpViewModel<MockCarItinMoreHelpScope>
    val noPhoneNumberJson = ItinMocker.carDetailsBadNameAndImage
    val noConfirmationNumber = ItinMocker.carDetailsBadLocations

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
    fun testMoreHelpHappyPath() {
        vm.itinObserver.onChanged(ItinMocker.carDetailsHappy)
        vm.itinCarObserver.onChanged(ItinMocker.carDetailsHappy.cars?.first())

        phoneNumberTestObserver.assertValue("02 9221 2231")

        val callButtonContDesc = (R.string.itin_car_call_button_content_description_TEMPLATE).toString().plus(
                mapOf("phonenumber" to "02 9221 2231"))
        callButtonContentDescriptionTestObserver.assertValue(callButtonContDesc)

        val helpText = (R.string.itin_more_help_text_TEMPLATE)
                .toString().plus(mapOf("supplier" to "Thrifty"))
        helpTextTestObserver.assertValue(helpText)

        confirmationNumberTestObserver.assertValue("8053084518926")
        confirmationNumberTextVisiblityTestObserver.assertValue(true)

        val confirmationNumberContDesc = (R.string.itin_more_help_confirmation_number_content_description_TEMPLATE)
                .toString().plus(mapOf("number" to "8053084518926"))
        confirmationNumberContentDescriptionTestObserver.assertValue(confirmationNumberContDesc)
    }

    @Test
    fun testNoPhoneNumberMoreHelp() {
        vm.itinObserver.onChanged(noPhoneNumberJson)
        vm.itinCarObserver.onChanged(noPhoneNumberJson.cars?.first())

        phoneNumberTestObserver.assertEmpty()
        callButtonContentDescriptionTestObserver.assertEmpty()

        confirmationNumberTestObserver.assertValue("8053084518926")
        confirmationNumberTextVisiblityTestObserver.assertValue(true)

        val confirmationNumberContDesc = (R.string.itin_more_help_confirmation_number_content_description_TEMPLATE)
                .toString().plus(mapOf("number" to "8053084518926"))
        confirmationNumberContentDescriptionTestObserver.assertValue(confirmationNumberContDesc)
    }

    @Test
    fun testNoConfirmationNumber() {
        vm.itinObserver.onChanged(noConfirmationNumber)

        confirmationNumberTestObserver.assertEmpty()
        confirmationNumberTextVisiblityTestObserver.assertValue(false)
    }

    @Test
    fun testPhoneNumberClickTracking() {
        assertFalse(vm.scope.tripsTracking.trackItinCarCallSupportClickedCalled)

        vm.phoneNumberClickSubject.onNext(Unit)

        assertTrue(vm.scope.tripsTracking.trackItinCarCallSupportClickedCalled)
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
        vm = CarItinMoreHelpViewModel(MockCarItinMoreHelpScope())

        vm.phoneNumberSubject.subscribe(phoneNumberTestObserver)
        vm.callButtonContentDescriptionSubject.subscribe(callButtonContentDescriptionTestObserver)
        vm.helpTextSubject.subscribe(helpTextTestObserver)
        vm.confirmationNumberSubject.subscribe(confirmationNumberTestObserver)
        vm.confirmationTitleVisibilitySubject.subscribe(confirmationNumberTextVisiblityTestObserver)
        vm.confirmationNumberContentDescriptionSubject.subscribe(confirmationNumberContentDescriptionTestObserver)
    }

    private class MockCarItinMoreHelpScope : HasStringProvider, HasLxRepo, HasLifecycleOwner, HasTripsTracking, HasCarRepo {
        override val strings: StringSource = MockStringProvider()
        override val itinLxRepo: ItinLxRepoInterface = MockLxRepo()
        override val lifecycleOwner: LifecycleOwner = MockLifecycleOwner()
        override val tripsTracking = MockTripsTracking()
        override val itinCarRepo: ItinCarRepoInterface = MockCarRepo()
    }
}
