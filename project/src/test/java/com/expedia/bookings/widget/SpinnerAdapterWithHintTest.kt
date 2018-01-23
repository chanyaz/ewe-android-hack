package com.expedia.bookings.widget

import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Strings
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class SpinnerAdapterWithHintTest {

    private val context = RuntimeEnvironment.application

    @Test
    fun testAdapter() {
        val options = listOf(1, 2, 3, 4, 5)
        val hint = "Hint"

        val adapter = SpinnerAdapterWithHint(context, hint, R.layout.rail_card_dropdown_item)
        adapter.dataSetChanged(options.map { SpinnerAdapterWithHint.SpinnerItem(it.toString(), it) })
        assertEquals(options.size, adapter.count)
        val hintView = adapter.getView(5, null, LinearLayout(context)) as TextView
        assertEquals(hint, hintView.hint)
        assertTrue(Strings.isEmpty(hintView.text))

        val selectableView = adapter.getView(1, null, LinearLayout(context)) as TextView
        assertEquals("2", selectableView.text)

        val testSpinner = Spinner(context)
        testSpinner.adapter = adapter
        val text = (testSpinner.adapter.getView(5, null, LinearLayout(context)) as TextView).text
        assertTrue(Strings.isEmpty(text))
        var expectedSelectionType: Any = Integer(1).javaClass
        var expectedSelectionValue: Any = 2
        testSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
                // Ignore
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = adapter.getItem(position)
                assertEquals(expectedSelectionType, selectedItem.item.javaClass)
                assertEquals(expectedSelectionValue.toString(), selectedItem.value)
            }
        }
        testSpinner.setSelection(1)
        expectedSelectionValue = 3
        testSpinner.setSelection(2)
        expectedSelectionType = Object().javaClass
        expectedSelectionValue = hint
        testSpinner.setSelection(5)
    }
}
