package com.expedia.bookings.hotel.widget

import android.app.Activity
import android.view.View
import android.widget.RadioButton
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.Neighborhood
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class ServerNeighborhoodFilterViewTest {

    private var neighborhoodFilterView: ServerNeighborhoodFilterView by Delegates.notNull()
    private var activity: Activity by Delegates.notNull()
    private var listener = object : OnHotelNeighborhoodFilterChangedListener {
        var neighborhoods = HashMap<Neighborhood, Boolean>()
        var doTracking = false

        override
        fun onHotelNeighborhoodFilterChanged(neighborhood: Neighborhood, selected: Boolean, doTracking: Boolean) {
            neighborhoods[neighborhood] = selected
            this.doTracking = doTracking
        }
    }

    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Default)
        Ui.getApplication(activity).defaultHotelComponents()
        neighborhoodFilterView = android.view.LayoutInflater.from(activity).inflate(R.layout.server_neighborhood_filter_view_test, null) as ServerNeighborhoodFilterView

        neighborhoodFilterView.setOnHotelNeighborhoodFilterChangedListener(listener)
        listener.neighborhoods.clear()
    }

    @Test
    fun testSelectNeighborhood() {
        val neighborhood1 = createNeighborhood("1", "1")
        val neighborhood2 = createNeighborhood("2", "2")
        neighborhoodFilterView.updateNeighborhoods(listOf(neighborhood1, neighborhood2))
        neighborhoodFilterView.selectNeighborhood(neighborhood2)

        assertFalse(neighborhoodFilterView.getRadioButtonAtIndex(0)!!.isChecked)
        assertTrue(neighborhoodFilterView.getRadioButtonAtIndex(1)!!.isChecked)
        assertNull(listener.neighborhoods[neighborhood1])
        assertTrue(listener.neighborhoods[neighborhood2]!!)
        assertFalse(listener.doTracking)
    }

    @Test
    fun testSelectNeighborhoodEmptyNeighborhoodGroup() {
        val neighborhood = createNeighborhood("", "")
        neighborhoodFilterView.selectNeighborhood(neighborhood)

        assertFalse(listener.neighborhoods[neighborhood]!!)
        assertFalse(listener.doTracking)
    }

    @Test
    fun testSelectNeighborhoodNotInNeighborhoodGroup() {
        val neighborhood1 = createNeighborhood("1", "1")
        val neighborhood2 = createNeighborhood("2", "2")
        neighborhoodFilterView.updateNeighborhoods(listOf(neighborhood2))
        neighborhoodFilterView.selectNeighborhood(neighborhood1)

        assertEquals(1, listener.neighborhoods.size)
        assertFalse(neighborhoodFilterView.getRadioButtonAtIndex(0)!!.isChecked)
        assertFalse(listener.neighborhoods[neighborhood1]!!)
        assertFalse(listener.doTracking)
    }

    @Test
    fun testGetRadioButtonAtIndex() {
        val neighborhood1 = createNeighborhood("1", "1")
        neighborhoodFilterView.updateNeighborhoods(listOf(neighborhood1))

        assertNull(neighborhoodFilterView.getRadioButtonAtIndex(-1))
        assertNull(neighborhoodFilterView.getRadioButtonAtIndex(1))
        val expectedRadioButton = neighborhoodFilterView.getNeighborhoodContainer().getChildAt(0)
        assertEquals(expectedRadioButton, neighborhoodFilterView.getRadioButtonAtIndex(0))
    }

    @Test
    fun testUpdateNeighborhoods() {
        val neighborhood1 = createNeighborhood("0", "0")
        val neighborhood2 = createNeighborhood("1", "1")
        val neighborhood3 = createNeighborhood("2", "2")
        val neighborhood4 = createNeighborhood("3", "3")

        neighborhoodFilterView.updateNeighborhoods(listOf(neighborhood1, neighborhood2, neighborhood3, neighborhood4))

        assertEquals(4, neighborhoodFilterView.getNeighborhoodContainer().childCount)
        for (i in 0 until 4) {
            assertTrue(neighborhoodFilterView.getNeighborhoodContainer().getChildAt(i) is RadioButton)
            assertEquals(i.toString(), neighborhoodFilterView.getRadioButtonAtIndex(i)!!.text)
        }
    }

    @Test
    fun testUpdateNeighborhoodsMoreOrLessViewVisibility() {
        val neighborhood1 = createNeighborhood("1", "1")
        val neighborhood2 = createNeighborhood("2", "2")
        neighborhoodFilterView.updateNeighborhoods(listOf(neighborhood1, neighborhood2))

        assertEquals(View.GONE, neighborhoodFilterView.moreLessView.visibility)

        val neighborhood3 = createNeighborhood("3", "3")
        val neighborhood4 = createNeighborhood("4", "4")

        neighborhoodFilterView.updateNeighborhoods(listOf(neighborhood1, neighborhood2, neighborhood3, neighborhood4))

        assertEquals(View.VISIBLE, neighborhoodFilterView.moreLessView.visibility)
    }

    @Test
    fun testClickNeighborhood() {
        val neighborhood1 = createNeighborhood("1", "1")
        val neighborhood2 = createNeighborhood("2", "2")
        neighborhoodFilterView.updateNeighborhoods(listOf(neighborhood1, neighborhood2))

        val radio1 = neighborhoodFilterView.getRadioButtonAtIndex(0)!!
        val radio2 = neighborhoodFilterView.getRadioButtonAtIndex(1)!!

        radio1.performClick()
        radio1.performClick()
        assertTrue(listener.neighborhoods[neighborhood1]!!)
        assertNull(listener.neighborhoods[neighborhood2])
        assertTrue(radio1.isChecked)
        assertFalse(radio2.isChecked)
        assertTrue(listener.doTracking)

        radio2.performClick()
        assertTrue(listener.neighborhoods[neighborhood1]!!)
        assertTrue(listener.neighborhoods[neighborhood2]!!)
        assertFalse(radio1.isChecked)
        assertTrue(radio2.isChecked)
        assertTrue(listener.doTracking)
    }

    @Test
    fun testMoreOrLessViewClick() {
        val neighborhood1 = createNeighborhood("0", "0")
        val neighborhood2 = createNeighborhood("1", "1")
        val neighborhood3 = createNeighborhood("2", "2")
        val neighborhood4 = createNeighborhood("3", "3")

        neighborhoodFilterView.updateNeighborhoods(listOf(neighborhood1, neighborhood2, neighborhood3, neighborhood4))

        assertFalse(neighborhoodFilterView.expanded)

        neighborhoodFilterView.moreLessView.callOnClick()

        assertTrue(neighborhoodFilterView.expanded)
    }

    @Test
    fun testCollapse() {
        val neighborhood1 = createNeighborhood("0", "0")
        val neighborhood2 = createNeighborhood("1", "1")
        val neighborhood3 = createNeighborhood("2", "2")
        val neighborhood4 = createNeighborhood("3", "3")

        neighborhoodFilterView.updateNeighborhoods(listOf(neighborhood1, neighborhood2, neighborhood3, neighborhood4))

        assertFalse(neighborhoodFilterView.expanded)

        neighborhoodFilterView.moreLessView.callOnClick()

        assertTrue(neighborhoodFilterView.expanded)

        neighborhoodFilterView.collapse()

        assertFalse(neighborhoodFilterView.expanded)
    }

    @Test
    fun testClear() {
        val neighborhood1 = createNeighborhood("1", "1")
        val neighborhood2 = createNeighborhood("2", "2")
        neighborhoodFilterView.updateNeighborhoods(listOf(neighborhood1, neighborhood2))
        neighborhoodFilterView.selectNeighborhood(neighborhood2)

        assertFalse(neighborhoodFilterView.getRadioButtonAtIndex(0)!!.isChecked)
        assertTrue(neighborhoodFilterView.getRadioButtonAtIndex(1)!!.isChecked)

        neighborhoodFilterView.clear()

        assertFalse(neighborhoodFilterView.getRadioButtonAtIndex(0)!!.isChecked)
        assertFalse(neighborhoodFilterView.getRadioButtonAtIndex(1)!!.isChecked)
    }

    private fun createNeighborhood(name: String, id: String): Neighborhood {
        return Neighborhood().apply {
            this.name = name
            this.id = id
        }
    }
}
