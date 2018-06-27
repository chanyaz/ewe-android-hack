package com.expedia.bookings.itin.triplist

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.MutableLiveData
import android.graphics.drawable.ColorDrawable
import android.support.design.widget.TabLayout
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.TripFolder
import com.expedia.bookings.itin.helpers.MockTripListRepository
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
class TripListFragmentTest {
    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()
    private lateinit var activity: AppCompatActivity
    private lateinit var fragmentManager: FragmentManager
    private val testFragment = TripListFragment()
    private val mockRepository = MockTripListRepository()

    @Before
    fun setup() {
        RuntimeEnvironment.application.setTheme(R.style.ItinTheme)
        activity = Robolectric.setupActivity(AppCompatActivity::class.java)
        fragmentManager = activity.supportFragmentManager
    }

    @Test
    fun testViewInflation() {
        assertNull(testFragment.view)
        loadTripListFragment()
        assertNotNull(testFragment.view)

        val tabLayout = testFragment.view!!.findViewById<TabLayout>(R.id.trip_list_tabs)
        val upcomingTab = tabLayout.getTabAt(0)!!
        assertEquals("Upcoming", upcomingTab.text)
        val pastTab = tabLayout.getTabAt(1)!!
        assertEquals("Past", pastTab.text)
        val cancelledTab = tabLayout.getTabAt(2)!!
        assertEquals("Cancelled", cancelledTab.text)
    }

    @Test
    fun testToolbar() {
        loadTripListFragment()

        val toolbar = testFragment.view!!.findViewById<Toolbar>(R.id.trip_list_toolbar)
        val toolbarBackGroundColor = (toolbar.background as ColorDrawable).color
        assertEquals(View.VISIBLE, toolbar.visibility)
        assertEquals(ContextCompat.getColor(activity, R.color.brand_primary), toolbarBackGroundColor)
    }

    @Test
    fun testTabLayout() {
        loadTripListFragment()

        val tabLayout = testFragment.view!!.findViewById<TabLayout>(R.id.trip_list_tabs)
        val tabLayoutColor = (tabLayout.background as ColorDrawable).color
        assertEquals(View.VISIBLE, tabLayout.visibility)
        assertEquals(ContextCompat.getColor(activity, R.color.brand_primary), tabLayoutColor)
    }

    @Test
    fun testTrackTripListVisit() {
        loadTripListFragment()
        val testObserver = TestObserver<Int>()
        testFragment.viewModel.tripListVisitTrackingSubject.subscribe(testObserver)
        testObserver.assertNoValues()
        testFragment.trackTripListVisit()
        testObserver.assertValueCount(1)
        testObserver.assertValuesAndClear(0)

        val tabLayout = testFragment.view!!.findViewById<TabLayout>(R.id.trip_list_tabs)
        testObserver.assertNoValues()
        tabLayout.getTabAt(2)?.select()
        testFragment.trackTripListVisit()
        testObserver.assertValueCount(1)
        testObserver.assertValuesAndClear(2)
    }

    @Test
    fun testTabSelect() {
        loadTripListFragment()
        val testObserver = TestObserver<Int>()
        testFragment.viewModel.tabSelectedSubject.subscribe(testObserver)
        testObserver.assertNoValues()

        val tabLayout = testFragment.view!!.findViewById<TabLayout>(R.id.trip_list_tabs)
        tabLayout.getTabAt(2)?.select()
        testObserver.assertValueCount(1)
        testObserver.assertValue(2)
    }

    @Test
    fun testFolderLivedataObserver() {
        val testObserver = TestObserver<List<TripFolder>>()
        testFragment.tripListAdapterViewModel.upcomingTripFoldersSubject.subscribe(testObserver)

        testObserver.assertNoValues()
        loadTripListFragment()
        testFragment.setupLiveDataObservers()
        testObserver.assertValueCount(1)
        testObserver.assertValuesAndClear(mockRepository.tripFolders)

        testFragment.viewModel.refreshTripFolders()
        testObserver.assertValueCount(1)
        testObserver.assertValuesAndClear(mockRepository.tripFoldersForRefresh)
    }

    private fun loadTripListFragment() {
        fragmentManager.beginTransaction().add(testFragment, "TRIP_LIST_FRAGMENT").commitNow()
        testFragment.viewModel = MockViewModel(mockRepository)
    }

    private class MockViewModel(val mockRepository: MockTripListRepository) : ITripListFragmentViewModel {
        override val upcomingFoldersLiveData: MutableLiveData<List<TripFolder>> = MutableLiveData()
        override val tripListVisitTrackingSubject: PublishSubject<Int> = PublishSubject.create()
        override val tabSelectedSubject: PublishSubject<Int> = PublishSubject.create()

        override fun refreshTripFolders() {
            upcomingFoldersLiveData.postValue(mockRepository.tripFoldersForRefresh)
        }

        init {
            upcomingFoldersLiveData.postValue(mockRepository.tripFolders)
        }
    }
}
