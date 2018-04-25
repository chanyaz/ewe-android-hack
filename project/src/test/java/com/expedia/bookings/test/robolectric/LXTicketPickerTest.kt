package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.lx.LXTicketType
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
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
@RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ))
class LXTicketPickerTest {
    private var picker: LXTicketPicker by Delegates.notNull()
    private var activity: Activity by Delegates.notNull()
    var ticketAdd: ImageButton by Delegates.notNull()
    var ticketRemove: ImageButton by Delegates.notNull()

    @Before fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        Ui.getApplication(activity).defaultLXComponents()
        activity.setTheme(R.style.V2_Theme_LX)
        picker = LayoutInflater.from(activity).inflate(R.layout.lx_ticket_picker, null) as LXTicketPicker
        ticketAdd = picker.findViewById<View>(R.id.ticket_add) as ImageButton
        ticketRemove = picker.findViewById<View>(R.id.ticket_remove) as ImageButton
    }

    @Test fun testTicketPicker() {
        picker.bind(buildTicket(), "", 1, false)

        val ticketCount = picker.findViewById<View>(R.id.ticket_count) as TextView
        val ticketDetailsContainer = picker.findViewById<View>(R.id.ticket_details_container) as LinearLayout
        val travelerType = picker.findViewById<View>(R.id.traveler_type) as TextView
        val originalPriceView = picker.findViewById<View>(R.id.original_price) as TextView
        val priceView = picker.findViewById<View>(R.id.actual_price) as TextView

        assertNotNull(ticketCount)
        assertNotNull(ticketAdd)
        assertNotNull(ticketRemove)
        assertNotNull(ticketDetailsContainer)

        assertEquals("1", ticketCount.text)
        assertEquals("Adult (16+ years)", travelerType.text)
        assertEquals("$99", priceView.text)
        assertEquals("$135", originalPriceView.text.toString())
        assertEquals("Add Adult", ticketAdd.contentDescription.toString())
        assertEquals("Remove Adult", ticketRemove.contentDescription.toString())
    }

    @Test fun testTicketPickerButtonDefaultValue() {
        picker.bind(buildTicket(), "", 1, false)

        assertTrue { ticketAdd.isEnabled }
        assertTrue { ticketRemove.isEnabled }
    }

    @Test fun testTicketPickerButtonMinimumValue() {
        picker.bind(buildTicket(), "", 0, false)

        assertTrue { ticketAdd.isEnabled }
        assertFalse { ticketRemove.isEnabled }
    }

    @Test fun testTicketPickerButtonMaximumState() {
        picker.bind(buildTicket(), "", 8, false)

        assertFalse { ticketAdd.isEnabled }
        assertTrue { ticketRemove.isEnabled }
    }

    @Test fun testTicketPickerButtonVolumeBasedState() {
        picker.bind(buildVolumeBasedTicket(), "", 4, false)

        assertFalse { ticketAdd.isEnabled }
        assertTrue { ticketRemove.isEnabled }
    }

    private fun buildTicket(): Ticket {
        val gson = GsonBuilder().create()
        val ticket = gson.fromJson<Ticket>(
                "{\ncode: \"Adult\",\nticketId: \"6319\",\nname: \"Adult\",\nlowerCaseName: \"adult\",\nrestrictionText: \"16+ years\",\nrestriction: {\ntype: \"AGE\",\nmax: 255,\nmin: 16\n},\nprice: \"$99\",\noriginalPrice: \"$135\",\namount: \"99.00\",\noriginalAmount: \"135.00\",\ndisplayName: null,\ndefaultTicketCount: 1\n}", Ticket::class.java)
        ticket.money = Money(99, "USD")
        ticket.originalPriceMoney = Money(135, "USD")

        return ticket
    }

    private fun buildVolumeBasedTicket(): Ticket {
        val ticket = Ticket()
        ticket.code = LXTicketType.Traveler
        ticket.ticketId = "568611"
        ticket.restrictionText = "4+ years"
        ticket.amount = "12.4875"
        ticket.originalAmount = ""
        ticket.count = 4
        ticket.money = Money("12.4875", "USD")
        ticket.originalPriceMoney = Money("0", "USD")

        val priceObject = Ticket.LxTicketPrices()
        priceObject.originalPrice = null
        priceObject.travellerNum = 4
        priceObject.amount = "12.4875"
        priceObject.price = "$12.49"
        priceObject.money = Money("12.4875", "USD")
        priceObject.originalPriceMoney = Money("0", "USD")

        ticket.prices = ArrayList<Ticket.LxTicketPrices>()
        ticket.prices.add(priceObject)

        return ticket
    }
}
