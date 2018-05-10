package com.expedia.bookings.itin.lx.details

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.LifecycleOwner
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.helpers.MockLifecycleOwner
import com.expedia.bookings.itin.helpers.MockLxRepo
import com.expedia.bookings.itin.helpers.MockTripsTracking
import com.expedia.bookings.itin.lx.ItinLxRepoInterface
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasLxRepo
import com.expedia.bookings.itin.scopes.HasTripsTracking
import com.expedia.bookings.itin.tripstore.extensions.firstLx
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

    @Before
    fun setup() {
        sut = LxItinMapWidgetViewModel(MockVMScope())
        sut.addressLineFirstSubject.subscribe(addressLineFirstTestObserver)
        sut.addressLineSecondSubject.subscribe(addressLineSecondTestObserver)
        sut.latLongSubject.subscribe(latLongTestObserver)
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
    fun faultyLxItinLongNullTest() {
        latLongTestObserver.assertNoValues()
        sut.scope.mockRepo.liveDataLx.value = ItinMocker.lxDetailsNoLat.firstLx()
        latLongTestObserver.assertNoValues()
    }

    @Test
    fun happyTest() {
        addressLineFirstTestObserver.assertNoValues()
        addressLineSecondTestObserver.assertNoValues()
        latLongTestObserver.assertNoValues()

        assertFalse(sut.scope.mockTracking.directionClicked)
        assertFalse(sut.scope.mockTracking.mapClicked)

        sut.itinLxObserver.onChanged(ItinMocker.lxDetailsHappy.firstLx())
        addressLineFirstTestObserver.assertValue("55 Music Concourse Drive")
        addressLineSecondTestObserver.assertValue("San Francisco, CA, USA, 94118")
        latLongTestObserver.assertValue(LatLng(37.76974, -122.46614))

        sut.mapClickSubject.onNext(Unit)
        sut.directionButtonClickSubject.onNext(Unit)

        assertTrue(sut.scope.mockTracking.directionClicked)
        assertTrue(sut.scope.mockTracking.mapClicked)
    }

    private class MockVMScope : HasLifecycleOwner, HasLxRepo, HasTripsTracking {
        val mockTracking = MockTripsTracking()
        override val tripsTracking: ITripsTracking = mockTracking
        override val lifecycleOwner: LifecycleOwner = MockLifecycleOwner()
        val mockRepo = MockLxRepo(false)
        override val itinLxRepo: ItinLxRepoInterface = mockRepo
    }
}
