package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.lx.Ticket
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.LXTicketPicker
import com.google.gson.GsonBuilder
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(RobolectricRunner::class)
@RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ))
class LXTicketPickerTest {
    private var picker: LXTicketPicker by Delegates.notNull()
    private var activity: Activity by Delegates.notNull()

    @Before fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        Ui.getApplication(activity).defaultLXComponents()
        activity.setTheme(R.style.V2_Theme_LX)
        picker = LayoutInflater.from(activity).inflate(R.layout.lx_ticket_picker, null) as LXTicketPicker
    }

    @Test fun testTicketPicker() {
        picker.bind(buildTicket(), "", 1, false)

        val ticketCount = picker.findViewById<View>(R.id.ticket_count) as TextView
        val ticketAdd = picker.findViewById<View>(R.id.ticket_add) as ImageButton
        val ticketRemove = picker.findViewById<View>(R.id.ticket_remove) as ImageButton
        val ticketDetails = picker.findViewById<View>(R.id.ticket_details) as TextView

        assertNotNull(ticketCount)
        assertNotNull(ticketAdd)
        assertNotNull(ticketRemove)
        assertNotNull(ticketDetails)


        assertEquals("1", ticketCount.text)
        assertEquals("$99 Adult (16+ years)", ticketDetails.text)
        assertEquals("Add Adult", ticketAdd.contentDescription.toString())
        assertEquals("Remove Adult", ticketRemove.contentDescription.toString())
    }


    private fun buildTicket(): Ticket {
        val gson = GsonBuilder().create()
        val ticket = gson.fromJson<Ticket>(
                "{\ncode: \"Adult\",\nticketId: \"6319\",\nname: \"Adult\",\nlowerCaseName: \"adult\",\nrestrictionText: \"16+ years\",\nrestriction: {\ntype: \"AGE\",\nmax: 255,\nmin: 16\n},\nprice: \"$99\",\noriginalPrice: \"$135\",\namount: \"99.00\",\noriginalAmount: \"135.00\",\ndisplayName: null,\ndefaultTicketCount: 1\n}", Ticket::class.java)
        ticket.money = Money(99, "USD")

        return ticket
    }

}