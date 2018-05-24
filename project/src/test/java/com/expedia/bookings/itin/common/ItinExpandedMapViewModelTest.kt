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
import org.junit.Before
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
    val happyPath = ItinMocker.lxDetailsHappy
    val noTitle = ItinMocker.lxDetailsNoLat
    val noLng = ItinMocker.lxDetailsInvalidLatLong
    val noLat = ItinMocker.lxDetailsNoDetailsUrl
    val noLatNoLng = ItinMocker.lxDetailsNoLatLong
    private lateinit var mockScope: MockItinExpandedMapViewModelScope

    @Before
    fun setUp() {
        mockScope = MockItinExpandedMapViewModelScope()
        sut = ItinExpandedMapViewModel(mockScope)
        sut.toolbarPairSubject.subscribe(toolbarPairSubjectObserver)
        sut.latLngSubject.subscribe(latLngSubjectObserver)
        sut.directionButtonClickSubject.subscribe(directionButtonClickSubjectObserver)
    }

    @Test
    fun testNullItin() {
        toolbarPairSubjectObserver.assertNoValues()
        sut.itinObserver.onChanged(null)
        toolbarPairSubjectObserver.assertNoValues()
        toolbarPairSubjectObserver.dispose()
    }

    @Test
    fun testGetNamePairHappyPath() {
        toolbarPairSubjectObserver.assertNoValues()
        sut.itinObserver.onChanged(happyPath)
        toolbarPairSubjectObserver.assertValue(Pair("California Academy of Sciences", "San Francisco, CA, USA, 94118"))
        toolbarPairSubjectObserver.dispose()
    }

    @Test
    fun testGetNamePairInvalid() {
        toolbarPairSubjectObserver.assertNoValues()
        sut.itinObserver.onChanged(noTitle)
        toolbarPairSubjectObserver.assertValue(Pair(null, "San Francisco, CA, USA, 94118"))
        toolbarPairSubjectObserver.dispose()
    }

    @Test
    fun testPublishLatLongHappyPath() {
        latLngSubjectObserver.assertNoValues()
        sut.itinObserver.onChanged(happyPath)
        latLngSubjectObserver.assertValue(LatLng(37.76974, -122.46614))
        toolbarPairSubjectObserver.dispose()
    }

    @Test
    fun testNoLat() {
        latLngSubjectObserver.assertNoValues()
        sut.itinObserver.onChanged(noLat)
        latLngSubjectObserver.assertNoValues()
        latLngSubjectObserver.dispose()
    }

    @Test
    fun testNoLng() {
        latLngSubjectObserver.assertNoValues()
        sut.itinObserver.onChanged(noLng)
        latLngSubjectObserver.assertNoValues()
        latLngSubjectObserver.dispose()
    }

    @Test
    fun testNoLatNoLng() {
        latLngSubjectObserver.assertNoValues()
        sut.itinObserver.onChanged(noLatNoLng)
        latLngSubjectObserver.assertNoValues()
        latLngSubjectObserver.dispose()
    }

    @Test
    fun testDirectionsButton() {
        directionButtonClickSubjectObserver.assertNoValues()

        sut.itinObserver.onChanged(happyPath)

        assertFalse(mockScope.mockTracking.trackItinMapDirectionsButtonCalled)
        assertFalse(mockScope.mockActivity.externalMapActivityLaunched)

        sut.directionButtonClickSubject.onNext(Unit)

        assertTrue(mockScope.mockTracking.trackItinMapDirectionsButtonCalled)
        assertTrue(mockScope.mockActivity.externalMapActivityLaunched)
    }

    private class MockItinExpandedMapViewModelScope : HasItinRepo, HasActivityLauncher, HasLifecycleOwner, HasItinType, HasTripsTracking {
        val mockTracking = MockTripsTracking()
        override val tripsTracking: ITripsTracking = mockTracking
        val mockActivity = MockActivityLauncher()
        override val activityLauncher: IActivityLauncher = mockActivity
        override val lifecycleOwner: LifecycleOwner = MockLifecycleOwner()
        val mockRepo = MockItinRepo()
        override val itinRepo: ItinRepoInterface = mockRepo
        override val type: String = TripProducts.ACTIVITY.name
    }
}
