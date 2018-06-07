package com.expedia.bookings.itin.triplist

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
class TripListAdapterTest {
    private lateinit var context: Context
    private lateinit var adapter: TripListAdapter

    @Before
    fun setup() {
        context = RuntimeEnvironment.application
        adapter = TripListAdapter(context)
    }

    @Test
    fun testIsViewFromObject() {
        var view: View? = View(context)
        var obj: Any? = ""
        assertFalse(adapter.isViewFromObject(view, obj))

        obj = null
        assertFalse(adapter.isViewFromObject(view, obj))

        view = null
        obj = ""
        assertFalse(adapter.isViewFromObject(view, obj))

        view = null
        obj = null
        assertTrue(adapter.isViewFromObject(view, obj))

        view = View(context)
        obj = view
        assertTrue(adapter.isViewFromObject(view, obj))
    }

    @Test
    fun testGetCount() {
        assertEquals(3, adapter.count)
    }

    @Test
    fun testGetPageTitle() {
        assertEquals("Upcoming", adapter.getPageTitle(0))
        assertEquals("Past", adapter.getPageTitle(1))
        assertEquals("Cancelled", adapter.getPageTitle(2))
    }

    @Test
    fun testInstantiateItem() {
        val viewGroup = FrameLayout(context)
        assertEquals(0, viewGroup.childCount)

        var returnedView = adapter.instantiateItem(viewGroup, 0)
        assertEquals(1, viewGroup.childCount)
        assertEquals(adapter.upcomingTripListView, returnedView)

        viewGroup.removeAllViews()
        assertEquals(0, viewGroup.childCount)

        returnedView = adapter.instantiateItem(viewGroup, 1)
        assertEquals(1, viewGroup.childCount)
        assertEquals(adapter.pastTripListView, returnedView)

        viewGroup.removeAllViews()
        assertEquals(0, viewGroup.childCount)

        returnedView = adapter.instantiateItem(viewGroup, 2)
        assertEquals(1, viewGroup.childCount)
        assertEquals(adapter.cancelledTripListView, returnedView)
    }

    @Test
    fun testDestroyItem() {
        val viewGroup = FrameLayout(context)
        viewGroup.addView(adapter.upcomingTripListView)
        viewGroup.addView(adapter.pastTripListView)
        viewGroup.addView(adapter.cancelledTripListView)

        assertEquals(adapter.upcomingTripListView, viewGroup.getChildAt(0))
        assertEquals(adapter.pastTripListView, viewGroup.getChildAt(1))
        assertEquals(adapter.cancelledTripListView, viewGroup.getChildAt(2))

        adapter.destroyItem(viewGroup, 0, adapter.upcomingTripListView)
        assertFalse(getChildren(viewGroup).contains(adapter.upcomingTripListView))
        assertEquals(2, viewGroup.childCount)

        adapter.destroyItem(viewGroup, 1, adapter.pastTripListView)
        assertFalse(getChildren(viewGroup).contains(adapter.pastTripListView))
        assertEquals(1, viewGroup.childCount)

        adapter.destroyItem(viewGroup, 2, adapter.cancelledTripListView)
        assertFalse(getChildren(viewGroup).contains(adapter.cancelledTripListView))
        assertEquals(0, viewGroup.childCount)
    }

    private fun getChildren(viewGroup: ViewGroup): List<View> {
        val children = mutableListOf<View>()
        for (i in 0 until viewGroup.childCount) {
            children.add(viewGroup.getChildAt(i))
        }
        return children
    }
}
