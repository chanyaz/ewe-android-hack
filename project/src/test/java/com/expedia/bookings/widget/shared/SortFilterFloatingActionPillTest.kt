package com.expedia.bookings.widget.shared

import android.app.Activity
import android.view.View
import android.widget.RelativeLayout
import com.expedia.bookings.R
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class SortFilterFloatingActionPillTest {

    private var pill: SortFilterFloatingActionPill by Delegates.notNull()
    private var activity: Activity by Delegates.notNull()

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        Ui.getApplication(activity).defaultHotelComponents()
        pill = android.view.LayoutInflater.from(activity).inflate(R.layout.hotel_search_floating_action_pill_layout_test, null) as SortFilterFloatingActionPill
    }

    @Test
    fun testFlightCompatibileMode() {
        pill.setFlightsCompatibleMode()

        assertEquals(View.GONE, pill.verticalDivider.visibility)
        assertEquals(View.GONE, pill.toggleContainer.visibility)
        assertEquals(activity.resources.getDimensionPixelSize(R.dimen.dimen_44), pill.filterButton.paddingLeft)
        assertEquals(activity.resources.getDimensionPixelSize(R.dimen.dimen_20), pill.filterButton.paddingRight)
        assertEquals(activity.resources.getDimensionPixelSize(R.dimen.dimen_20), (pill.filterCountText.layoutParams as RelativeLayout.LayoutParams).leftMargin)
        assertEquals(activity.resources.getDimensionPixelSize(R.dimen.dimen_20), (pill.filterIcon.layoutParams as RelativeLayout.LayoutParams).leftMargin)
    }
}
