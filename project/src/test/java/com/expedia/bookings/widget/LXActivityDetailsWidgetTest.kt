package com.expedia.bookings.widget

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.lx.ActivityDetailsResponse
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.google.gson.GsonBuilder
import org.joda.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import java.io.InputStreamReader
import kotlin.properties.Delegates
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class LXActivityDetailsWidgetTest {
    private var details: LXActivityDetailsWidget by Delegates.notNull()
    private var activity: Activity by Delegates.notNull()

    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        Ui.getApplication(activity).defaultLXComponents()
        activity.setTheme(R.style.V2_Theme_LX)
        details = LayoutInflater.from(activity).inflate(R.layout.widget_activity_details, null) as LXActivityDetailsWidget

    }

    @Test
    fun testActivityDetailsViews() {
        val activityGallery = details.findViewById(R.id.activity_gallery)
        val description = details.findViewById(R.id.description)
        val location = details.findViewById(R.id.location)
        val highlights = details.findViewById(R.id.highlights)
        val offers = details.findViewById(R.id.offers)
        val offerDatesContainer = details.findViewById(R.id.offer_dates_container)
        val inclusions = details.findViewById(R.id.inclusions)
        val exclusions = details.findViewById(R.id.exclusions)
        val knowBeforeYouBook = details.findViewById(R.id.know_before_you_book)
        val cancellation = details.findViewById(R.id.cancellation)
        val eventLocation = details.findViewById(R.id.event_location)
        val redemptionLocation = details.findViewById(R.id.redemption_location)
        val infositeMap = details.findViewById(R.id.map_click_container)

        assertNotNull(activityGallery)
        assertNotNull(description)
        assertNotNull(location)
        assertNotNull(highlights)
        assertNotNull(offers)
        assertNotNull(offerDatesContainer)
        assertNotNull(inclusions)
        assertNotNull(exclusions)
        assertNotNull(knowBeforeYouBook)
        assertNotNull(cancellation)
        assertNotNull(eventLocation)
        assertNotNull(redemptionLocation)
        assertNotNull(infositeMap)
    }

    @Test
    fun testDatesContainer() {
        details.activityDetails = getOfferDetails()
        val now = LocalDate(2016, 7, 17)

        details.buildOfferDatesSelector(getOfferDetails().offersDetail, now)
        val container = details.findViewById(R.id.offer_dates_container) as LinearLayout

        val count = container.childCount
        val range = activity.baseContext.resources.getInteger(R.integer.lx_default_search_range) + 1
        val dateOne = container.getChildAt(0) as LXOfferDatesButton
        val dateTwo = container.getChildAt(1) as LXOfferDatesButton

        assertNotNull(container)
        assertEquals(range, count)
        assertTrue(dateOne.isChecked)
        assertFalse(dateTwo.isChecked)
    }

    @Test
    fun testDatesChanges() {
        details.activityDetails = getOfferDetails()
        val now = LocalDate(2016, 7, 17)

        details.buildOfferDatesSelector(getOfferDetails().offersDetail, now)
        val container = details.findViewById(R.id.offer_dates_container) as LinearLayout

        val count = container.childCount
        val range = activity.baseContext.resources.getInteger(R.integer.lx_default_search_range) + 1
        val dateOne = container.getChildAt(0) as LXOfferDatesButton
        val dateTwo = container.getChildAt(1) as LXOfferDatesButton
        val dateThree = container.getChildAt(3) as LXOfferDatesButton

        assertNotNull(container)
        assertEquals(range, count)
        assertTrue(dateOne.isChecked)
        assertFalse(dateTwo.isChecked)

        assertEquals("Sun\n17\nJul", dateOne.text.toString())

        val selectedDate = LocalDate(2016, 7, 20)

        details.onDetailsDateChanged(selectedDate, dateThree)
        assertEquals("Wed\n20\nJul", dateThree.text.toString())

        assertTrue(dateThree.isChecked)
    }

    @Test
    fun testActivityOffer() {
        val now = LocalDate(2016, 7, 17)
        details.activityDetails = getOfferDetails()
        details.buildOffersSection(now)

        val container = details.findViewById(R.id.offers_container) as LinearLayout

        val offerOne = container.getChildAt(0)
        val offerTwo = container.getChildAt(1)

        val count = container.childCount

        val offerTitleOne = offerOne.findViewById(R.id.offer_title) as TextView
        val offerPriceSummaryOne = offerOne.findViewById(R.id.price_summary) as TextView

        val offerTitleTwo = offerTwo.findViewById(R.id.offer_title) as TextView
        val offerPriceSummaryTwo = offerTwo.findViewById(R.id.price_summary) as TextView
        val offerSelectTicketTwo = offerTwo.findViewById(R.id.select_tickets) as Button
        val offerRowTwo = offerTwo.findViewById(R.id.offer_row) as LinearLayout

        assertNotNull(container)
        assertEquals(3, count)

        // First Offer
        assertEquals("1-Day Ticket", offerTitleOne.text.toString())
        assertEquals("$45 Adult, $25 Child", offerPriceSummaryOne.text.toString())

        // Second Offer
        assertEquals("2-Day Ticket", offerTitleTwo.text.toString())
        assertEquals("$55 Adult, $35 Child", offerPriceSummaryTwo.text.toString())
        assertEquals(offerRowTwo.visibility, View.VISIBLE)
        offerSelectTicketTwo.performClick()

        assertEquals(offerRowTwo.visibility, View.GONE)
    }

    @Test
    fun testOffersExpandCollapse() {
        val now = LocalDate(2016, 7, 17)
        details.activityDetails = getOfferDetails()
        details.buildOffersSection(now)

        val container = details.findViewById(R.id.offers_container) as LinearLayout

        val offerOne = container.getChildAt(0)
        val offerTwo = container.getChildAt(1)


        val offerOneSelectTicket= offerOne.findViewById(R.id.select_tickets) as Button
        val offerOneRow = offerOne.findViewById(R.id.offer_row) as LinearLayout
        val offerOneTicketsPicker= offerOne.findViewById(R.id.offer_tickets_picker) as LinearLayout

        val offerSelectTicketTwo = offerTwo.findViewById(R.id.select_tickets) as Button
        val offerRowTwo = offerTwo.findViewById(R.id.offer_row) as LinearLayout
        val offerTwoTicketsPicker= offerTwo.findViewById(R.id.offer_tickets_picker) as LinearLayout


        assertEquals(offerOneRow.visibility, View.VISIBLE)
        assertEquals(offerRowTwo.visibility, View.VISIBLE)
        assertEquals(offerOneTicketsPicker.visibility, View.GONE)
        assertEquals(offerTwoTicketsPicker.visibility, View.GONE)

        // Expand Offer Two
        offerSelectTicketTwo.performClick()

        assertEquals(offerOneTicketsPicker.visibility, View.GONE)
        assertEquals(offerTwoTicketsPicker.visibility, View.VISIBLE)
        assertEquals(offerRowTwo.visibility, View.GONE)
        assertEquals(offerOneRow.visibility, View.VISIBLE)

        // Expand Offer One
        offerOneSelectTicket.performClick()
        assertEquals(offerOneTicketsPicker.visibility, View.VISIBLE)
        assertEquals(offerTwoTicketsPicker.visibility, View.GONE)
        assertEquals(offerRowTwo.visibility, View.VISIBLE)
        assertEquals(offerOneRow.visibility, View.GONE)
    }

    private fun getOfferDetails(): ActivityDetailsResponse {

        val gson = GsonBuilder().create()
        val activityDetailsResponse = gson.fromJson(InputStreamReader(details.context.assets.open("MockData/lx_details_response.json")), ActivityDetailsResponse::class.java)


        for (offer in activityDetailsResponse.offersDetail.offers) {
            for (availabilityInfo in offer.availabilityInfo) {
                for (ticket in availabilityInfo.tickets) {
                    ticket.money = Money(ticket.amount, "USD")
                }
            }
        }

        return activityDetailsResponse
    }
}