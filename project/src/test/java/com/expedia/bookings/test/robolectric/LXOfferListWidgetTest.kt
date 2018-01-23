package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.abacus.AbacusResponse
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.lx.LXTicketType
import com.expedia.bookings.data.lx.Offer
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.widget.LXOffersListWidget
import com.google.gson.GsonBuilder
import org.joda.time.LocalDate
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import java.util.ArrayList
import kotlin.properties.Delegates

@RunWith(RobolectricRunner::class)
@RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ))
class LXOfferListWidgetTest {
    private var widget: LXOffersListWidget by Delegates.notNull()
    private var activity: Activity by Delegates.notNull()

    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_LX)
        widget = LayoutInflater.from(activity).inflate(R.layout.widget_lx_offer_list, null) as LXOffersListWidget
    }

    @Test
    fun testSetOffersBucketedForFirstOfferExpandedABTest() {
        bucketFirstOfferInListExpandedABTest(AbacusUtils.DefaultVariant.BUCKETED)
        val offersList = setActivityOfferList()
        widget.sortTicketByPriorityAndOfferByPrice(offersList)
        assertTrue(offersList[0].isToggled)
    }

    @Test
    fun testSetOffersControlledForFirstOfferExpandedABTest() {
        bucketFirstOfferInListExpandedABTest(AbacusUtils.DefaultVariant.CONTROL)
        val offersList = setActivityOfferList()
        assertFalse(offersList[0].isToggled)
    }

    @Test
    fun testFirstOfferExpandedBucketed() {
        bucketFirstOfferInListExpandedABTest(AbacusUtils.DefaultVariant.BUCKETED)
        setActivityOfferList()

        val offerContainer = widget.offerContainer
        val offerTicketPicker = offerContainer.findViewById<View>(R.id.offer_tickets_picker)
        val offerRow = offerContainer.findViewById<View>(R.id.offer_row)

        assertNotNull(offerContainer)
        assertNotNull(offerTicketPicker)
        assertNotNull(offerRow)
        assertEquals(offerTicketPicker.visibility, View.VISIBLE)
        assertEquals(offerRow.visibility, View.GONE)
    }

    @Test
    fun testFirstOfferCollapsed() {
        bucketFirstOfferInListExpandedABTest(AbacusUtils.DefaultVariant.CONTROL)
        setActivityOfferList()

        val offerContainer = widget.offerContainer
        val offerTicketPicker = offerContainer.findViewById<View>(R.id.offer_tickets_picker)
        val offerRow = offerContainer.findViewById<View>(R.id.offer_row)

        assertNotNull(offerContainer)
        assertNotNull(offerTicketPicker)
        assertNotNull(offerRow)
        assertEquals(offerTicketPicker.visibility, View.GONE)
        assertEquals(offerRow.visibility, View.VISIBLE)
    }

    private fun bucketFirstOfferInListExpandedABTest(defaultVariate: AbacusUtils.DefaultVariant) {
        val abacusResponse = AbacusResponse()
        abacusResponse.updateABTestForDebug(AbacusUtils.EBAndroidAppLXFirstActivityListingExpanded.key,
                defaultVariate.ordinal)
        Db.sharedInstance.setAbacusResponse(abacusResponse)
    }

    private fun setActivityOfferList(): List<Offer> {
        val gson = GsonBuilder().create()
        val offerOne = gson.fromJson("{\"id\": \"183619\", \"title\": \"1-Day New York Pass\", \"description\": \"\", \"currencySymbol\": \"$\", \"currencyDisplayedLeft\": true, \"freeCancellation\": true, \"duration\": \"1d\", \"discountPercentage\": null, \"directionality\": \"\", \"availabilityInfo\": [{\"availabilities\": {\"displayDate\": \"Tue, Feb 24\", \"valueDate\": \"2015-02-24 07:30:00\", \"allDayActivity\": false }, \"tickets\": [{\"code\": \"Adult\", \"ticketId\": \"90042\", \"name\": \"Adult\", \"restrictionText\": \"13+ years\", \"price\": \"$130\", \"originalPrice\": \"\", \"amount\": \"130\", \"currencyCode\": \"USD\", \"displayName\": null, \"defaultTicketCount\": 2 }, {\"code\": \"Child\", \"ticketId\": \"90043\", \"name\": \"Child\", \"restrictionText\": \"4-12 years\", \"price\": \"$105\", \"originalPrice\": \"\", \"amount\": \"105\", \"currencyCode\": \"USD\", \"displayName\": null, \"defaultTicketCount\": 0 } ] } ], \"direction\": null }", Offer::class.java)
        val offerTwo = gson.fromJson("{\"id\": \"183620\", \"title\": \"2-Day New York Pass\", \"description\": \"\", \"currencySymbol\": \"$\", \"currencyDisplayedLeft\": true, \"freeCancellation\": true, \"duration\": \"2d\", \"discountPercentage\": null, \"directionality\": \"\", \"availabilityInfo\": [{\"availabilities\": {\"displayDate\": \"Tue, Feb 24\", \"valueDate\": \"2015-02-24 09:30:00\", \"allDayActivity\": false }, \"tickets\": [{\"code\": \"Adult\", \"ticketId\": \"90042\", \"name\": \"Adult\", \"restrictionText\": \"13+ years\", \"price\": \"$110\", \"originalPrice\": \"\", \"amount\": \"110\", \"currencyCode\": \"USD\", \"displayName\": null, \"defaultTicketCount\": 2 }, {\"code\": \"Child\", \"ticketId\": \"90043\", \"name\": \"Child\", \"restrictionText\": \"4-12 years\", \"price\": \"$60\", \"originalPrice\": \"\", \"amount\": \"60\", \"currencyCode\": \"USD\", \"displayName\": null, \"defaultTicketCount\": 0 } ] } ], \"direction\": null }", Offer::class.java)
        val offerThree = gson.fromJson("{\"id\": \"183620\", \"title\": \"3-Day New York Pass\", \"description\": \"\", \"currencySymbol\": \"$\", \"currencyDisplayedLeft\": true, \"freeCancellation\": true, \"duration\": \"2d\", \"discountPercentage\": null, \"directionality\": \"\", \"availabilityInfo\": [{\"availabilities\": {\"displayDate\": \"Tue, Feb 24\", \"valueDate\": \"2015-02-24 09:30:00\", \"allDayActivity\": false }, \"tickets\": [{\"code\": \"Adult\", \"ticketId\": \"90042\", \"name\": \"Adult\", \"restrictionText\": \"13+ years\", \"price\": \"$110\", \"originalPrice\": \"\", \"amount\": \"110\", \"currencyCode\": \"USD\", \"displayName\": null, \"defaultTicketCount\": 2 }, {\"code\": \"Child\", \"ticketId\": \"90043\", \"name\": \"Child\", \"restrictionText\": \"4-12 years\", \"price\": \"$90\", \"originalPrice\": \"\", \"amount\": \"90\", \"currencyCode\": \"USD\", \"displayName\": null, \"defaultTicketCount\": 0 } ] } ], \"direction\": null }", Offer::class.java)

        val offersList = ArrayList<Offer>()
        offersList.add(offerOne)
        offersList.add(offerTwo)
        offersList.add(offerThree)

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

    @Test
    fun testOffersSortedByPrice() {
        val offerList = setActivityOfferList()
        val availableOffers = ArrayList<Offer>()

        for (offer in offerList) {
            if (offer.updateAvailabilityInfoOfSelectedDate(org.joda.time.LocalDate("2015-02-24")) != null) {
                availableOffers.add(offer)
            }
        }

        val expectedOffers = widget.sortTicketByPriorityAndOfferByPrice(availableOffers) as ArrayList<Offer>
        assertEquals(3, expectedOffers.size)
        assertEquals(LXTicketType.Adult, expectedOffers[0].availabilityInfoOfSelectedDate.tickets[0].code)

        assertEquals("2-Day New York Pass", expectedOffers[0].title)
        assertEquals("3-Day New York Pass", expectedOffers[1].title)
        assertEquals("1-Day New York Pass", expectedOffers[2].title)
    }
}
