package com.expedia.bookings.itin.triplist.tripfolderlistitems

import android.app.Activity
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.TripFolder
import com.expedia.bookings.data.trips.TripFolderProduct
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class TripFolderItemViewTest {

    private lateinit var view: TripFolderItemView
    private val viewModel = MockViewModel()

    @Before
    fun setup() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().start().get()
        view = LayoutInflater.from(activity).inflate(R.layout.trip_folder_list_item, null) as TripFolderItemView
        view.viewModel = viewModel
    }

    @Test
    fun testTitle() {
        val testObserver = TestObserver<String>()
        viewModel.titleSubject.subscribe(testObserver)
        testObserver.assertEmpty()

        val expected = "Trip to Vancouver"
        viewModel.titleSubject.onNext(expected)
        testObserver.assertValueCount(1)
        testObserver.assertValue(expected)
        assertEquals(expected, view.folderTitle.text)
    }

    @Test
    fun testTiming() {
        val testObserver = TestObserver<String>()
        viewModel.timingSubject.subscribe(testObserver)
        testObserver.assertEmpty()

        val expected = "Nov 2 - 5"
        viewModel.timingSubject.onNext(expected)
        testObserver.assertValueCount(1)
        testObserver.assertValue(expected)
        assertEquals(expected, view.folderTiming.text)
    }

    @Test
    fun testLobIcons() {
        val testObserver = TestObserver<List<TripFolderProduct>>()
        viewModel.lobIconSubject.subscribe(testObserver)
        testObserver.assertEmpty()

        val expected = listOf(TripFolderProduct.HOTEL, TripFolderProduct.FLIGHT)
        viewModel.lobIconSubject.onNext(expected)
        testObserver.assertValueCount(1)
        assertTrue(testObserver.values()[0].containsAll(expected))
        assertEquals(2, view.folderLobIconContainer.childCount)

        viewModel.lobIconSubject.onNext(emptyList())
        testObserver.assertValueAt(1, emptyList())
        assertEquals(0, view.folderLobIconContainer.childCount)
    }

    class MockViewModel : ITripFolderItemViewModel {
        override val bindTripFolderSubject: PublishSubject<TripFolder> = PublishSubject.create()
        override val titleSubject: PublishSubject<String> = PublishSubject.create()
        override val timingSubject: PublishSubject<String> = PublishSubject.create()
        override val lobIconSubject: PublishSubject<List<TripFolderProduct>> = PublishSubject.create()
    }
}
