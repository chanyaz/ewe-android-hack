package com.expedia.bookings.itin.common

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.LifecycleOwner
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.helpers.MockActivityLauncher
import com.expedia.bookings.itin.helpers.MockItinRepo
import com.expedia.bookings.itin.helpers.MockLifecycleOwner
import com.expedia.bookings.itin.helpers.MockTripsTracking
import com.expedia.bookings.itin.scopes.HasActivityLauncher
import com.expedia.bookings.itin.scopes.HasItinRepo
import com.expedia.bookings.itin.scopes.HasItinType
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasTripsTracking
import com.expedia.bookings.itin.utils.IActivityLauncher
import com.expedia.bookings.tracking.ITripsTracking
import com.google.android.gms.maps.model.LatLng
import io.reactivex.observers.TestObserver
import org.junit.After
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ItinExpandedMapViewModelTest {
    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()
    private lateinit var sut: ItinExpandedMapViewModel<MockItinExpandedMapViewModelScope>
    var toolbarPairSubjectObserver = TestObserver<Pair<String?, String?>>()
    var latLngSubjectObserver = TestObserver<LatLng>()
    var directionButtonClickSubjectObserver = TestObserver<Unit>()
    val happyPathHotel = ItinMocker.hotelDetailsHappy
    val noTitleHotel = ItinMocker.hotelDetailsNoPriceDetails
    val noAddressHotel = ItinMocker.hotelDetailsExpediaCollect
    val happyPathLx = ItinMocker.lxDetailsHappy
    val happyPathCar = ItinMocker.carDetailsHappy
    val noTitleLx = ItinMocker.lxDetailsNoLat
    val noTitleCar = ItinMocker.carDetailsBadNameAndImage
    val noLngHotel = ItinMocker.hotelDetailsExpediaCollect
    val noLngLx = ItinMocker.lxDetailsInvalidLatLong
    val noLngCar = ItinMocker.carDetailsBadPickupAndTimes
    val noLatHotel = ItinMocker.hotelDetailsNoPriceDetails
    val noLatLx = ItinMocker.lxDetailsNoDetailsUrl
    val noLatCar = ItinMocker.carDetailsBadNameAndImage
    val noLatNoLng = ItinMocker.lxDetailsNoLatLong
    val noAddressCar = ItinMocker.carDetailsBadPickupAndTimes
    val noAddressLx = ItinMocker.lxDetailsNoDetailsUrl
    private lateinit var mockScope: MockItinExpandedMapViewModelScope

    @After
    fun tearDown() {
        toolbarPairSubjectObserver.dispose()
        latLngSubjectObserver.dispose()
        directionButtonClickSubjectObserver.dispose()
    }

    @Test
    fun testNullItinHotel() {
        setUpLOB(TripProducts.HOTEL.name)
        toolbarPairSubjectObserver.assertNoValues()
        sut.itinObserver.onChanged(null)

        toolbarPairSubjectObserver.assertNoValues()
    }

    @Test
    fun testNullItinLx() {
        setUpLOB(TripProducts.ACTIVITY.name)
        toolbarPairSubjectObserver.assertNoValues()
        sut.itinObserver.onChanged(null)

        toolbarPairSubjectObserver.assertNoValues()
    }

    @Test
    fun testNullItinCar() {
        setUpLOB(TripProducts.CAR.name)
        toolbarPairSubjectObserver.assertNoValues()
        sut.itinObserver.onChanged(null)

        toolbarPairSubjectObserver.assertNoValues()
    }

    @Test
    fun testGetNamePairHappyPathHotel() {
        setUpLOB(TripProducts.HOTEL.name)
        toolbarPairSubjectObserver.assertNoValues()
        sut.itinObserver.onChanged(happyPathHotel)

        toolbarPairSubjectObserver.assertValue(Pair("Crest Hotel", "Bengaluru, Karnataka, IND, 560080"))
    }

    @Test
    fun testGetNamePairHappyPathLx() {
        setUpLOB(TripProducts.ACTIVITY.name)
        toolbarPairSubjectObserver.assertNoValues()
        sut.itinObserver.onChanged(happyPathLx)

        toolbarPairSubjectObserver.assertValue(Pair("California Academy of Sciences", "San Francisco, CA, USA, 94118"))
    }

    @Test
    fun testGetNamePairHappyPathCar() {
        setUpLOB(TripProducts.CAR.name)
        toolbarPairSubjectObserver.assertNoValues()

        sut.itinObserver.onChanged(happyPathCar)

        toolbarPairSubjectObserver.assertValue(Pair("Thrifty", "Docklands, Victoria, AUS, 98188"))
    }

    @Test
    fun testGetNamePairNoNameHotel() {
        setUpLOB(TripProducts.HOTEL.name)
        toolbarPairSubjectObserver.assertNoValues()

        sut.itinObserver.onChanged(noTitleHotel)

        toolbarPairSubjectObserver.assertValue(Pair(null, "Moscow, RUS, 105613"))
    }

    @Test
    fun testGetNamePairNoNameLx() {
        setUpLOB(TripProducts.ACTIVITY.name)
        toolbarPairSubjectObserver.assertNoValues()

        sut.itinObserver.onChanged(noTitleLx)

        toolbarPairSubjectObserver.assertValue(Pair(null, "San Francisco, CA, USA, 94118"))
    }

    @Test
    fun testGetNamePairNoNameCar() {
        setUpLOB(TripProducts.CAR.name)
        toolbarPairSubjectObserver.assertNoValues()

        sut.itinObserver.onChanged(noTitleCar)

        toolbarPairSubjectObserver.assertValue(Pair(null, "Docklands, Victoria, AUS, 98188"))
    }

    @Test
    fun testGetNamePairNoAddressHotel() {
        setUpLOB(TripProducts.HOTEL.name)
        toolbarPairSubjectObserver.assertNoValues()

        sut.itinObserver.onChanged(noAddressHotel)

        toolbarPairSubjectObserver.assertValue(Pair("Clayton Plaza Hotel", ""))
    }

    @Test
    fun testGetNamePairNoAddressLx() {
        setUpLOB(TripProducts.ACTIVITY.name)
        toolbarPairSubjectObserver.assertNoValues()

        sut.itinObserver.onChanged(noAddressLx)

        toolbarPairSubjectObserver.assertValue(Pair("California Academy of Sciences", ""))
    }

    @Test
    fun testGetNamePairNoAddressCar() {
        setUpLOB(TripProducts.CAR.name)
        toolbarPairSubjectObserver.assertNoValues()

        sut.itinObserver.onChanged(noAddressCar)

        toolbarPairSubjectObserver.assertValue(Pair("Thrifty", ""))
    }

    @Test
    fun testPublishLatLongHappyPathHotel() {
        setUpLOB(TripProducts.HOTEL.name)
        latLngSubjectObserver.assertNoValues()

        sut.itinObserver.onChanged(happyPathHotel)

        latLngSubjectObserver.assertValue(LatLng(13.014492, 77.583052))
    }

    @Test
    fun testPublishLatLongHappyPathLx() {
        setUpLOB(TripProducts.ACTIVITY.name)
        latLngSubjectObserver.assertNoValues()

        sut.itinObserver.onChanged(happyPathLx)

        latLngSubjectObserver.assertValue(LatLng(37.76974, -122.46614))
    }

    @Test
    fun testPublishLatLongHappyPathCar() {
        setUpLOB(TripProducts.CAR.name)
        latLngSubjectObserver.assertNoValues()

        sut.itinObserver.onChanged(happyPathCar)

        latLngSubjectObserver.assertValue(LatLng(-37.818294, 144.953432))
    }

    @Test
    fun testNoLat() {
        setUpLOB(TripProducts.HOTEL.name)
        latLngSubjectObserver.assertNoValues()
        sut.itinObserver.onChanged(noLatHotel)
        latLngSubjectObserver.assertNoValues()
        latLngSubjectObserver.dispose()

        setUpLOB(TripProducts.ACTIVITY.name)
        latLngSubjectObserver.assertNoValues()
        sut.itinObserver.onChanged(noLatLx)
        latLngSubjectObserver.assertNoValues()
        latLngSubjectObserver.dispose()

        setUpLOB(TripProducts.CAR.name)
        latLngSubjectObserver.assertNoValues()
        sut.itinObserver.onChanged(noLatCar)
        latLngSubjectObserver.assertNoValues()
    }

    @Test
    fun testNoLng() {
        setUpLOB(TripProducts.HOTEL.name)
        latLngSubjectObserver.assertNoValues()
        sut.itinObserver.onChanged(noLngHotel)
        latLngSubjectObserver.assertNoValues()
        latLngSubjectObserver.dispose()

        setUpLOB(TripProducts.ACTIVITY.name)
        latLngSubjectObserver.assertNoValues()
        sut.itinObserver.onChanged(noLngLx)
        latLngSubjectObserver.assertNoValues()
        latLngSubjectObserver.dispose()

        setUpLOB(TripProducts.CAR.name)
        latLngSubjectObserver.assertNoValues()
        sut.itinObserver.onChanged(noLngCar)
        latLngSubjectObserver.assertNoValues()
    }

    @Test
    fun testNoLatNoLng() {
        setUpLOB(TripProducts.ACTIVITY.name)

        latLngSubjectObserver.assertNoValues()

        sut.itinObserver.onChanged(noLatNoLng)

        latLngSubjectObserver.assertNoValues()
    }

    @Test
    fun testDirectionsButtonHotel() {
        setUpLOB(TripProducts.HOTEL.name)
        directionButtonClickSubjectObserver.assertNoValues()

        sut.itinObserver.onChanged(happyPathHotel)

        assertFalse(mockScope.mockTracking.trackItinMapDirectionsButtonCalled)
        assertFalse(mockScope.mockActivity.externalMapActivityLaunched)

        sut.directionButtonClickSubject.onNext(Unit)

        assertTrue(mockScope.mockTracking.trackItinMapDirectionsButtonCalled)
        assertTrue(mockScope.mockActivity.externalMapActivityLaunched)
    }

    @Test
    fun testDirectionsButtonLx() {
        setUpLOB(TripProducts.ACTIVITY.name)
        directionButtonClickSubjectObserver.assertNoValues()

        sut.itinObserver.onChanged(happyPathLx)

        assertFalse(mockScope.mockTracking.trackItinMapDirectionsButtonCalled)
        assertFalse(mockScope.mockActivity.externalMapActivityLaunched)

        sut.directionButtonClickSubject.onNext(Unit)

        assertTrue(mockScope.mockTracking.trackItinMapDirectionsButtonCalled)
        assertTrue(mockScope.mockActivity.externalMapActivityLaunched)
    }

    @Test
    fun testDirectionsButtonCar() {
        setUpLOB(TripProducts.CAR.name)
        directionButtonClickSubjectObserver.assertNoValues()

        sut.itinObserver.onChanged(happyPathCar)

        assertFalse(mockScope.mockTracking.trackItinMapDirectionsButtonCalled)
        assertFalse(mockScope.mockActivity.externalMapActivityLaunched)

        sut.directionButtonClickSubject.onNext(Unit)

        assertTrue(mockScope.mockTracking.trackItinMapDirectionsButtonCalled)
        assertTrue(mockScope.mockActivity.externalMapActivityLaunched)
    }

    private class MockItinExpandedMapViewModelScope(lob: String) : HasItinRepo, HasActivityLauncher, HasLifecycleOwner, HasItinType, HasTripsTracking {
        val mockTracking = MockTripsTracking()
        override val tripsTracking: ITripsTracking = mockTracking
        val mockActivity = MockActivityLauncher()
        override val activityLauncher: IActivityLauncher = mockActivity
        override val lifecycleOwner: LifecycleOwner = MockLifecycleOwner()
        val mockRepo = MockItinRepo()
        override val itinRepo: ItinRepoInterface = mockRepo
        override val type: String = lob
    }

    private fun setUpLOB(lob: String) {
        mockScope = MockItinExpandedMapViewModelScope(lob)
        sut = ItinExpandedMapViewModel(mockScope)
        sut.toolbarPairSubject.subscribe(toolbarPairSubjectObserver)
        sut.latLngSubject.subscribe(latLngSubjectObserver)
        sut.directionButtonClickSubject.subscribe(directionButtonClickSubjectObserver)
    }
}
