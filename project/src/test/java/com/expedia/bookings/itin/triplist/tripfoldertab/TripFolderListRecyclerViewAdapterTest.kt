package com.expedia.bookings.itin.triplist.tripfoldertab

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class TripFolderListRecyclerViewAdapterTest {

    private lateinit var context: Context
    private lateinit var viewGroup: ViewGroup
    private lateinit var viewAdapter: TripFolderListRecyclerViewAdapter
    private lateinit var mockTripFolderViewAdapterDelegate: MockViewAdapterDelegate
    private val tripFolderViewType = TripFolderListViewType.TRIP_FOLDER

    @Before
    fun setup() {
        context = RuntimeEnvironment.application
        viewGroup = FrameLayout(context)
        viewAdapter = TripFolderListRecyclerViewAdapter()
        mockTripFolderViewAdapterDelegate = MockViewAdapterDelegate(context, tripFolderViewType)
        viewAdapter.tripFolderViewAdapterDelegate = mockTripFolderViewAdapterDelegate
    }

    @Test
    fun testGetItemViewType() {
        assertFalse(mockTripFolderViewAdapterDelegate.isGetViewTypeCalled)
        assertEquals(tripFolderViewType.value, viewAdapter.getItemViewType(0))
        assertTrue(mockTripFolderViewAdapterDelegate.isGetViewTypeCalled)
    }

    @Test
    fun testOnCreateViewHolder() {
        assertFalse(mockTripFolderViewAdapterDelegate.isCreateViewCalled)

        val viewHolder = viewAdapter.onCreateViewHolder(viewGroup, tripFolderViewType.value)

        assertTrue(viewHolder.itemView is View)
        assertTrue(mockTripFolderViewAdapterDelegate.isCreateViewCalled)
    }

    @Test
    fun testGetItemCount() {
        viewAdapter.tripListItems = listOf(1, 2, 3)
        assertEquals(3, viewAdapter.itemCount)

        viewAdapter.tripListItems = emptyList()
        assertEquals(0, viewAdapter.itemCount)
    }

    @Test
    fun testOnBindViewHolder() {
        assertFalse(mockTripFolderViewAdapterDelegate.isBindViewCalled)
        val tripFolderViewHolder = viewAdapter.createViewHolder(viewGroup, tripFolderViewType.value)
        viewAdapter.tripListItems = listOf("SomeListItem")

        viewAdapter.bindViewHolder(tripFolderViewHolder, 0)
        assertTrue(mockTripFolderViewAdapterDelegate.isBindViewCalled)
    }

    @Test
    fun testUpdateTripListItems() {
        viewAdapter.tripListItems = listOf(1, 2, 3)
        val expected = listOf("a", "b", "c")
        viewAdapter.updateTripListItems(expected)
        assertEquals(expected, viewAdapter.tripListItems)
    }

    class MockViewAdapterDelegate(val context: Context, private val viewType: TripFolderListViewType) : IViewAdapterDelegate {
        var isGetViewTypeCalled = false
        var isItemForViewCalled = false
        var isCreateViewCalled = false
        var isBindViewCalled = false

        override fun getViewType(): Int {
            isGetViewTypeCalled = true
            return viewType.value
        }

        override fun isItemForView(item: Any): Boolean {
            isItemForViewCalled = true
            return true
        }

        override fun createView(parent: ViewGroup): View {
            isCreateViewCalled = true
            return View(context)
        }

        override fun bindView(view: View, item: Any) {
            isBindViewCalled = true
        }
    }
}
