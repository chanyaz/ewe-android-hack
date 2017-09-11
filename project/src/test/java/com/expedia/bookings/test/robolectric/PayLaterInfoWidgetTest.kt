package com.expedia.bookings.test.robolectric


import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import android.app.Activity
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.widget.PayLaterInfoWidget
import com.expedia.bookings.widget.TextView
import org.junit.Test
import kotlin.properties.Delegates
import kotlin.test.assertEquals


@RunWith(RobolectricRunner::class)
@RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
class PayLaterInfoWidgetTest {
    private var widget: PayLaterInfoWidget by Delegates.notNull()

    @Before
    fun setup() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Default)
        widget = android.view.LayoutInflater.from(activity).inflate(R.layout.test_paylater_widget, null) as PayLaterInfoWidget
    }

    @Test
    fun testWidgetEarnField() {
        val earnText = widget.findViewById<View>(R.id.etp_earn_text) as TextView
        val earnLayout = widget.findViewById<View>(R.id.earn_text_layout) as LinearLayout
        assertEquals(View.GONE, earnLayout.visibility)//
        assertEquals("", earnText.text)
        assertEquals("Close", widget.toolbar.navigationContentDescription)
    }


}
