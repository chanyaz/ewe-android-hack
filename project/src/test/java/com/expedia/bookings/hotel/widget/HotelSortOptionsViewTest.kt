package com.expedia.bookings.hotel.widget

import android.app.Activity
import android.view.MotionEvent
import com.expedia.bookings.R
import com.expedia.bookings.data.hotel.DisplaySort
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelSortOptionsViewTest {

    private var sortOptionsView: HotelSortOptionsView by Delegates.notNull()
    private var activity: Activity by Delegates.notNull()
    private var listener = object : OnHotelSortChangedListener {
        var displaySort = DisplaySort.getDefaultSort()
        var doTracking = false

        override fun onHotelSortChanged(displaySort: DisplaySort, doTracking: Boolean) {
            this.displaySort = displaySort
            this.doTracking = doTracking
        }
    }

    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Default)
        Ui.getApplication(activity).defaultHotelComponents()
        sortOptionsView = android.view.LayoutInflater.from(activity).inflate(R.layout.hotel_sort_options_view_test, null) as HotelSortOptionsView

        sortOptionsView.setOnHotelSortChangedListener(listener)

        sortOptionsView.updateSortItems(DisplaySort.values().toList())
    }

    @Test
    fun testSetSort() {
        val displaySort = DisplaySort.PRICE
        sortOptionsView.setSort(displaySort)

        assertEquals(DisplaySort.values().indexOf(displaySort), sortOptionsView.sortByButtonGroup.selectedItemPosition)
        assertEquals("Sort by Price", sortOptionsView.sortByButtonGroup.contentDescription)

        assertEquals(displaySort, listener.displaySort)
        assertFalse(listener.doTracking)
    }

    @Test
    fun testGetSortItems() {
        sortOptionsView.updateSortItems(DisplaySort.values().reversed().toList())
        val sortItems = sortOptionsView.getSortItems()
        val displaySortValues = DisplaySort.values().reversed()
        assertEquals(displaySortValues.size, sortItems.size)
        for (i in 0 until sortItems.size) {
            assertEquals(displaySortValues[i], sortItems[i])
        }
    }

    @Test
    fun testUpdateSortItems() {
        assertEquals(DisplaySort.values().size, sortOptionsView.getSortItems().size)
        val sortList = listOf(DisplaySort.DISTANCE, DisplaySort.DEALS, DisplaySort.PACKAGE_DISCOUNT)
        sortOptionsView.updateSortItems(sortList)
        assertEquals(sortList.size, sortOptionsView.getSortItems().size)

        assertEquals(0, sortOptionsView.sortByButtonGroup.selectedItemPosition)
        assertEquals("Sort by Distance", sortOptionsView.sortByButtonGroup.contentDescription)

        assertEquals(DisplaySort.DISTANCE, listener.displaySort)
        assertFalse(listener.doTracking)
    }

    @Test
    fun testSortByButtonGroupOnTouch() {
        val testObserver = TestObserver<Unit>()
        sortOptionsView.downEventSubject.subscribe(testObserver)
        val press = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 0f, 0f, 0)
        sortOptionsView.sortByButtonGroup.dispatchTouchEvent(press)
        testObserver.assertValue(Unit)
    }

    @Test
    fun testOnItemSelected() {
        sortOptionsView.sortByButtonGroup.onItemSelectedListener.onItemSelected(null, null, 2, 2)

        assertEquals("Sort by Deals", sortOptionsView.sortByButtonGroup.contentDescription)
        assertEquals(DisplaySort.DEALS, listener.displaySort)
        assertTrue(listener.doTracking)
    }
}
