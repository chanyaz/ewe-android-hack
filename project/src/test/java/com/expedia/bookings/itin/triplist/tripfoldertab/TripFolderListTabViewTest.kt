package com.expedia.bookings.itin.triplist.tripfoldertab

import com.expedia.bookings.data.trips.TripFolder
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.google.gson.Gson
import com.mobiata.mocke3.getJsonStringFromMock
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class TripFolderListTabViewTest {

    private lateinit var view: TripFolderListTabView
    private lateinit var viewModel: ITripFolderListTabViewModel

    @Before
    fun setup() {
        view = TripFolderListTabView(RuntimeEnvironment.application, null)
        viewModel = MockViewModel()
        view.viewModel = viewModel
    }

    @Test
    fun testFoldersSubjectEmptyList() {
        val testObserver = TestObserver<List<TripFolder>>()
        viewModel.foldersSubject.subscribe(testObserver)
        testObserver.assertEmpty()

        viewModel.foldersSubject.onNext(emptyList())
        testObserver.assertValueCount(1)
        testObserver.assertValue(emptyList())
        assertEquals(0, view.tripListRecyclerView.adapter.itemCount)
    }

    @Test
    fun testFoldersSubjectWithFolders() {
        val testObserver = TestObserver<List<TripFolder>>()
        viewModel.foldersSubject.subscribe(testObserver)
        testObserver.assertEmpty()

        val foldersJson = getJsonStringFromMock("api/trips/tripfolders/tripfolders_three_hotels_one_cruise.json", null)
        val folders = Gson().fromJson(foldersJson, Array<TripFolder>::class.java).toList()
        viewModel.foldersSubject.onNext(folders)
        testObserver.assertValueCount(1)
        assertTrue(testObserver.values()[0].containsAll(folders))
        assertEquals(4, view.tripListRecyclerView.adapter.itemCount)
    }

    class MockViewModel : ITripFolderListTabViewModel {
        override val foldersSubject: PublishSubject<List<TripFolder>> = PublishSubject.create()
    }
}
