package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.abacus.AbacusResponse
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.lx.ActivityDetailsResponse
import com.expedia.bookings.data.lx.LXActivity
import com.expedia.bookings.data.lx.RecommendedActivitiesResponse
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.LXActivityDetailsWidget
import com.expedia.bookings.widget.LXOfferDatesButton
import com.expedia.bookings.widget.TextView
import com.google.gson.GsonBuilder
import org.joda.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import java.io.InputStreamReader
import java.util.ArrayList
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

    private fun recommendationABTest(defaultVariate: AbacusUtils.DefaultVariate) {
        val abacusResponse = AbacusResponse()
        abacusResponse.updateABTestForDebug(AbacusUtils.EBAndroidAppLXRecommendedActivitiesTest,
                defaultVariate.ordinal)
        Db.setAbacusResponse(abacusResponse)
    }

    @Test
    fun testRecommendations() {
        recommendationABTest(AbacusUtils.DefaultVariate.BUCKETED)
        val recommendationSubscriber = details.recommendedObserver
        recommendationSubscriber.onNext(buildRecommendedActivityResponse());

        val recommendations = details.recommendations

        val moreLikeThis = details.moreLikeThis
        val firstRecommendedActivityView = details.moreLikeThis.getChildAt(0)
        val activityTitle = firstRecommendedActivityView.findViewById(R.id.activity_title) as TextView
        val activityDuration = firstRecommendedActivityView.findViewById(R.id.activity_duration) as android.widget.TextView

        assertEquals(recommendations.visibility, View.VISIBLE)
        assertEquals(2, moreLikeThis.childCount)
        assertNotNull(firstRecommendedActivityView)
        assertEquals("New York Pass: Visit up to 80 Attractions, Museums & Tours", activityTitle.text)
        assertEquals("2d+", activityDuration.text.toString())
    }

    private fun buildRecommendedActivityResponse(): RecommendedActivitiesResponse {
        val gson = GsonBuilder().create()
        val lxActivityOne = gson.fromJson<LXActivity>(
                "{\"id\": \"183615\", \"title\": \"New York Pass: Visit up to 80 Attractions, Museums & Tours\", \"description\": \"<p>Whether you know exactly where you want to visit or you're improvising, The New York Pass offers something just right for you.</p>\", \"images\": [{\"url\": \"//a.travel-assets.com/mediavault.le/media/c932f66857388ec282910f62d64354eff6760223.jpeg\", \"caption\": \"notitle\", \"creditString\": \"nocredit\"}, {\"url\": \"//a.travel-assets.com/mediavault.le/media/4da13469be6dffa5ff880b0a3cc59cb58e6690bc.jpeg\", \"caption\": \"notitle\", \"creditString\": \"nocredit\"}, {\"url\": \"//a.travel-assets.com/mediavault.le/media/efe62c374095946276d943831420ccad01e47396.jpeg\", \"caption\": \"notitle\", \"creditString\": \"nocredit\"}, {\"url\": \"//a.travel-assets.com/mediavault.le/media/cc66fbb8e40c74c670f32124aac534e29128e01b.jpeg\", \"caption\": \"notitle\", \"creditString\": \"nocredit\"}, {\"url\": \"//a.travel-assets.com/mediavault.le/media/909d80624ff13a43a950e0149adf49bf23195774.jpeg\", \"caption\": \"notitle\", \"creditString\": \"nocredit\"} ], \"highlights\": [\"<p>Admission to more than 80 top attractions, museums &amp; tours</p>\", \"<p>VIP Fast-Track entry at several top attractions</p>\", \"<p>200-page guidebook with area maps &amp; attraction details</p>\", \"<p>Guidebook offered in multiple languages</p>\", \"<p>7-Day Pass for the 5-Day Pass price when you buy now</p>\"], \"fromPrice\": \"$130\", \"fromOriginalPrice\": \"\", \"fromPriceTicketType\": \"Adult\", \"startDate\": \"2015-02-24\", \"endDate\": \"2015-02-24\", \"maximumBookingLength\": null, \"lastValidDate\": null, \"firstValidDate\": null, \"duration\": \"2d\", \"inclusions\": [\"<p>Admission to more than 80 top attractions, museums, &amp; tours</p>\", \"<p>200-page guidebook offered in English, Italian, Spanish, French, German, and Brazilian Portuguese</p>\", \"<p>Skip-the-Line access at many attractions</p>\", \"<p>Special discounts at restaurants &amp; retailers</p>\"], \"isMultiDuration\": \"true\", \"exclusions\": [\"<p>Transportation to and from attractions</p>\"], \"freeCancellationMinHours\": 72, \"knowBeforeYouBook\": [\"<p>Children 3 and younger are complimentary at most attractions.</p>\", \"<p>Pass is valid for the number of consecutive calendar days you purchase, beginning on the first day of use.</p>\", \"<p>Advance reservations may be made for tours included on the pass; mention you are a New York Pass holder.</p>\", \"<p>Hours and dates of operation for individual attractions vary.</p>\", \"<p>Get the 7-Day Pass for the 5-Day Price when you buy now, savings reflected in the price above.</p>\"], \"freeCancellation\": true, \"discountPercentage\": 0, \"address\": \"Gray Line New York Visitors Center, 777 8th Avenue\\r\\nbetween 47th and 48th Street, New York, NY 10036, United States\", \"location\": \"New York, United States\", \"regionId\": \"178293\", \"destination\": \"New York\", \"fullName\": \"New York (and vicinity), New York, United States of America\", \"omnitureJson\": \"{\\\"accountName\\\":\\\"expedia1\\\",\\\"omnitureProperties\\\":{\\\"server\\\":\\\"www.expedia.com\\\",\\\"authChannel\\\":\\\"SIGNIN_FORM\\\",\\\"eVar6\\\":\\\"0\\\",\\\"eVar5\\\":\\\"4\\\",\\\"eVar4\\\":\\\"D\\\\u003dc4\\\",\\\"eVar2\\\":\\\"D\\\\u003dc2\\\",\\\"channel\\\":\\\"local expert\\\",\\\"pageName\\\":\\\"page.LX.Infosite.Information\\\",\\\"products\\\":\\\"LX;Merchant LX:183615\\\",\\\"activityId\\\":\\\"4f04feaa-fdf0-4f40-8de1-6ffa262e28f3\\\",\\\"prop30\\\":\\\"1033\\\",\\\"prop11\\\":\\\"null\\\",\\\"prop13\\\":\\\"0\\\",\\\"prop12\\\":\\\"eba1f647-61a7-406f-8bda-6f0c7124e789\\\",\\\"prop34\\\":\\\"6880.0|6611.0|6727.0|5242.0|6524.1|6620.0|5150.1|6271.1\\\",\\\"events\\\":\\\"event3\\\",\\\"charSet\\\":\\\"UTF-8\\\",\\\"eVar34\\\":\\\"D\\\\u003dc34\\\",\\\"eVar56\\\":\\\"RewardsStatus()\\\",\\\"eVar55\\\":\\\"unknown\\\",\\\"eVar54\\\":\\\"1033\\\",\\\"eVar18\\\":\\\"D\\\\u003dpageName\\\",\\\"eVar17\\\":\\\"D\\\\u003dpageName\\\",\\\"prop6\\\":\\\"2015-02-24\\\",\\\"prop5\\\":\\\"2015-02-24\\\",\\\"prop4\\\":\\\"178293\\\",\\\"list1\\\":\\\"6880.0|6611.0|6727.0|5242.0|6524.1|6620.0|5150.1|6271.1\\\",\\\"prop2\\\":\\\"local expert\\\",\\\"prop1\\\":\\\"4\\\",\\\"userType\\\":\\\"ANONYMOUS\\\"}}\", \"offersDetail\": {\"offers\": [{\"id\": \"183619\", \"title\": \"2-Day New York Pass\", \"description\": \"\", \"currencySymbol\": \"$\", \"currencyDisplayedLeft\": true, \"freeCancellation\": true, \"duration\": \"2d\", \"discountPercentage\": null, \"directionality\": \"\", \"availabilityInfo\": [{\"availabilities\": {\"displayDate\": \"Tue, Feb 24\", \"valueDate\": \"2015-02-24 07:30:00\", \"allDayActivity\": false }, \"tickets\": [{\"code\": \"Adult\", \"ticketId\": \"90042\", \"name\": \"Adult\", \"restrictionText\": \"13+ years\", \"price\": \"$130\", \"originalPrice\": \"123\", \"amount\": \"130\", \"displayName\": null, \"defaultTicketCount\": 2 }, {\"code\": \"Child\", \"ticketId\": \"90043\", \"name\": \"Child\", \"restrictionText\": \"4-12 years\", \"price\": \"$110\", \"originalPrice\": \"\", \"amount\": \"110\", \"displayName\": null, \"defaultTicketCount\": 0 } ] } ], \"direction\": null }, {\"id\": \"183621\", \"title\": \"3-Day New York Pass\", \"description\": \"\", \"currencySymbol\": \"$\", \"currencyDisplayedLeft\": true, \"freeCancellation\": true, \"duration\": \"3d\", \"discountPercentage\": null, \"directionality\": \"\", \"availabilityInfo\": [{\"availabilities\": {\"displayDate\": \"Tue, Feb 24\", \"valueDate\": \"2015-02-24 07:30:00\", \"allDayActivity\": false }, \"tickets\": [{\"code\": \"Adult\", \"ticketId\": \"90046\", \"name\": \"Adult\", \"restrictionText\": \"13+ years\", \"price\": \"$180\", \"originalPrice\": \"\", \"amount\": \"180\", \"displayName\": null, \"defaultTicketCount\": 2 }, {\"code\": \"Child\", \"ticketId\": \"90047\", \"name\": \"Child\", \"restrictionText\": \"4-12 years\", \"price\": \"$140\", \"originalPrice\": \"\", \"amount\": \"140\", \"displayName\": null, \"defaultTicketCount\": 0 } ] } ], \"direction\": null }, {\"id\": \"183623\", \"title\": \"5-Day New York Pass\", \"description\": \"\", \"currencySymbol\": \"$\", \"currencyDisplayedLeft\": true, \"freeCancellation\": true, \"duration\": \"5d\", \"discountPercentage\": null, \"directionality\": \"\", \"availabilityInfo\": [{\"availabilities\": {\"displayDate\": \"Tue, Feb 24\", \"valueDate\": \"2015-02-24 07:30:00\", \"allDayActivity\": false }, \"tickets\": [{\"code\": \"Adult\", \"ticketId\": \"90054\", \"name\": \"Adult\", \"restrictionText\": \"13+ years\", \"price\": \"$210\", \"originalPrice\": \"\", \"amount\": \"210\", \"displayName\": null, \"defaultTicketCount\": 2 }, {\"code\": \"Child\", \"ticketId\": \"90055\", \"name\": \"Child\", \"restrictionText\": \"4-12 years\", \"price\": \"$155\", \"originalPrice\": \"\", \"amount\": \"155\", \"displayName\": null, \"defaultTicketCount\": 0 } ] } ], \"direction\": null }, {\"id\": \"183625\", \"title\": \"7-Day New York Pass\", \"description\": \"\", \"currencySymbol\": \"$\", \"currencyDisplayedLeft\": true, \"freeCancellation\": true, \"duration\": \"7d\", \"discountPercentage\": \"8\", \"directionality\": \"\", \"availabilityInfo\": [{\"availabilities\": {\"displayDate\": \"Tue, Feb 24\", \"valueDate\": \"2015-02-24 07:30:00\", \"allDayActivity\": false }, \"tickets\": [{\"code\": \"Adult\", \"ticketId\": \"90062\", \"name\": \"Adult\", \"restrictionText\": \"13+ years\", \"price\": \"$210\", \"originalPrice\": \"$230\", \"amount\": \"210\", \"displayName\": null, \"defaultTicketCount\": 2 }, {\"code\": \"Child\", \"ticketId\": \"90063\", \"name\": \"Child\", \"restrictionText\": \"4-12 years\", \"price\": \"$155\", \"originalPrice\": \"$165\", \"amount\": \"155\", \"displayName\": null, \"defaultTicketCount\": 0 } ] } ], \"direction\": null } ], \"priceFootnote\": \"*Taxes included\", \"sameDateSearch\": true }, \"dateAdjusted\": false, \"typeGT\": false, \"passengers\": null, \"bags\": null, \"metaDescription\": \"Whether you know exactly where you want to visit or you're improvising, The New York Pass offers something just right for you.\", \"metaKeywords\": \"New York Pass: Visit up to 80 Attractions, Museums & Tours, New York, United States, New York Attractions, New York Cruises & Water Tours, New York Sightseeing Passes, New York Tours & Sightseeing, New York Activities, New York Things To Do , Book New York Activities , Book New York Things To Do , Activities, Things To Do\", \"pageTitle\": \"New York Pass: Visit up to 80 Attractions, Museums &amp; Tours\", \"category\": \"Attractions\"}", LXActivity::class.java)
        val lxActivityTwo = gson.fromJson<LXActivity>(
                "{\"id\": \"183614\", \"title\": \"New York Pass: Visit up to 100 Attractions, Museums & Tours\", \"description\": \"<p>Whether you know exactly where you want to visit or you're improvising, The New York Pass offers something just right for you.</p>\", \"images\": [{\"url\": \"//a.travel-assets.com/mediavault.le/media/c932f66857388ec282910f62d64354eff6760223.jpeg\", \"caption\": \"notitle\", \"creditString\": \"nocredit\"}, {\"url\": \"//a.travel-assets.com/mediavault.le/media/4da13469be6dffa5ff880b0a3cc59cb58e6690bc.jpeg\", \"caption\": \"notitle\", \"creditString\": \"nocredit\"}, {\"url\": \"//a.travel-assets.com/mediavault.le/media/efe62c374095946276d943831420ccad01e47396.jpeg\", \"caption\": \"notitle\", \"creditString\": \"nocredit\"}, {\"url\": \"//a.travel-assets.com/mediavault.le/media/cc66fbb8e40c74c670f32124aac534e29128e01b.jpeg\", \"caption\": \"notitle\", \"creditString\": \"nocredit\"}, {\"url\": \"//a.travel-assets.com/mediavault.le/media/909d80624ff13a43a950e0149adf49bf23195774.jpeg\", \"caption\": \"notitle\", \"creditString\": \"nocredit\"} ], \"highlights\": [\"<p>Admission to more than 80 top attractions, museums &amp; tours</p>\", \"<p>VIP Fast-Track entry at several top attractions</p>\", \"<p>200-page guidebook with area maps &amp; attraction details</p>\", \"<p>Guidebook offered in multiple languages</p>\", \"<p>7-Day Pass for the 5-Day Pass price when you buy now</p>\"], \"fromPrice\": \"$130\", \"fromOriginalPrice\": \"\", \"fromPriceTicketType\": \"Adult\", \"startDate\": \"2015-02-24\", \"endDate\": \"2015-02-24\", \"maximumBookingLength\": null, \"lastValidDate\": null, \"firstValidDate\": null, \"duration\": \"2d\", \"inclusions\": [\"<p>Admission to more than 80 top attractions, museums, &amp; tours</p>\", \"<p>200-page guidebook offered in English, Italian, Spanish, French, German, and Brazilian Portuguese</p>\", \"<p>Skip-the-Line access at many attractions</p>\", \"<p>Special discounts at restaurants &amp; retailers</p>\"], \"isMultiDuration\": \"true\", \"exclusions\": [\"<p>Transportation to and from attractions</p>\"], \"freeCancellationMinHours\": 72, \"knowBeforeYouBook\": [\"<p>Children 3 and younger are complimentary at most attractions.</p>\", \"<p>Pass is valid for the number of consecutive calendar days you purchase, beginning on the first day of use.</p>\", \"<p>Advance reservations may be made for tours included on the pass; mention you are a New York Pass holder.</p>\", \"<p>Hours and dates of operation for individual attractions vary.</p>\", \"<p>Get the 7-Day Pass for the 5-Day Price when you buy now, savings reflected in the price above.</p>\"], \"freeCancellation\": true, \"discountPercentage\": 0, \"address\": \"Gray Line New York Visitors Center, 777 8th Avenue\\r\\nbetween 47th and 48th Street, New York, NY 10036, United States\", \"location\": \"New York, United States\", \"regionId\": \"178293\", \"destination\": \"New York\", \"fullName\": \"New York (and vicinity), New York, United States of America\", \"omnitureJson\": \"{\\\"accountName\\\":\\\"expedia1\\\",\\\"omnitureProperties\\\":{\\\"server\\\":\\\"www.expedia.com\\\",\\\"authChannel\\\":\\\"SIGNIN_FORM\\\",\\\"eVar6\\\":\\\"0\\\",\\\"eVar5\\\":\\\"4\\\",\\\"eVar4\\\":\\\"D\\\\u003dc4\\\",\\\"eVar2\\\":\\\"D\\\\u003dc2\\\",\\\"channel\\\":\\\"local expert\\\",\\\"pageName\\\":\\\"page.LX.Infosite.Information\\\",\\\"products\\\":\\\"LX;Merchant LX:183615\\\",\\\"activityId\\\":\\\"4f04feaa-fdf0-4f40-8de1-6ffa262e28f3\\\",\\\"prop30\\\":\\\"1033\\\",\\\"prop11\\\":\\\"null\\\",\\\"prop13\\\":\\\"0\\\",\\\"prop12\\\":\\\"eba1f647-61a7-406f-8bda-6f0c7124e789\\\",\\\"prop34\\\":\\\"6880.0|6611.0|6727.0|5242.0|6524.1|6620.0|5150.1|6271.1\\\",\\\"events\\\":\\\"event3\\\",\\\"charSet\\\":\\\"UTF-8\\\",\\\"eVar34\\\":\\\"D\\\\u003dc34\\\",\\\"eVar56\\\":\\\"RewardsStatus()\\\",\\\"eVar55\\\":\\\"unknown\\\",\\\"eVar54\\\":\\\"1033\\\",\\\"eVar18\\\":\\\"D\\\\u003dpageName\\\",\\\"eVar17\\\":\\\"D\\\\u003dpageName\\\",\\\"prop6\\\":\\\"2015-02-24\\\",\\\"prop5\\\":\\\"2015-02-24\\\",\\\"prop4\\\":\\\"178293\\\",\\\"list1\\\":\\\"6880.0|6611.0|6727.0|5242.0|6524.1|6620.0|5150.1|6271.1\\\",\\\"prop2\\\":\\\"local expert\\\",\\\"prop1\\\":\\\"4\\\",\\\"userType\\\":\\\"ANONYMOUS\\\"}}\", \"offersDetail\": {\"offers\": [{\"id\": \"183619\", \"title\": \"2-Day New York Pass\", \"description\": \"\", \"currencySymbol\": \"$\", \"currencyDisplayedLeft\": true, \"freeCancellation\": true, \"duration\": \"2d\", \"discountPercentage\": null, \"directionality\": \"\", \"availabilityInfo\": [{\"availabilities\": {\"displayDate\": \"Tue, Feb 24\", \"valueDate\": \"2015-02-24 07:30:00\", \"allDayActivity\": false }, \"tickets\": [{\"code\": \"Adult\", \"ticketId\": \"90042\", \"name\": \"Adult\", \"restrictionText\": \"13+ years\", \"price\": \"$130\", \"originalPrice\": \"123\", \"amount\": \"130\", \"displayName\": null, \"defaultTicketCount\": 2 }, {\"code\": \"Child\", \"ticketId\": \"90043\", \"name\": \"Child\", \"restrictionText\": \"4-12 years\", \"price\": \"$110\", \"originalPrice\": \"\", \"amount\": \"110\", \"displayName\": null, \"defaultTicketCount\": 0 } ] } ], \"direction\": null }, {\"id\": \"183621\", \"title\": \"3-Day New York Pass\", \"description\": \"\", \"currencySymbol\": \"$\", \"currencyDisplayedLeft\": true, \"freeCancellation\": true, \"duration\": \"3d\", \"discountPercentage\": null, \"directionality\": \"\", \"availabilityInfo\": [{\"availabilities\": {\"displayDate\": \"Tue, Feb 24\", \"valueDate\": \"2015-02-24 07:30:00\", \"allDayActivity\": false }, \"tickets\": [{\"code\": \"Adult\", \"ticketId\": \"90046\", \"name\": \"Adult\", \"restrictionText\": \"13+ years\", \"price\": \"$180\", \"originalPrice\": \"\", \"amount\": \"180\", \"displayName\": null, \"defaultTicketCount\": 2 }, {\"code\": \"Child\", \"ticketId\": \"90047\", \"name\": \"Child\", \"restrictionText\": \"4-12 years\", \"price\": \"$140\", \"originalPrice\": \"\", \"amount\": \"140\", \"displayName\": null, \"defaultTicketCount\": 0 } ] } ], \"direction\": null }, {\"id\": \"183623\", \"title\": \"5-Day New York Pass\", \"description\": \"\", \"currencySymbol\": \"$\", \"currencyDisplayedLeft\": true, \"freeCancellation\": true, \"duration\": \"5d\", \"discountPercentage\": null, \"directionality\": \"\", \"availabilityInfo\": [{\"availabilities\": {\"displayDate\": \"Tue, Feb 24\", \"valueDate\": \"2015-02-24 07:30:00\", \"allDayActivity\": false }, \"tickets\": [{\"code\": \"Adult\", \"ticketId\": \"90054\", \"name\": \"Adult\", \"restrictionText\": \"13+ years\", \"price\": \"$210\", \"originalPrice\": \"\", \"amount\": \"210\", \"displayName\": null, \"defaultTicketCount\": 2 }, {\"code\": \"Child\", \"ticketId\": \"90055\", \"name\": \"Child\", \"restrictionText\": \"4-12 years\", \"price\": \"$155\", \"originalPrice\": \"\", \"amount\": \"155\", \"displayName\": null, \"defaultTicketCount\": 0 } ] } ], \"direction\": null }, {\"id\": \"183625\", \"title\": \"7-Day New York Pass\", \"description\": \"\", \"currencySymbol\": \"$\", \"currencyDisplayedLeft\": true, \"freeCancellation\": true, \"duration\": \"7d\", \"discountPercentage\": \"8\", \"directionality\": \"\", \"availabilityInfo\": [{\"availabilities\": {\"displayDate\": \"Tue, Feb 24\", \"valueDate\": \"2015-02-24 07:30:00\", \"allDayActivity\": false }, \"tickets\": [{\"code\": \"Adult\", \"ticketId\": \"90062\", \"name\": \"Adult\", \"restrictionText\": \"13+ years\", \"price\": \"$210\", \"originalPrice\": \"$230\", \"amount\": \"210\", \"displayName\": null, \"defaultTicketCount\": 2 }, {\"code\": \"Child\", \"ticketId\": \"90063\", \"name\": \"Child\", \"restrictionText\": \"4-12 years\", \"price\": \"$155\", \"originalPrice\": \"$165\", \"amount\": \"155\", \"displayName\": null, \"defaultTicketCount\": 0 } ] } ], \"direction\": null } ], \"priceFootnote\": \"*Taxes included\", \"sameDateSearch\": true }, \"dateAdjusted\": false, \"typeGT\": false, \"passengers\": null, \"bags\": null, \"metaDescription\": \"Whether you know exactly where you want to visit or you're improvising, The New York Pass offers something just right for you.\", \"metaKeywords\": \"New York Pass: Visit up to 80 Attractions, Museums & Tours, New York, United States, New York Attractions, New York Cruises & Water Tours, New York Sightseeing Passes, New York Tours & Sightseeing, New York Activities, New York Things To Do , Book New York Activities , Book New York Things To Do , Activities, Things To Do\", \"pageTitle\": \"New York Pass: Visit up to 80 Attractions, Museums &amp; Tours\", \"category\": \"Attractions\"}", LXActivity::class.java)


        lxActivityOne.originalPrice = Money(10, "USD")
        lxActivityOne.price = Money(10, "USD")

        lxActivityTwo.price = Money(20, "USD")
        lxActivityTwo.originalPrice = Money(10, "USD")

        val activities = ArrayList<LXActivity>()
        activities.add(lxActivityOne)
        activities.add(lxActivityTwo)
        val recommendedActivitiesResponse = RecommendedActivitiesResponse(activities, "USD")

        return recommendedActivitiesResponse
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
        var now = LocalDate(2016, 7, 17);

        details.buildOfferDatesSelector(getOfferDetails().offersDetail, now)
        val container = details.findViewById(R.id.offer_dates_container) as LinearLayout

        val count = container.childCount
        val range = activity.baseContext.resources.getInteger(R.integer.lx_default_search_range) + 1;
        val dateOne = container.getChildAt(0) as LXOfferDatesButton
        val dateTwo = container.getChildAt(1) as LXOfferDatesButton

        assertNotNull(container)
        assertEquals(range, count);
        assertTrue(dateOne.isChecked)
        assertFalse(dateTwo.isChecked)
    }

    @Test
    fun testDatesChanges() {
        details.activityDetails = getOfferDetails()
        var now = LocalDate(2016, 7, 17);

        details.buildOfferDatesSelector(getOfferDetails().offersDetail, now)
        val container = details.findViewById(R.id.offer_dates_container) as LinearLayout

        val count = container.childCount
        val range = activity.baseContext.resources.getInteger(R.integer.lx_default_search_range) + 1;
        val dateOne = container.getChildAt(0) as LXOfferDatesButton
        val dateTwo = container.getChildAt(1) as LXOfferDatesButton
        val dateThree = container.getChildAt(3) as LXOfferDatesButton

        assertNotNull(container)
        assertEquals(range, count);
        assertTrue(dateOne.isChecked)
        assertFalse(dateTwo.isChecked)

        assertEquals("Sun\n17\nJul", dateOne.text.toString())

        var selectedDate = LocalDate(2016, 7, 20);

        details.onDetailsDateChanged(selectedDate, dateThree)
        assertEquals("Wed\n20\nJul", dateThree.text.toString())

        assertTrue(dateThree.isChecked)
    }

    @Test
    fun testActivityOffer() {
        var now = LocalDate(2016, 7, 17);
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
        assertEquals(3, count);

        // First Offer
        assertEquals("1-Day Ticket", offerTitleOne.text.toString());
        assertEquals("$45 Adult, $25 Child", offerPriceSummaryOne.text.toString());

        // Second Offer
        assertEquals("2-Day Ticket", offerTitleTwo.text.toString());
        assertEquals("$55 Adult, $35 Child", offerPriceSummaryTwo.text.toString());
        assertEquals(offerRowTwo.visibility, View.VISIBLE)
        offerSelectTicketTwo.performClick()

        assertEquals(offerRowTwo.visibility, View.GONE)
    }

    @Test
    fun testOffersExpandCollapse() {
        var now = LocalDate(2016, 7, 17);
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