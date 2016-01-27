package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.abacus.AbacusResponse
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.lx.Offer
import com.expedia.bookings.widget.LXOffersListWidget
import com.google.gson.GsonBuilder
import org.joda.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import java.util.ArrayList
import kotlin.properties.Delegates

@RunWith(RobolectricRunner::class)
public class LXOfferListWidgetTest {
    private var widget: LXOffersListWidget by Delegates.notNull()
    private var activity: Activity by Delegates.notNull()

    @Before
    public fun before() {
        activity = Robolectric.buildActivity(javaClass<Activity>()).create().get()
        activity.setTheme(R.style.V2_Theme_LX)
        widget = LayoutInflater.from(activity).inflate(R.layout.widget_lx_offer_list, null) as LXOffersListWidget
    }

    @Test
    public fun testSetOffersBucketedForFirstOfferExpandedABTest() {
        bucketFirstOfferInListExpandedABTest(AbacusUtils.DefaultVariate.BUCKETED)
        val offersList = setActivityOfferList()
        assertTrue(offersList.get(0).isToggled)
    }

    @Test
    public fun testSetOffersControlledForFirstOfferExpandedABTest() {
        bucketFirstOfferInListExpandedABTest(AbacusUtils.DefaultVariate.CONTROL)
        val offersList = setActivityOfferList()
        assertFalse(offersList.get(0).isToggled)
    }

    @Test
    public fun testFirstOfferExpandedBucketed() {
        bucketFirstOfferInListExpandedABTest(AbacusUtils.DefaultVariate.BUCKETED)
        setActivityOfferList()

        val offerContainer = widget.getOfferContainer()
        val offerTicketPicker = offerContainer.findViewById(R.id.offer_tickets_picker)
        val offerRow = offerContainer.findViewById(R.id.offer_row)

        assertNotNull(offerContainer)
        assertNotNull(offerTicketPicker)
        assertNotNull(offerRow)
        assertEquals(offerTicketPicker.getVisibility(), View.VISIBLE)
        assertEquals(offerRow.getVisibility(), View.GONE)
    }

    @Test
    public fun testFirstOfferCollapsed() {
        bucketFirstOfferInListExpandedABTest(AbacusUtils.DefaultVariate.CONTROL)
        setActivityOfferList()

        val offerContainer = widget.getOfferContainer()
        val offerTicketPicker = offerContainer.findViewById(R.id.offer_tickets_picker)
        val offerRow = offerContainer.findViewById(R.id.offer_row)

        assertNotNull(offerContainer)
        assertNotNull(offerTicketPicker)
        assertNotNull(offerRow)
        assertEquals(offerTicketPicker.getVisibility(), View.GONE)
        assertEquals(offerRow.getVisibility(), View.VISIBLE)
    }

    private fun bucketFirstOfferInListExpandedABTest(defaultVariate: AbacusUtils.DefaultVariate ) {
        val abacusResponse = AbacusResponse()
        abacusResponse.updateABTestForDebug(AbacusUtils.EBAndroidAppLXFirstActivityListingExpanded,
                defaultVariate.ordinal())
        Db.setAbacusResponse(abacusResponse)
    }

    private fun setActivityOfferList(): List<Offer> {
        val gson = GsonBuilder().create()
        val offerOne = gson.fromJson("{\"id\": \"183619\", \"title\": \"1-Day New York Pass\", \"description\": \"\", \"currencySymbol\": \"$\", \"currencyDisplayedLeft\": true, \"freeCancellation\": true, \"duration\": \"1d\", \"discountPercentage\": null, \"directionality\": \"\", \"availabilityInfo\": [{\"availabilities\": {\"displayDate\": \"Tue, Feb 24\", \"valueDate\": \"2015-02-24 07:30:00\", \"allDayActivity\": false }, \"tickets\": [{\"code\": \"Adult\", \"ticketId\": \"90042\", \"name\": \"Adult\", \"restrictionText\": \"13+ years\", \"price\": \"$130\", \"originalPrice\": \"\", \"amount\": \"130\", \"currencyCode\": \"USD\", \"displayName\": null, \"defaultTicketCount\": 2 }, {\"code\": \"Child\", \"ticketId\": \"90043\", \"name\": \"Child\", \"restrictionText\": \"4-12 years\", \"price\": \"$110\", \"originalPrice\": \"\", \"amount\": \"110\", \"currencyCode\": \"USD\", \"displayName\": null, \"defaultTicketCount\": 0 } ] } ], \"direction\": null }", javaClass<Offer>())
        val offerTwo = gson.fromJson("{\"id\": \"183620\", \"title\": \"2-Day New York Pass\", \"description\": \"\", \"currencySymbol\": \"$\", \"currencyDisplayedLeft\": true, \"freeCancellation\": true, \"duration\": \"2d\", \"discountPercentage\": null, \"directionality\": \"\", \"availabilityInfo\": [{\"availabilities\": {\"displayDate\": \"Tue, Feb 24\", \"valueDate\": \"2015-02-24 09:30:00\", \"allDayActivity\": false }, \"tickets\": [{\"code\": \"Adult\", \"ticketId\": \"90042\", \"name\": \"Adult\", \"restrictionText\": \"13+ years\", \"price\": \"$130\", \"originalPrice\": \"\", \"amount\": \"130\", \"currencyCode\": \"USD\", \"displayName\": null, \"defaultTicketCount\": 2 }, {\"code\": \"Child\", \"ticketId\": \"90043\", \"name\": \"Child\", \"restrictionText\": \"4-12 years\", \"price\": \"$110\", \"originalPrice\": \"\", \"amount\": \"110\", \"currencyCode\": \"USD\", \"displayName\": null, \"defaultTicketCount\": 0 } ] } ], \"direction\": null }", javaClass<Offer>())
        val offersList = ArrayList<Offer>()
        offersList.add(offerOne)
        offersList.add(offerTwo)
        for (offer in offersList) {
            for (availabilityInfo in offer.availabilityInfo) {
                for (ticket in availabilityInfo.tickets) {
                    ticket.money = Money(ticket.amount, "USD")
                }
            }
        }
        widget.setOffers(offersList, LocalDate(2015, 2, 24))
        return offersList
    }
}