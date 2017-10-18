package com.expedia.bookings.widget.rail

import android.app.Activity
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.responses.RailCard
import com.expedia.bookings.services.RailServices
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.rail.widget.RailCardPickerRowView
import com.expedia.bookings.rail.widget.RailCardPickerView
import com.expedia.vm.RailCardPickerViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class RailCardPickerViewTest {
    var cardPickerView by Delegates.notNull<RailCardPickerView>()
    var railServicesRule = ServicesRule(RailServices::class.java)
        @Rule get

    private val context = RuntimeEnvironment.application

    @Before
    fun before() {
        val viewModel = RailCardPickerViewModel(railServicesRule.services!!, context)
        val testSubscriber = TestSubscriber.create<List<RailCard>>()
        viewModel.railCardTypes.subscribe(testSubscriber)
        testSubscriber.awaitTerminalEvent(2, TimeUnit.SECONDS)

        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        Ui.getApplication(activity).defaultRailComponents()
        cardPickerView = LayoutInflater.from(activity).inflate(R.layout.widget_rail_card_search, null) as RailCardPickerView
        cardPickerView.viewModel = viewModel
    }

    @Test
    fun testPickerWithAddRemove() {
        assertTrue(cardPickerView.addButton.isEnabled)
        assertFalse(cardPickerView.removeButton.isEnabled)

        assertEquals(3, cardPickerView.childCount)
        val row1 = cardPickerView.getChildAt(2) as RailCardPickerRowView
        assertNotNull(row1)
        assertEquals(0, row1.viewModel.rowId)

        cardPickerView.addButton.performClick()
        assertEquals(4, cardPickerView.childCount)
        val row2 = cardPickerView.getChildAt(3) as RailCardPickerRowView
        assertNotNull(row2)
        assertEquals(1, row2.viewModel.rowId)
        assertTrue(cardPickerView.removeButton.isEnabled)

        cardPickerView.removeButton.performClick()
        assertEquals(3, cardPickerView.childCount)
        assertFalse(cardPickerView.removeButton.isEnabled)
        assertEquals(0, (cardPickerView.getChildAt(2) as RailCardPickerRowView).viewModel.rowId)
    }
}