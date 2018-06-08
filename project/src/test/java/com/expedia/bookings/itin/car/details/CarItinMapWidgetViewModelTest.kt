package com.expedia.bookings.itin.car.details

import android.arch.lifecycle.LifecycleOwner
import com.expedia.bookings.R
import com.expedia.bookings.itin.cars.ItinCarRepoInterface
import com.expedia.bookings.itin.cars.details.CarItinDropOffMapWidgetViewModel
import com.expedia.bookings.itin.cars.details.CarItinMapWidgetViewModel
import com.expedia.bookings.itin.cars.details.CarItinMapWidgetViewModelScope
import com.expedia.bookings.itin.cars.details.CarItinPickupMapWidgetViewModel
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.helpers.MockActivityLauncher
import com.expedia.bookings.itin.helpers.MockCarRepo
import com.expedia.bookings.itin.helpers.MockLifecycleOwner
import com.expedia.bookings.itin.helpers.MockPhoneHandler
import com.expedia.bookings.itin.helpers.MockStringProvider
import com.expedia.bookings.itin.helpers.MockToaster
import com.expedia.bookings.itin.helpers.MockTripsTracking
import com.expedia.bookings.itin.scopes.HasActivityLauncher
import com.expedia.bookings.itin.scopes.HasCarRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasPhoneHandler
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasToaster
import com.expedia.bookings.itin.scopes.HasTripsTracking
import com.expedia.bookings.itin.tripstore.data.CarLocation
import com.expedia.bookings.itin.tripstore.data.ItinCar
import com.expedia.bookings.itin.tripstore.extensions.buildFullAddress
import com.expedia.bookings.itin.tripstore.extensions.firstCar
import com.expedia.bookings.itin.utils.IActivityLauncher
import com.expedia.bookings.itin.utils.IPhoneHandler
import com.expedia.bookings.itin.utils.IToaster
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.tracking.ITripsTracking
import com.google.android.gms.maps.model.LatLng
import io.reactivex.observers.TestObserver
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CarItinMapWidgetViewModelTest {
    private lateinit var sut: CarItinMapWidgetViewModel<*>
    private var car: ItinCar? = null
    val addressLineFirstTestObserver = TestObserver<String>()
    val addressLineSecondTestObserver = TestObserver<String>()
    val latLongTestObserver = TestObserver<LatLng>()
    val contentDescLocationTestObserver = TestObserver<String>()
    val phoneNumberTextTestObserver = TestObserver<String>()
    val phoneNumberContDescTestObserver = TestObserver<String>()

    private class MockScope : HasLifecycleOwner, HasTripsTracking, HasToaster, HasStringProvider, HasPhoneHandler, HasCarRepo, HasActivityLauncher {
        override val itinCarRepo: ItinCarRepoInterface = MockCarRepo()
        val mockStrings = MockStringProvider()
        override val strings: StringSource = mockStrings
        val mockTracking = MockTripsTracking()
        override val tripsTracking: ITripsTracking = mockTracking
        override val lifecycleOwner: LifecycleOwner = MockLifecycleOwner()
        val mockToaster = MockToaster()
        override val toaster: IToaster = mockToaster
        val mockPhoneHandler = MockPhoneHandler()
        override val phoneHandler: IPhoneHandler = mockPhoneHandler
        override val activityLauncher: IActivityLauncher = MockActivityLauncher()
    }

    @Test
    fun happyTest() {
        val mockScope = MockScope()
        sut = MockCarItinMapWidgetViewModel(mockScope)
        setupObservers()

        addressLineFirstTestObserver.assertNoValues()
        addressLineSecondTestObserver.assertNoValues()
        phoneNumberTextTestObserver.assertNoValues()
        phoneNumberContDescTestObserver.assertNoValues()
        latLongTestObserver.assertNoValues()
        contentDescLocationTestObserver.assertNoValues()

        car = ItinMocker.carDetailsHappy.firstCar()
        val carVendor = car?.carVendor
        val expectedNumberDescString = R.string.itin_car_call_button_content_description_TEMPLATE.toString().plus(mapOf("phonenumber" to carVendor?.localPhoneNumber, "vendor" to carVendor?.longName))
        val expectedNumber = "02 9221 2231"
        val expectedCopyDescString = R.string.itin_car_address_copy_content_description_TEMPLATE.toString().plus(mapOf("address" to car?.pickupLocation?.buildFullAddress()))

        sut.itinLOBObserver.onChanged(car)

        addressLineFirstTestObserver.assertValue("Sir John Young Crescent Domain Car Park")
        addressLineSecondTestObserver.assertValue("Sydney, Victoria, AUS, 98188")
        phoneNumberTextTestObserver.assertValue(expectedNumber)
        phoneNumberContDescTestObserver.assertValue(expectedNumberDescString)
        contentDescLocationTestObserver.assertValue(expectedCopyDescString)
        latLongTestObserver.assertValue(LatLng(-33.871565, 151.214855))
        assertFalse(mockScope.mockPhoneHandler.handleCalled)

        sut.phoneNumberClickSubject.onNext(Unit)

        assertTrue(mockScope.mockPhoneHandler.handleCalled)
    }

    @Test
    fun getLocationNullTest() {
        val mockScope = MockScope()
        sut = MockCarItinMapWidgetViewModelWithNull(mockScope)
        setupObservers()

        addressLineFirstTestObserver.assertNoValues()
        addressLineSecondTestObserver.assertNoValues()
        phoneNumberTextTestObserver.assertNoValues()
        phoneNumberContDescTestObserver.assertNoValues()
        latLongTestObserver.assertNoValues()
        contentDescLocationTestObserver.assertNoValues()

        val car = ItinMocker.carDetailsHappy.firstCar()
        sut.itinLOBObserver.onChanged(car)

        addressLineFirstTestObserver.assertNoValues()
        addressLineSecondTestObserver.assertNoValues()
        phoneNumberTextTestObserver.assertNoValues()
        phoneNumberContDescTestObserver.assertNoValues()
        latLongTestObserver.assertNoValues()
        contentDescLocationTestObserver.assertNoValues()
    }

    @Test
    fun getLocationPickupTest() {
        val scope = CarItinMapWidgetViewModelScope(MockStringProvider(), MockTripsTracking(), MockLifecycleOwner(), MockCarRepo(), MockToaster(), MockPhoneHandler(), MockActivityLauncher())
        sut = CarItinPickupMapWidgetViewModel(scope)
        val car = ItinMocker.carDetailsHappy.firstCar()
        setupObservers()

        addressLineFirstTestObserver.assertNoValues()
        addressLineSecondTestObserver.assertNoValues()
        sut.itinLOBObserver.onChanged(car)
        addressLineFirstTestObserver.assertValue("Sir John Young Crescent Domain Car Park")
        addressLineSecondTestObserver.assertValue("Sydney, Victoria, AUS, 98188")
    }

    @Test
    fun getLocationDropOffDifferentTest() {
        val scope = CarItinMapWidgetViewModelScope(MockStringProvider(), MockTripsTracking(), MockLifecycleOwner(), MockCarRepo(), MockToaster(), MockPhoneHandler(), MockActivityLauncher())
        sut = CarItinDropOffMapWidgetViewModel(scope)
        val car = ItinMocker.carDetailsHappy.firstCar()

        setupObservers()

        addressLineFirstTestObserver.assertNoValues()
        addressLineSecondTestObserver.assertNoValues()
        sut.itinLOBObserver.onChanged(car)
        addressLineFirstTestObserver.assertValue("99 Spencer Street")
        addressLineSecondTestObserver.assertValue("Docklands, Victoria, AUS, 98188")
    }

    @Test
    fun nullHandleAddressLineOne() {
        val mockScope = MockScope()
        sut = MockCarItinMapWidgetViewModel(mockScope)
        setupObservers()

        addressLineFirstTestObserver.assertNoValues()
        car = ItinMocker.carDetailsBadLocations.firstCar()

        sut.itinLOBObserver.onChanged(car)

        addressLineFirstTestObserver.assertNoValues()
    }

    @Test
    fun nullLatOrLongTest() {
        val mockScope = MockScope()
        sut = MockCarItinMapWidgetViewModel(mockScope)
        setupObservers()
        latLongTestObserver.assertNoValues()
        val noLatCar = ItinMocker.carDetailsBadLocations.firstCar()
        val noLongCar = ItinMocker.carDetailsBadNameAndImage.firstCar()

        sut.itinLOBObserver.onChanged(noLatCar)
        latLongTestObserver.assertNoValues()

        sut.itinLOBObserver.onChanged(noLongCar)
        latLongTestObserver.assertNoValues()
    }

    @Test
    fun getLocationDropOffSameTest() {
        val scope = CarItinMapWidgetViewModelScope(MockStringProvider(), MockTripsTracking(), MockLifecycleOwner(), MockCarRepo(), MockToaster(), MockPhoneHandler(), MockActivityLauncher())
        sut = CarItinDropOffMapWidgetViewModel(scope)
        val car = ItinMocker.carDetailsHappyPickupDropOffSame.firstCar()

        setupObservers()

        addressLineFirstTestObserver.assertNoValues()
        addressLineSecondTestObserver.assertNoValues()
        sut.itinLOBObserver.onChanged(car)
        addressLineFirstTestObserver.assertNoValues()
        addressLineSecondTestObserver.assertNoValues()
    }

    private fun setupObservers() {
        sut.addressLineFirstSubject.subscribe(addressLineFirstTestObserver)
        sut.addressLineSecondSubject.subscribe(addressLineSecondTestObserver)
        sut.addressContainerContentDescription.subscribe(contentDescLocationTestObserver)
        sut.latLongSubject.subscribe(latLongTestObserver)
        sut.phoneNumberTextSubject.subscribe(phoneNumberTextTestObserver)
        sut.phoneNumberContDescriptionSubject.subscribe(phoneNumberContDescTestObserver)
    }

    private class MockCarItinMapWidgetViewModel(mockScope: MockScope) : CarItinMapWidgetViewModel<MockScope>(mockScope) {
        override fun getLocation(itinCar: ItinCar): CarLocation? {
            return itinCar.pickupLocation
        }
    }

    private class MockCarItinMapWidgetViewModelWithNull(mockScope: MockScope) : CarItinMapWidgetViewModel<MockScope>(mockScope) {
        override fun getLocation(itinCar: ItinCar): CarLocation? {
            return null
        }
    }
}
