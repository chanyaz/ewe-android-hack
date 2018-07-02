package com.expedia.bookings.itin.triplist.tripfoldertab

import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout
import com.expedia.bookings.data.trips.TripFolder
import com.expedia.bookings.itin.triplist.tripfolderlistitems.TripFolderItemView
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.google.gson.Gson
import com.mobiata.mocke3.getJsonStringFromMock
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class TripFolderViewAdapterDelegateTest {

    private lateinit var context: Context
    private lateinit var viewGroup: ViewGroup
    private lateinit var viewAdapterDelegate: TripFolderViewAdapterDelegate
    private val foldersJson = getJsonStringFromMock("api/trips/tripfolders/tripfolders_three_hotels_one_cruise.json", null)
    private val folders = Gson().fromJson(foldersJson, Array<TripFolder>::class.java).toList()
    private val folder = folders[0]

    @Before
    fun setup() {
        context = RuntimeEnvironment.application
        viewGroup = FrameLayout(context)
        viewAdapterDelegate = TripFolderViewAdapterDelegate(TripFolderListViewType.TRIP_FOLDER)
    }

    @Test
    fun testGetViewType() {
        assertEquals(TripFolderListViewType.TRIP_FOLDER.value, viewAdapterDelegate.getViewType())
    }

    @Test
    fun testIsItemForView() {
        assertFalse(viewAdapterDelegate.isItemForView(""))
        assertFalse(viewAdapterDelegate.isItemForView(123))
        assertTrue(viewAdapterDelegate.isItemForView(folder))
    }

    @Test
    fun testCreateView() {
        val actualView = viewAdapterDelegate.createView(viewGroup)
        assertTrue(actualView is TripFolderItemView)
        assertNotNull((actualView as TripFolderItemView).viewModel)
    }

    @Test
    fun testBindView() {
        val testObserver = TestObserver<TripFolder>()
        val testView = viewAdapterDelegate.createView(viewGroup) as TripFolderItemView
        testView.viewModel.bindTripFolderSubject.subscribe(testObserver)
        testObserver.assertEmpty()

        viewAdapterDelegate.bindView(testView, folder)
        testObserver.assertValueCount(1)
        testObserver.assertValue(folder)
    }
}
