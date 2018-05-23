package com.expedia.bookings.itin.lx.details

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.LifecycleOwner
import com.expedia.bookings.R
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.helpers.MockLifecycleOwner
import com.expedia.bookings.itin.helpers.MockLxRepo
import com.expedia.bookings.itin.helpers.MockPhoneHandler
import com.expedia.bookings.itin.helpers.MockStringProvider
import com.expedia.bookings.itin.helpers.MockToaster
import com.expedia.bookings.itin.helpers.MockTripsTracking
import com.expedia.bookings.itin.lx.ItinLxRepoInterface
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasLxRepo
import com.expedia.bookings.itin.scopes.HasPhoneHandler
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasToaster
import com.expedia.bookings.itin.scopes.HasTripsTracking
import com.expedia.bookings.itin.tripstore.extensions.buildFullAddress
import com.expedia.bookings.itin.tripstore.extensions.firstLx
import com.expedia.bookings.itin.utils.IPhoneHandler
import com.expedia.bookings.itin.utils.IToaster
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.tracking.ITripsTracking
import com.google.android.gms.maps.model.LatLng
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LxItinMapWidgetViewModelTest {
    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()
    private lateinit var sut: LxItinMapWidgetViewModel<MockVMScope>
    val addressLineFirstTestObserver = TestObserver<String>()
    val addressLineSecondTestObserver = TestObserver<String>()
    val latLongTestObserver = TestObserver<LatLng>()
    val contentDescTestObserver = TestObserver<String>()
    val phoneNumberTextTestObserver = TestObserver<String>()
    val phoneNumberContDescTestObserver = TestObserver<String>()
    private lateinit var mockScope: MockVMScope

    @Before
    fun setup() {
        mockScope = MockVMScope()
        sut = LxItinMapWidgetViewModel(mockScope)
        sut.addressLineFirstSubject.subscribe(addressLineFirstTestObserver)
        sut.addressLineSecondSubject.subscribe(addressLineSecondTestObserver)
        sut.addressContainerContentDescription.subscribe(contentDescTestObserver)
        sut.latLongSubject.subscribe(latLongTestObserver)
        sut.phoneNumberTextSubject.subscribe(phoneNumberTextTestObserver)
        sut.phoneNumberContDescriptionSubject.subscribe(phoneNumberContDescTestObserver)
    }

    @Test
    fun nullLxItinTest() {
        addressLineFirstTestObserver.assertNoValues()
        addressLineSecondTestObserver.assertNoValues()
        latLongTestObserver.assertNoValues()
        sut.itinLxObserver.onChanged(null)
        addressLineFirstTestObserver.assertNoValues()
        addressLineSecondTestObserver.assertNoValues()
        latLongTestObserver.assertNoValues()
    }

    @Test
    fun faultyLxItinAllButLongNullTest() {
        addressLineFirstTestObserver.assertNoValues()
        addressLineSecondTestObserver.assertNoValues()
        latLongTestObserver.assertNoValues()
        sut.itinLxObserver.onChanged(ItinMocker.lxDetailsNoDetailsUrl.firstLx())
        addressLineFirstTestObserver.assertNoValues()
        addressLineSecondTestObserver.assertValue("")
        latLongTestObserver.assertNoValues()
    }

    @Test
    fun phoneNumberHappy() {
        phoneNumberTextTestObserver.assertNoValues()
        phoneNumberContDescTestObserver.assertNoValues()
        assertFalse(mockScope.mockPhoneHandler.handleCalled)

        sut.itinLxObserver.onChanged(ItinMocker.lxDetailsAlsoHappy.firstLx())

        val expectedNumber = "+1 (415) 379 8000"
        val expectedString = R.string.itin_activity_manage_booking_call_lx_button_content_description_TEMPLATE.toString().plus(mapOf("phonenumber" to expectedNumber))
        phoneNumberTextTestObserver.assertValue(expectedNumber)
        phoneNumberContDescTestObserver.assertValue(expectedString)
        assertFalse(mockScope.mockPhoneHandler.handleCalled)

        sut.phoneNumberClickSubject.onNext(Unit)

        assertTrue(mockScope.mockPhoneHandler.handleCalled)
    }

    @Test
    fun phoneNumberSad() {
        phoneNumberTextTestObserver.assertNoValues()
        phoneNumberContDescTestObserver.assertNoValues()
        assertFalse(mockScope.mockPhoneHandler.handleCalled)

        sut.itinLxObserver.onChanged(ItinMocker.lxDetailsNoVendorPhone.firstLx())

        phoneNumberTextTestObserver.assertNoValues()
        phoneNumberContDescTestObserver.assertNoValues()
        assertFalse(mockScope.mockPhoneHandler.handleCalled)

        sut.phoneNumberClickSubject.onNext(Unit)

        assertFalse(mockScope.mockPhoneHandler.handleCalled)
    }

    @Test
    fun faultyLxItinLongNullTest() {
        latLongTestObserver.assertNoValues()
        sut.scope.mockRepo.liveDataLx.value = ItinMocker.lxDetailsNoLat.firstLx()
        latLongTestObserver.assertNoValues()
    }

    @Test
    fun addressClickTest() {
        sut.itinLxObserver.onChanged(ItinMocker.lxDetailsHappy.firstLx())
        assertFalse(mockScope.mockToaster.toasted)
        sut.addressClickSubject.onNext(Unit)
        assertTrue(mockScope.mockToaster.toasted)
    }

    @Test
    fun happyTest() {
        addressLineFirstTestObserver.assertNoValues()
        addressLineSecondTestObserver.assertNoValues()
        latLongTestObserver.assertNoValues()
        contentDescTestObserver.assertNoValues()

        assertFalse(sut.scope.mockTracking.directionClicked)
        assertFalse(sut.scope.mockTracking.mapClicked)
        assertFalse(sut.scope.mockStrings.fetchWithPhraseCalled)
        val expectedString = R.string.itin_lx_details_address_copy_content_description_TEMPLATE.toString().plus(mapOf("address" to ItinMocker.lxDetailsHappy.firstLx()?.buildFullAddress()))
        sut.itinLxObserver.onChanged(ItinMocker.lxDetailsHappy.firstLx())
        addressLineFirstTestObserver.assertValue("55 Music Concourse Drive")
        addressLineSecondTestObserver.assertValue("San Francisco, CA, USA, 94118")
        contentDescTestObserver.assertValue(expectedString)
        latLongTestObserver.assertValue(LatLng(37.76974, -122.46614))

        sut.mapClickSubject.onNext(Unit)
        sut.directionButtonClickSubject.onNext(Unit)

        assertTrue(sut.scope.mockTracking.directionClicked)
        assertTrue(sut.scope.mockTracking.mapClicked)
        assertTrue(sut.scope.mockStrings.fetchWithPhraseCalled)
    }

    private class MockVMScope : HasLifecycleOwner, HasLxRepo, HasTripsTracking, HasToaster, HasStringProvider, HasPhoneHandler {
        val mockPhoneHandler = MockPhoneHandler()
        override val phoneHandler: IPhoneHandler = mockPhoneHandler
        val mockStrings = MockStringProvider()
        override val strings: StringSource = mockStrings
        val mockToaster = MockToaster()
        override val toaster: IToaster = mockToaster
        val mockTracking = MockTripsTracking()
        override val tripsTracking: ITripsTracking = mockTracking
        override val lifecycleOwner: LifecycleOwner = MockLifecycleOwner()
        val mockRepo = MockLxRepo(false)
        override val itinLxRepo: ItinLxRepoInterface = mockRepo
    }
}
