package com.expedia.bookings.test.robolectric

import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.features.Features
import com.expedia.bookings.fragment.CalendarDialogFragment
import com.expedia.bookings.lob.lx.ui.viewmodel.LXSearchViewModel
import com.expedia.bookings.utils.FeatureTestUtils
import com.expedia.bookings.utils.LxCalendarRules
import com.expedia.bookings.utils.Ui
import kotlinx.android.synthetic.main.change_dates_calendar_picker.view.*
import kotlinx.android.synthetic.main.spinner_date_picker.view.*
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(RobolectricRunner::class)
class LXCalendarDialogFragmentTest {
    private val context = RuntimeEnvironment.application
    var activity: FragmentActivity by Delegates.notNull()
    var vm: LXSearchViewModel by Delegates.notNull()
    private val testRules = LxCalendarRules(context)
    private lateinit var fragmentManager: FragmentManager
    var testFragment: CalendarDialogFragment by Delegates.notNull()

    @Before
    fun before() {
        FeatureTestUtils.enableFeature(context, Features.all.lxMultipleDatesSearch)
        activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_LX)
        Ui.getApplication(activity).defaultLXComponents()
        vm = LXSearchViewModel(activity)
        testFragment = CalendarDialogFragment(vm, testRules)

        fragmentManager = activity.supportFragmentManager
        testFragment.show(fragmentManager, "dummy_tag")
    }

    @Test
    fun testWithPresetDates() {
        val expectedStart = LocalDate.now()
        val expectedEnd = null

        val view = testFragment.onCreateView(LayoutInflater.from(context), null, null)
        testFragment.onViewCreated(view, null)
        testFragment.onCreateDialog(null)

        testFragment.calendar.setSelectedDates(expectedStart, expectedEnd)
        assertNotNull(testFragment.calendar)
        testFragment.calendar.let { picker ->
            assertEquals(expectedStart, picker.startDate)
            assertEquals(expectedEnd, picker.endDate)
        }
        testFragment.calendar.performClick()
        testFragment.calendar.let { picker ->
            assertEquals(expectedStart, picker.startDate)
        }
    }
}
