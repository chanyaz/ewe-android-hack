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
    val happyPathLx = ItinMocker.lxDetailsHappy
    val happyPathCar = ItinMocker.carDetailsHappy
    val noTitleLx = ItinMocker.lxDetailsNoLat
    val noTitleCar = ItinMocker.carDetailsBadNameAndImage
    val noLng = ItinMocker.lxDetailsInvalidLatLong
    val noLngCar = ItinMocker.carDetailsBadPickupAndTimes
    val noLat = ItinMocker.lxDetailsNoDetailsUrl
    val noLatCar = ItinMocker.carDetailsBadNameAndImage
    val noLatNoLng = ItinMocker.lxDetailsNoLatLong
    val noAddressCar = ItinMocker.carDetailsBadPickupAndTimes
    val noAddressLx = ItinMocker.lxDetailsNoDetailsUrl
    private lateinit var mockScope: MockItinExpandedMapViewModelScope

    @Test
    fun testNullItin() {
        setUpLOB(TripProducts.ACTIVITY.name)
        toolbarPairSubjectObserver.assertNoValues()
        sut.itinObserver.onChanged(null)

        toolbarPairSubjectObserver.assertNoValues()
        toolbarPairSubjectObserver.dispose()
    }

    @Test
    fun testGetNamePairHappyPathLx() {
        setUpLOB(TripProducts.ACTIVITY.name)
        toolbarPairSubjectObserver.assertNoValues()
        sut.itinObserver.onChanged(happyPathLx)

        toolbarPairSubjectObserver.assertValue(Pair("California Academy of Sciences", "San Francisco, CA, USA, 94118"))
        toolbarPairSubjectObserver.dispose()
    }

    @Test
    fun testGetNamePairHappyPathCar() {
        setUpLOB(TripProducts.CAR.name)
        toolbarPairSubjectObserver.assertNoValues()

        sut.itinObserver.onChanged(happyPathCar)

        toolbarPairSubjectObserver.assertValue(Pair("Thrifty", "Docklands, Victoria, AUS, 98188"))
        toolbarPairSubjectObserver.dispose()
    }

    @Test
    fun testGetNamePairNoNameLx() {
        setUpLOB(TripProducts.ACTIVITY.name)
        toolbarPairSubjectObserver.assertNoValues()

        sut.itinObserver.onChanged(noTitleLx)

        toolbarPairSubjectObserver.assertValue(Pair(null, "San Francisco, CA, USA, 94118"))
        toolbarPairSubjectObserver.dispose()
    }

    @Test
    fun testGetNamePairNoNameCar() {
        setUpLOB(TripProducts.CAR.name)
        toolbarPairSubjectObserver.assertNoValues()

        sut.itinObserver.onChanged(noTitleCar)

        toolbarPairSubjectObserver.assertValue(Pair(null, "Docklands, Victoria, AUS, 98188"))
        toolbarPairSubjectObserver.dispose()
    }

    @Test
    fun testGetNamePairNoAddressLx() {
        setUpLOB(TripProducts.ACTIVITY.name)
        toolbarPairSubjectObserver.assertNoValues()

        sut.itinObserver.onChanged(noAddressLx)

        toolbarPairSubjectObserver.assertValue(Pair("California Academy of Sciences", ""))
        toolbarPairSubjectObserver.dispose()
    }

    @Test
    fun testGetNamePairNoAddressCar() {
        setUpLOB(TripProducts.CAR.name)
        toolbarPairSubjectObserver.assertNoValues()

        sut.itinObserver.onChanged(noAddressCar)

        toolbarPairSubjectObserver.assertValue(Pair("Thrifty", ""))
        toolbarPairSubjectObserver.dispose()
    }

    @Test
    fun testPublishLatLongHappyPathLx() {
        setUpLOB(TripProducts.ACTIVITY.name)
        latLngSubjectObserver.assertNoValues()

        sut.itinObserver.onChanged(happyPathLx)

        latLngSubjectObserver.assertValue(LatLng(37.76974, -122.46614))
        toolbarPairSubjectObserver.dispose()
    }

    @Test
    fun testPublishLatLongHappyPathCar() {
        setUpLOB(TripProducts.CAR.name)
        latLngSubjectObserver.assertNoValues()

        sut.itinObserver.onChanged(happyPathCar)

        latLngSubjectObserver.assertValue(LatLng(-37.818294, 144.953432))
        toolbarPairSubjectObserver.dispose()
    }

    @Test
    fun testNoLat() {
        setUpLOB(TripProducts.ACTIVITY.name)
        latLngSubjectObserver.assertNoValues()
        sut.itinObserver.onChanged(noLat)
        latLngSubjectObserver.assertNoValues()

        setUpLOB(TripProducts.CAR.name)
        latLngSubjectObserver.assertNoValues()
        sut.itinObserver.onChanged(noLatCar)
        latLngSubjectObserver.assertNoValues()

        latLngSubjectObserver.dispose()
    }

    @Test
    fun testNoLng() {
        setUpLOB(TripProducts.ACTIVITY.name)
        latLngSubjectObserver.assertNoValues()
        sut.itinObserver.onChanged(noLng)
        latLngSubjectObserver.assertNoValues()

        setUpLOB(TripProducts.CAR.name)
        latLngSubjectObserver.assertNoValues()
        sut.itinObserver.onChanged(noLngCar)
        latLngSubjectObserver.assertNoValues()

        latLngSubjectObserver.dispose()
    }

    @Test
    fun testNoLatNoLng() {
        setUpLOB(TripProducts.ACTIVITY.name)

        latLngSubjectObserver.assertNoValues()

        sut.itinObserver.onChanged(noLatNoLng)

        latLngSubjectObserver.assertNoValues()
        latLngSubjectObserver.dispose()
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
