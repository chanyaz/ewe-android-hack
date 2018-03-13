package com.expedia.bookings.itin.widget

import android.content.Context
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.itin.common.ItinToolbarWithSpinner
import com.expedia.bookings.test.robolectric.RobolectricRunner
import kotlinx.android.synthetic.main.widget_toolbar_with_spinner.view.toolbar_spinner
import kotlinx.android.synthetic.main.widget_toolbar_with_spinner.view.toolbar_button
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
class ItinToolbarWithSpinnerTest {
    lateinit var context: Context
    lateinit var sut: ItinToolbarWithSpinner

    @Before
    fun setup() {
        context = RuntimeEnvironment.application
        sut = LayoutInflater.from(context).inflate(R.layout.test_itin_toolbar_with_spinner, null) as ItinToolbarWithSpinner
    }

    @Test
    fun testSetSpinnerList() {
        assertNull(sut.toolbar_spinner.adapter)
        sut.setSpinnerList(list = ArrayList<String>())
        assertNull(sut.toolbar_spinner.adapter)
        sut.setSpinnerList(list = null)
        assertNull(sut.toolbar_spinner.adapter)
        sut.setSpinnerList(listOf("123", "1234"))
        assertEquals(2, sut.toolbar_spinner.adapter.count)
    }

    @Test
    fun testSetButtonText() {
        sut.setButtonText(R.string.legend)
        assertEquals(context.getString(R.string.legend), sut.toolbar_button.text.toString())
        assertEquals(context.getString(R.string.legend) + " " + context.getString(R.string.accessibility_cont_desc_role_button),
                sut.toolbar_button.contentDescription.toString())
    }
}
