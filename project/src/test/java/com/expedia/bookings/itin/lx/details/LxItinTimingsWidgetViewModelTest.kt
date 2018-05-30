package com.expedia.bookings.itin.lx.details

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.LifecycleOwner
import com.expedia.bookings.R
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.helpers.MockLifecycleOwner
import com.expedia.bookings.itin.helpers.MockLxRepo
import com.expedia.bookings.itin.helpers.MockStringProvider
import com.expedia.bookings.itin.lx.ItinLxRepoInterface
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasLxRepo
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.tripstore.extensions.firstLx
import com.expedia.bookings.itin.utils.StringSource
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LxItinTimingsWidgetViewModelTest {
    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()
    private lateinit var sut: LxItinTimingsWidgetViewModel<MockScope>
    private lateinit var mockScope: MockScope
    val endTitleTestObserver = TestObserver<String>()
    val endDateTestObserver = TestObserver<String>()
    val endTimeTestObserver = TestObserver<String>()
    val startTimeObserver = TestObserver<String>()
    val startDateTestObserver = TestObserver<String>()
    val startTitleTestObserver = TestObserver<String>()

    @Before
    fun setup() {
        mockScope = MockScope()
        sut = LxItinTimingsWidgetViewModel(mockScope)
        sut.endDateSubject.subscribe(endDateTestObserver)
        sut.endTitleSubject.subscribe(endTitleTestObserver)
        sut.endTimeSubject.subscribe(endTimeTestObserver)
        sut.startDateSubject.subscribe(startDateTestObserver)
        sut.startTitleSubject.subscribe(startTitleTestObserver)
        sut.startTimeSubject.subscribe(startTimeObserver)
    }

    @Test
    fun happyPathTest() {
        endTitleTestObserver.assertNoValues()
        endDateTestObserver.assertNoValues()
        endTimeTestObserver.assertNoValues()
        startTimeObserver.assertNoValues()
        startDateTestObserver.assertNoValues()
        startTitleTestObserver.assertNoValues()

        sut.itinObserver.onChanged(ItinMocker.lxDetailsAlsoHappy.firstLx())

        endTimeTestObserver.assertValue("9:30am")
        endDateTestObserver.assertValue("Wed, Oct 24")
        endTitleTestObserver.assertValue(R.string.itin_expires.toString())
        startTimeObserver.assertValue("9:30am")
        startDateTestObserver.assertValue("Wed, Oct 24")
        startTitleTestObserver.assertValue(R.string.itin_active.toString())
    }

    @Test
    fun sadPathTest() {
        endTitleTestObserver.assertNoValues()
        endDateTestObserver.assertNoValues()
        endTimeTestObserver.assertNoValues()
        startTimeObserver.assertNoValues()
        startDateTestObserver.assertNoValues()
        startTitleTestObserver.assertNoValues()

        sut.itinObserver.onChanged(ItinMocker.lxDetailsNoDates.firstLx())

        endTitleTestObserver.assertValue(R.string.itin_expires.toString())
        endDateTestObserver.assertNoValues()
        endTimeTestObserver.assertNoValues()
        startTimeObserver.assertNoValues()
        startDateTestObserver.assertNoValues()
        startTitleTestObserver.assertValue(R.string.itin_active.toString())
    }

    private class MockScope : HasStringProvider, HasLxRepo, HasLifecycleOwner {
        override val strings: StringSource = MockStringProvider()
        override val lifecycleOwner: LifecycleOwner = MockLifecycleOwner()
        override val itinLxRepo: ItinLxRepoInterface = MockLxRepo()
    }
}
