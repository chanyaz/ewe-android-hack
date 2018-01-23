package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.LXOfferDatesButton
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class LXOfferDatesButtonTest {
    private var datesButton: LXOfferDatesButton by Delegates.notNull()
    private var activity: Activity by Delegates.notNull()

    @Before fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        Ui.getApplication(activity).defaultLXComponents()
        activity.setTheme(R.style.V2_Theme_LX)
        datesButton = LayoutInflater.from(activity).inflate(R.layout.lx_offer_date_button, null) as LXOfferDatesButton
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY, MultiBrand.AIRASIAGO,
            MultiBrand.VOYAGES, MultiBrand.WOTIF, MultiBrand.LASTMINUTE, MultiBrand.EBOOKERS))
    fun testLXDatesWidget() {
        datesButton.bind(LocalDate(1989, 1, 14), false)
        assertEquals("Saturday\n14\n", datesButton.contentDescription.toString())
        assertEquals("Sat\n14\n", datesButton.text.toString())

        datesButton.bind(LocalDate(1989, 1, 14), true)
        datesButton.isChecked = true

        assertEquals("Saturday\n14\nJanuary", datesButton.contentDescription.toString())
        assertEquals("Sat\n14\nJan", datesButton.text.toString())
    }
}
