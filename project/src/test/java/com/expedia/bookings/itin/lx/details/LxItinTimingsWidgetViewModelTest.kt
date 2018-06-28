package com.expedia.bookings.itin.lx.details

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.LifecycleOwner
import com.expedia.bookings.R
import com.expedia.bookings.itin.common.ItinRepoInterface
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.helpers.MockItinRepo
import com.expedia.bookings.itin.helpers.MockLifecycleOwner
import com.expedia.bookings.itin.helpers.MockStringProvider
import com.expedia.bookings.itin.scopes.HasItinRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasStringProvider
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
    private val endTitleTestObserver = TestObserver<String>()
    private val endDateTestObserver = TestObserver<String>()
    private val endTimeTestObserver = TestObserver<String>()
    private val startTimeObserver = TestObserver<String>()
    private val startDateTestObserver = TestObserver<String>()
    private val startTitleTestObserver = TestObserver<String>()

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

        sut.itinObserver.onChanged(ItinMocker.lxDetailsAlsoHappy)

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

        sut.itinObserver.onChanged(ItinMocker.lxDetailsNoDates)

        endTitleTestObserver.assertValue(R.string.itin_expires.toString())
        endDateTestObserver.assertNoValues()
        endTimeTestObserver.assertNoValues()
        startTimeObserver.assertNoValues()
        startDateTestObserver.assertNoValues()
        startTitleTestObserver.assertValue(R.string.itin_active.toString())
    }

    private class MockScope : HasStringProvider, HasItinRepo, HasLifecycleOwner {
        override val strings: StringSource = MockStringProvider()
        override val lifecycleOwner: LifecycleOwner = MockLifecycleOwner()
        override val itinRepo: ItinRepoInterface = MockItinRepo()
    }
}
