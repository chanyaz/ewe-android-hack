package com.expedia.bookings.presenter.lx

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LXState
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.abacus.AbacusResponse
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.lx.LXActivity
import com.expedia.bookings.data.lx.LXCategoryMetadata
import com.expedia.bookings.data.lx.LXCategoryType
import com.expedia.bookings.data.lx.LXSearchResponse
import com.expedia.bookings.data.lx.LxSearchParams
import com.expedia.bookings.data.lx.SearchType
import com.expedia.bookings.otto.Events
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.FilterButtonWithCountWidget
import com.expedia.bookings.widget.LXErrorWidget
import com.expedia.bookings.widget.LXResultsListAdapter
import com.expedia.bookings.widget.LXSearchResultsWidget
import com.expedia.bookings.widget.LXThemeResultsWidget
import com.google.gson.GsonBuilder
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.shadows.ShadowAlertDialog
import java.io.IOException
import java.util.ArrayList
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(RobolectricRunner::class)
@RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ))
class LXResultsPresenterTest {
    var lxResultsPresenter by Delegates.notNull<LXResultsPresenter>()

    @Before
    fun before() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_LX)
        Ui.getApplication(activity).defaultLXComponents()
        lxResultsPresenter = LayoutInflater.from(activity).inflate(R.layout.lx_result_presenter, null) as LXResultsPresenter
    }

    @Test
    fun testToolbar() {
        val searchParams = buildSearchParams()
        val expectedToolbarDateRange = searchParams.startDate.toString("MMM d") + " - " + searchParams.endDate!!.toString("MMM d")

        assertEquals(searchParams.location, lxResultsPresenter.toolBarDetailText.text);
        assertEquals(expectedToolbarDateRange, lxResultsPresenter.toolBarSubtitleText.text);
    }

    @Test
    fun testSearchResultsList() {
        val gson = GsonBuilder().create()
        val lxActivities = ArrayList<LXActivity>()
        for (index in 0..10) {
            val lxActivity = gson.fromJson<LXActivity>(
                    "{\"id\": \"183615\", \"title\": \"New York Pass: Visit up to 80 Attractions, Museums & Tours" + index + "\", \"description\": \"<p>Whether you know exactly where you want to visit or you're improvising, The New York Pass offers something just right for you.</p>\", \"images\": [{\"url\": \"//a.travel-assets.com/mediavault.le/media/c932f66857388ec282910f62d64354eff6760223.jpeg\", \"caption\": \"notitle\", \"creditString\": \"nocredit\"}, {\"url\": \"//a.travel-assets.com/mediavault.le/media/4da13469be6dffa5ff880b0a3cc59cb58e6690bc.jpeg\", \"caption\": \"notitle\", \"creditString\": \"nocredit\"}, {\"url\": \"//a.travel-assets.com/mediavault.le/media/efe62c374095946276d943831420ccad01e47396.jpeg\", \"caption\": \"notitle\", \"creditString\": \"nocredit\"}, {\"url\": \"//a.travel-assets.com/mediavault.le/media/cc66fbb8e40c74c670f32124aac534e29128e01b.jpeg\", \"caption\": \"notitle\", \"creditString\": \"nocredit\"}, {\"url\": \"//a.travel-assets.com/mediavault.le/media/909d80624ff13a43a950e0149adf49bf23195774.jpeg\", \"caption\": \"notitle\", \"creditString\": \"nocredit\"} ], \"highlights\": [\"<p>Admission to more than 80 top attractions, museums &amp; tours</p>\", \"<p>VIP Fast-Track entry at several top attractions</p>\", \"<p>200-page guidebook with area maps &amp; attraction details</p>\", \"<p>Guidebook offered in multiple languages</p>\", \"<p>7-Day Pass for the 5-Day Pass price when you buy now</p>\"], \"fromPrice\": \"$130\", \"fromOriginalPrice\": \"\", \"fromPriceTicketType\": \"Adult\", \"startDate\": \"2015-02-24\", \"endDate\": \"2015-02-24\", \"maximumBookingLength\": null, \"lastValidDate\": null, \"firstValidDate\": null, \"duration\": \"2d\", \"inclusions\": [\"<p>Admission to more than 80 top attractions, museums, &amp; tours</p>\", \"<p>200-page guidebook offered in English, Italian, Spanish, French, German, and Brazilian Portuguese</p>\", \"<p>Skip-the-Line access at many attractions</p>\", \"<p>Special discounts at restaurants &amp; retailers</p>\"], \"isMultiDuration\": \"true\", \"exclusions\": [\"<p>Transportation to and from attractions</p>\"], \"freeCancellationMinHours\": 72, \"knowBeforeYouBook\": [\"<p>Children 3 and younger are complimentary at most attractions.</p>\", \"<p>Pass is valid for the number of consecutive calendar days you purchase, beginning on the first day of use.</p>\", \"<p>Advance reservations may be made for tours included on the pass; mention you are a New York Pass holder.</p>\", \"<p>Hours and dates of operation for individual attractions vary.</p>\", \"<p>Get the 7-Day Pass for the 5-Day Price when you buy now, savings reflected in the price above.</p>\"], \"freeCancellation\": true, \"discountPercentage\": 0, \"address\": \"Gray Line New York Visitors Center, 777 8th Avenue\\r\\nbetween 47th and 48th Street, New York, NY 10036, United States\", \"location\": \"New York, United States\", \"regionId\": \"178293\", \"destination\": \"New York\", \"fullName\": \"New York (and vicinity), New York, United States of America\", \"omnitureJson\": \"{\\\"accountName\\\":\\\"expedia1\\\",\\\"omnitureProperties\\\":{\\\"server\\\":\\\"www.expedia.com\\\",\\\"authChannel\\\":\\\"SIGNIN_FORM\\\",\\\"eVar6\\\":\\\"0\\\",\\\"eVar5\\\":\\\"4\\\",\\\"eVar4\\\":\\\"D\\\\u003dc4\\\",\\\"eVar2\\\":\\\"D\\\\u003dc2\\\",\\\"channel\\\":\\\"local expert\\\",\\\"pageName\\\":\\\"page.LX.Infosite.Information\\\",\\\"products\\\":\\\"LX;Merchant LX:183615\\\",\\\"activityId\\\":\\\"4f04feaa-fdf0-4f40-8de1-6ffa262e28f3\\\",\\\"prop30\\\":\\\"1033\\\",\\\"prop11\\\":\\\"null\\\",\\\"prop13\\\":\\\"0\\\",\\\"prop12\\\":\\\"eba1f647-61a7-406f-8bda-6f0c7124e789\\\",\\\"prop34\\\":\\\"6880.0|6611.0|6727.0|5242.0|6524.1|6620.0|5150.1|6271.1\\\",\\\"events\\\":\\\"event3\\\",\\\"charSet\\\":\\\"UTF-8\\\",\\\"eVar34\\\":\\\"D\\\\u003dc34\\\",\\\"eVar56\\\":\\\"RewardsStatus()\\\",\\\"eVar55\\\":\\\"unknown\\\",\\\"eVar54\\\":\\\"1033\\\",\\\"eVar18\\\":\\\"D\\\\u003dpageName\\\",\\\"eVar17\\\":\\\"D\\\\u003dpageName\\\",\\\"prop6\\\":\\\"2015-02-24\\\",\\\"prop5\\\":\\\"2015-02-24\\\",\\\"prop4\\\":\\\"178293\\\",\\\"list1\\\":\\\"6880.0|6611.0|6727.0|5242.0|6524.1|6620.0|5150.1|6271.1\\\",\\\"prop2\\\":\\\"local expert\\\",\\\"prop1\\\":\\\"4\\\",\\\"userType\\\":\\\"ANONYMOUS\\\"}}\", \"offersDetail\": {\"offers\": [{\"id\": \"183619\", \"title\": \"2-Day New York Pass\", \"description\": \"\", \"currencySymbol\": \"$\", \"currencyDisplayedLeft\": true, \"freeCancellation\": true, \"duration\": \"2d\", \"discountPercentage\": null, \"directionality\": \"\", \"availabilityInfo\": [{\"availabilities\": {\"displayDate\": \"Tue, Feb 24\", \"valueDate\": \"2015-02-24 07:30:00\", \"allDayActivity\": false }, \"tickets\": [{\"code\": \"Adult\", \"ticketId\": \"90042\", \"name\": \"Adult\", \"restrictionText\": \"13+ years\", \"price\": \"$130\", \"originalPrice\": \"123\", \"amount\": \"130\", \"displayName\": null, \"defaultTicketCount\": 2 }, {\"code\": \"Child\", \"ticketId\": \"90043\", \"name\": \"Child\", \"restrictionText\": \"4-12 years\", \"price\": \"$110\", \"originalPrice\": \"\", \"amount\": \"110\", \"displayName\": null, \"defaultTicketCount\": 0 } ] } ], \"direction\": null }, {\"id\": \"183621\", \"title\": \"3-Day New York Pass\", \"description\": \"\", \"currencySymbol\": \"$\", \"currencyDisplayedLeft\": true, \"freeCancellation\": true, \"duration\": \"3d\", \"discountPercentage\": null, \"directionality\": \"\", \"availabilityInfo\": [{\"availabilities\": {\"displayDate\": \"Tue, Feb 24\", \"valueDate\": \"2015-02-24 07:30:00\", \"allDayActivity\": false }, \"tickets\": [{\"code\": \"Adult\", \"ticketId\": \"90046\", \"name\": \"Adult\", \"restrictionText\": \"13+ years\", \"price\": \"$180\", \"originalPrice\": \"\", \"amount\": \"180\", \"displayName\": null, \"defaultTicketCount\": 2 }, {\"code\": \"Child\", \"ticketId\": \"90047\", \"name\": \"Child\", \"restrictionText\": \"4-12 years\", \"price\": \"$140\", \"originalPrice\": \"\", \"amount\": \"140\", \"displayName\": null, \"defaultTicketCount\": 0 } ] } ], \"direction\": null }, {\"id\": \"183623\", \"title\": \"5-Day New York Pass\", \"description\": \"\", \"currencySymbol\": \"$\", \"currencyDisplayedLeft\": true, \"freeCancellation\": true, \"duration\": \"5d\", \"discountPercentage\": null, \"directionality\": \"\", \"availabilityInfo\": [{\"availabilities\": {\"displayDate\": \"Tue, Feb 24\", \"valueDate\": \"2015-02-24 07:30:00\", \"allDayActivity\": false }, \"tickets\": [{\"code\": \"Adult\", \"ticketId\": \"90054\", \"name\": \"Adult\", \"restrictionText\": \"13+ years\", \"price\": \"$210\", \"originalPrice\": \"\", \"amount\": \"210\", \"displayName\": null, \"defaultTicketCount\": 2 }, {\"code\": \"Child\", \"ticketId\": \"90055\", \"name\": \"Child\", \"restrictionText\": \"4-12 years\", \"price\": \"$155\", \"originalPrice\": \"\", \"amount\": \"155\", \"displayName\": null, \"defaultTicketCount\": 0 } ] } ], \"direction\": null }, {\"id\": \"183625\", \"title\": \"7-Day New York Pass\", \"description\": \"\", \"currencySymbol\": \"$\", \"currencyDisplayedLeft\": true, \"freeCancellation\": true, \"duration\": \"7d\", \"discountPercentage\": \"8\", \"directionality\": \"\", \"availabilityInfo\": [{\"availabilities\": {\"displayDate\": \"Tue, Feb 24\", \"valueDate\": \"2015-02-24 07:30:00\", \"allDayActivity\": false }, \"tickets\": [{\"code\": \"Adult\", \"ticketId\": \"90062\", \"name\": \"Adult\", \"restrictionText\": \"13+ years\", \"price\": \"$210\", \"originalPrice\": \"$230\", \"amount\": \"210\", \"displayName\": null, \"defaultTicketCount\": 2 }, {\"code\": \"Child\", \"ticketId\": \"90063\", \"name\": \"Child\", \"restrictionText\": \"4-12 years\", \"price\": \"$155\", \"originalPrice\": \"$165\", \"amount\": \"155\", \"displayName\": null, \"defaultTicketCount\": 0 } ] } ], \"direction\": null } ], \"priceFootnote\": \"*Taxes included\", \"sameDateSearch\": true }, \"dateAdjusted\": false, \"typeGT\": false, \"passengers\": null, \"bags\": null, \"metaDescription\": \"Whether you know exactly where you want to visit or you're improvising, The New York Pass offers something just right for you.\", \"metaKeywords\": \"New York Pass: Visit up to 80 Attractions, Museums & Tours, New York, United States, New York Attractions, New York Cruises & Water Tours, New York Sightseeing Passes, New York Tours & Sightseeing, New York Activities, New York Things To Do , Book New York Activities , Book New York Things To Do , Activities, Things To Do\", \"pageTitle\": \"New York Pass: Visit up to 80 Attractions, Museums &amp; Tours\", \"category\": \"Attractions\"}", LXActivity::class.java)
            lxActivity.originalPrice = Money(10, "USD")
            lxActivity.price = Money(10 + index, "USD")
            lxActivities.add(lxActivity)
        }
        lxResultsPresenter.searchResultsWidget.adapter.items = lxActivities

        val recyclerView = lxResultsPresenter.searchResultsWidget.recyclerView
        val searchAdapter = lxResultsPresenter.searchResultsWidget.adapter
        val itemCount = searchAdapter.itemCount
        for (index in 0..(itemCount - 1)) {
            val viewHolder = searchAdapter.createViewHolder(recyclerView, searchAdapter.getItemViewType(index)) as LXResultsListAdapter.ViewHolder
            searchAdapter.onBindViewHolder(viewHolder, index)
            assertEquals("New York Pass: Visit up to 80 Attractions, Museums & Tours" + index, (viewHolder.itemView.findViewById<TextView>(R.id.activity_title))?.text)
            assertEquals(lxActivities[index].originalPrice.getFormattedMoney(Money.F_NO_DECIMAL), (viewHolder.itemView.findViewById<TextView>(R.id.activity_original_price))?.text.toString())
            assertEquals("2d+", (viewHolder.itemView.findViewById<TextView>(R.id.activity_duration))?.text.toString())
        }
    }

    @Test
    fun testNoSearchResults() {
        Events.register(lxResultsPresenter)
        Events.register(lxResultsPresenter.searchResultsWidget)

        val errorWidget = lxResultsPresenter.findViewById<LXErrorWidget>(R.id.lx_search_error_widget)
        val searResultObserver = lxResultsPresenter.SearchResultObserver()
        searResultObserver.widget = lxResultsPresenter.searchResultsWidget
        searResultObserver.searchType = SearchType.EXPLICIT_SEARCH
        searResultObserver.onError(ApiError(ApiError.Code.LX_SEARCH_NO_RESULTS))
        assertEquals(View.VISIBLE, errorWidget.visibility)
        assertEquals("Your search returned no results. Please retry your search with different criteria.", (errorWidget.findViewById<TextView>(R.id.error_text))?.text)
    }

    @Test
    fun testisUserBucketedForCategoriesTest() {
        val themeResultsWidget = lxResultsPresenter.findViewById<LXThemeResultsWidget>(R.id.lx_theme_results_widget)
        val searchResultsWidget = lxResultsPresenter.findViewById<LXSearchResultsWidget>(R.id.lx_search_results_widget)
        val toolbarDetailText =  lxResultsPresenter.findViewById<TextView>(R.id.toolbar_detail_text)
        val toolbarSubtitleText =  lxResultsPresenter.findViewById<TextView>(R.id.toolbar_subtitle_text)
        val lxThemeList = lxResultsPresenter.findViewById<android.support.v7.widget.RecyclerView>(R.id.lx_theme_list)
        val toolbarSortFilter = lxResultsPresenter.findViewById<ViewGroup>(R.id.toolbar_sort_filter)
        val sortText = toolbarSortFilter.getChildAt(0) as TextView
        val filterText = lxResultsPresenter.findViewById<TextView>(R.id.filter_text)

        lxCategoriesABTest(AbacusUtils.DefaultVariant.BUCKETED)
        buildSearchParams()
        assertEquals(View.VISIBLE, themeResultsWidget.visibility)
        assertEquals(View.GONE, searchResultsWidget.visibility)
        assertEquals(View.VISIBLE, lxThemeList.visibility)
        assertEquals(View.VISIBLE, toolbarSortFilter.visibility)
        assertEquals("Select a Category", toolbarDetailText.text)
        assertEquals("New York", toolbarSubtitleText.text)
        assertEquals("Sort", sortText.text)
        assertEquals("Sort", filterText.text)
        filterText.performClick()
        assertNotNull(lxResultsPresenter.searchSubscription)
        assertEquals(false, lxResultsPresenter.searchSubscription.isDisposed)

        lxCategoriesABTest(AbacusUtils.DefaultVariant.CONTROL)

    }

    @Test
    fun testWhenNoInternetConnected() {

        val searchResultObserver = lxResultsPresenter.SearchResultObserver()
        val lxSearchResultWidget = lxResultsPresenter.findViewById<LXSearchResultsWidget>(R.id.lx_search_results_widget)

        searchResultObserver.widget = lxResultsPresenter.searchResultsWidget
        searchResultObserver.searchType = SearchType.EXPLICIT_SEARCH
        searchResultObserver.onError(IOException())

        val alertDialog = ShadowAlertDialog.getLatestAlertDialog()
        val errorMessage = alertDialog.findViewById<TextView>(android.R.id.message)
        val cancelButton = alertDialog.findViewById<Button>(android.R.id.button2)
        val retryButton = alertDialog.findViewById<Button>(android.R.id.button1)

        assertEquals(true ,alertDialog.isShowing)
        assertEquals("Your device is not connected to the internet.  Please check your connection and try again.", errorMessage.text)
        assertEquals("Cancel",cancelButton.text )
        assertEquals("Retry",retryButton.text )

        //Tap on cancel button
        cancelButton.performClick()
        assertEquals(false, alertDialog.isShowing)
        //Tap on retry button
        searchResultObserver.onError(IOException())
        retryButton.performClick()
        assertEquals(false, alertDialog.isShowing)
        assertEquals(View.VISIBLE, lxSearchResultWidget.visibility)

    }

    @Test
    fun testSearchResultObserver() {
        val lxSearchResponse = createLxSearchResponse()
        buildSearchParams()
        val searResultObserver = lxResultsPresenter.SearchResultObserver()
        searResultObserver.onNext(lxSearchResponse)
        val sortFilterButton = lxResultsPresenter.findViewById<FilterButtonWithCountWidget>(R.id.sort_filter_button_container)
        val filterIcon = lxResultsPresenter.findViewById<View>(R.id.filter_icon)
        val searchResultsWidget = lxResultsPresenter.findViewById<LXSearchResultsWidget>(R.id.lx_search_results_widget)
        val recyclerView = searchResultsWidget.findViewById<RecyclerView>(R.id.lx_search_results_list)
        val errorScreen = searchResultsWidget.findViewById<LXErrorWidget>(R.id.lx_search_error_widget)
        val themeResultsWidget = lxResultsPresenter.findViewById<LXThemeResultsWidget>(R.id.lx_theme_results_widget)
        val holder = searchResultsWidget.recyclerView.adapter.createViewHolder(searchResultsWidget.recyclerView,1) as LXResultsListAdapter.ViewHolder

        searchResultsWidget.recyclerView.adapter.bindViewHolder(holder,0)
        assertNotNull(searchResultsWidget)
        val actualActivityTitle = holder.itemView.findViewById<TextView>(R.id.activity_title)
        assertEquals(3, searchResultsWidget.recyclerView.adapter.itemCount)
        assertEquals("New York Pass: Visit up to 80 Attractions, Museums & Tours0", actualActivityTitle.text)
        assertEquals(View.VISIBLE, sortFilterButton.visibility)
        assertEquals(View.VISIBLE, recyclerView.visibility)
        assertEquals(View.GONE, errorScreen.visibility)
        assertEquals(View.GONE, themeResultsWidget.visibility)
        assertEquals(View.VISIBLE, filterIcon.visibility)
        searResultObserver.onComplete()
        assertEquals(true, lxResultsPresenter.searchSubscription.isDisposed)

    }

    @Test
    fun testOnLXSearchError() {
        Events.register(lxResultsPresenter)
        Events.post(Events.LXShowSearchError(ApiError(ApiError.Code.LX_DETAILS_FETCH_ERROR),SearchType.DEFAULT_SEARCH))
        val toolBarDetailText = lxResultsPresenter.findViewById<TextView>(R.id.toolbar_detail_text)
        val toolBarSubtitleText = lxResultsPresenter.findViewById<TextView>(R.id.toolbar_subtitle_text)
        assertEquals("Please try again", toolBarDetailText.text)
        assertEquals(View.GONE, toolBarSubtitleText.visibility)

    }
    @Test
    fun testOnLXShowLoadingAnimation() {
        Events.register(lxResultsPresenter)
        val themeResultsWidget = lxResultsPresenter.findViewById<LXThemeResultsWidget>(R.id.lx_theme_results_widget)
        val searchResultsWidget = lxResultsPresenter.findViewById<LXSearchResultsWidget>(R.id.lx_search_results_widget)
        Events.post(Events.LXShowLoadingAnimation())
        assertEquals(View.GONE, themeResultsWidget.visibility)
        assertEquals(View.VISIBLE, searchResultsWidget.visibility)

        lxCategoriesABTest(AbacusUtils.DefaultVariant.BUCKETED)
        lxResultsPresenter.setUserBucketedForCategoriesTest(true)

        Events.post(Events.LXShowLoadingAnimation())
        assertEquals(View.VISIBLE, themeResultsWidget.visibility)
        assertEquals(View.GONE, searchResultsWidget.visibility)

        lxCategoriesABTest(AbacusUtils.DefaultVariant.CONTROL)
    }

    private fun lxCategoriesABTest(defaultVariate: AbacusUtils.DefaultVariant) {
        val abacusResponse = AbacusResponse()
        abacusResponse.updateABTestForDebug(AbacusUtils.EBAndroidAppLXCategoryABTest.key,
                defaultVariate.ordinal)
        Db.sharedInstance.setAbacusResponse(abacusResponse)
    }

    fun createLxSearchResponse(): LXSearchResponse {
        val lxSearchResponse = LXSearchResponse()
        val lxcategoryMetadata = LXCategoryMetadata()
        lxcategoryMetadata.displayValue = "Attractions"
        lxcategoryMetadata.categoryKeyEN = "Attractions"
        lxcategoryMetadata.categoryType = LXCategoryType.PrivateTransfers
        val filterCategories = java.util.HashMap<String, LXCategoryMetadata>()
        filterCategories.put("PrivateTransfers", lxcategoryMetadata )
        val gson = GsonBuilder().create()
        val lxActivities = ArrayList<LXActivity>()

        for (index in 0..2) {
            val lxActivity = gson.fromJson<LXActivity>(
                    "{\"id\": \"183615\", \"title\": \"New York Pass: Visit up to 80 Attractions, Museums & Tours" + index + "\", \"description\": \"<p>Whether you know exactly where you want to visit or you're improvising, The New York Pass offers something just right for you.</p>\", \"images\": [{\"url\": \"//a.travel-assets.com/mediavault.le/media/c932f66857388ec282910f62d64354eff6760223.jpeg\", \"caption\": \"notitle\", \"creditString\": \"nocredit\"}, {\"url\": \"//a.travel-assets.com/mediavault.le/media/4da13469be6dffa5ff880b0a3cc59cb58e6690bc.jpeg\", \"caption\": \"notitle\", \"creditString\": \"nocredit\"}, {\"url\": \"//a.travel-assets.com/mediavault.le/media/efe62c374095946276d943831420ccad01e47396.jpeg\", \"caption\": \"notitle\", \"creditString\": \"nocredit\"}, {\"url\": \"//a.travel-assets.com/mediavault.le/media/cc66fbb8e40c74c670f32124aac534e29128e01b.jpeg\", \"caption\": \"notitle\", \"creditString\": \"nocredit\"}, {\"url\": \"//a.travel-assets.com/mediavault.le/media/909d80624ff13a43a950e0149adf49bf23195774.jpeg\", \"caption\": \"notitle\", \"creditString\": \"nocredit\"} ], \"highlights\": [\"<p>Admission to more than 80 top attractions, museums &amp; tours</p>\", \"<p>VIP Fast-Track entry at several top attractions</p>\", \"<p>200-page guidebook with area maps &amp; attraction details</p>\", \"<p>Guidebook offered in multiple languages</p>\", \"<p>7-Day Pass for the 5-Day Pass price when you buy now</p>\"], \"fromPrice\": \"$130\", \"fromOriginalPrice\": \"\", \"fromPriceTicketType\": \"Adult\", \"startDate\": \"2015-02-24\", \"endDate\": \"2015-02-24\", \"maximumBookingLength\": null, \"lastValidDate\": null, \"firstValidDate\": null, \"duration\": \"2d\", \"inclusions\": [\"<p>Admission to more than 80 top attractions, museums, &amp; tours</p>\", \"<p>200-page guidebook offered in English, Italian, Spanish, French, German, and Brazilian Portuguese</p>\", \"<p>Skip-the-Line access at many attractions</p>\", \"<p>Special discounts at restaurants &amp; retailers</p>\"], \"isMultiDuration\": \"true\", \"exclusions\": [\"<p>Transportation to and from attractions</p>\"], \"freeCancellationMinHours\": 72, \"knowBeforeYouBook\": [\"<p>Children 3 and younger are complimentary at most attractions.</p>\", \"<p>Pass is valid for the number of consecutive calendar days you purchase, beginning on the first day of use.</p>\", \"<p>Advance reservations may be made for tours included on the pass; mention you are a New York Pass holder.</p>\", \"<p>Hours and dates of operation for individual attractions vary.</p>\", \"<p>Get the 7-Day Pass for the 5-Day Price when you buy now, savings reflected in the price above.</p>\"], \"freeCancellation\": true, \"discountPercentage\": 0, \"address\": \"Gray Line New York Visitors Center, 777 8th Avenue\\r\\nbetween 47th and 48th Street, New York, NY 10036, United States\", \"location\": \"New York, United States\", \"regionId\": \"178293\", \"destination\": \"New York\", \"fullName\": \"New York (and vicinity), New York, United States of America\", \"omnitureJson\": \"{\\\"accountName\\\":\\\"expedia1\\\",\\\"omnitureProperties\\\":{\\\"server\\\":\\\"www.expedia.com\\\",\\\"authChannel\\\":\\\"SIGNIN_FORM\\\",\\\"eVar6\\\":\\\"0\\\",\\\"eVar5\\\":\\\"4\\\",\\\"eVar4\\\":\\\"D\\\\u003dc4\\\",\\\"eVar2\\\":\\\"D\\\\u003dc2\\\",\\\"channel\\\":\\\"local expert\\\",\\\"pageName\\\":\\\"page.LX.Infosite.Information\\\",\\\"products\\\":\\\"LX;Merchant LX:183615\\\",\\\"activityId\\\":\\\"4f04feaa-fdf0-4f40-8de1-6ffa262e28f3\\\",\\\"prop30\\\":\\\"1033\\\",\\\"prop11\\\":\\\"null\\\",\\\"prop13\\\":\\\"0\\\",\\\"prop12\\\":\\\"eba1f647-61a7-406f-8bda-6f0c7124e789\\\",\\\"prop34\\\":\\\"6880.0|6611.0|6727.0|5242.0|6524.1|6620.0|5150.1|6271.1\\\",\\\"events\\\":\\\"event3\\\",\\\"charSet\\\":\\\"UTF-8\\\",\\\"eVar34\\\":\\\"D\\\\u003dc34\\\",\\\"eVar56\\\":\\\"RewardsStatus()\\\",\\\"eVar55\\\":\\\"unknown\\\",\\\"eVar54\\\":\\\"1033\\\",\\\"eVar18\\\":\\\"D\\\\u003dpageName\\\",\\\"eVar17\\\":\\\"D\\\\u003dpageName\\\",\\\"prop6\\\":\\\"2015-02-24\\\",\\\"prop5\\\":\\\"2015-02-24\\\",\\\"prop4\\\":\\\"178293\\\",\\\"list1\\\":\\\"6880.0|6611.0|6727.0|5242.0|6524.1|6620.0|5150.1|6271.1\\\",\\\"prop2\\\":\\\"local expert\\\",\\\"prop1\\\":\\\"4\\\",\\\"userType\\\":\\\"ANONYMOUS\\\"}}\", \"offersDetail\": {\"offers\": [{\"id\": \"183619\", \"title\": \"2-Day New York Pass\", \"description\": \"\", \"currencySymbol\": \"$\", \"currencyDisplayedLeft\": true, \"freeCancellation\": true, \"duration\": \"2d\", \"discountPercentage\": null, \"directionality\": \"\", \"availabilityInfo\": [{\"availabilities\": {\"displayDate\": \"Tue, Feb 24\", \"valueDate\": \"2015-02-24 07:30:00\", \"allDayActivity\": false }, \"tickets\": [{\"code\": \"Adult\", \"ticketId\": \"90042\", \"name\": \"Adult\", \"restrictionText\": \"13+ years\", \"price\": \"$130\", \"originalPrice\": \"123\", \"amount\": \"130\", \"displayName\": null, \"defaultTicketCount\": 2 }, {\"code\": \"Child\", \"ticketId\": \"90043\", \"name\": \"Child\", \"restrictionText\": \"4-12 years\", \"price\": \"$110\", \"originalPrice\": \"\", \"amount\": \"110\", \"displayName\": null, \"defaultTicketCount\": 0 } ] } ], \"direction\": null }, {\"id\": \"183621\", \"title\": \"3-Day New York Pass\", \"description\": \"\", \"currencySymbol\": \"$\", \"currencyDisplayedLeft\": true, \"freeCancellation\": true, \"duration\": \"3d\", \"discountPercentage\": null, \"directionality\": \"\", \"availabilityInfo\": [{\"availabilities\": {\"displayDate\": \"Tue, Feb 24\", \"valueDate\": \"2015-02-24 07:30:00\", \"allDayActivity\": false }, \"tickets\": [{\"code\": \"Adult\", \"ticketId\": \"90046\", \"name\": \"Adult\", \"restrictionText\": \"13+ years\", \"price\": \"$180\", \"originalPrice\": \"\", \"amount\": \"180\", \"displayName\": null, \"defaultTicketCount\": 2 }, {\"code\": \"Child\", \"ticketId\": \"90047\", \"name\": \"Child\", \"restrictionText\": \"4-12 years\", \"price\": \"$140\", \"originalPrice\": \"\", \"amount\": \"140\", \"displayName\": null, \"defaultTicketCount\": 0 } ] } ], \"direction\": null }, {\"id\": \"183623\", \"title\": \"5-Day New York Pass\", \"description\": \"\", \"currencySymbol\": \"$\", \"currencyDisplayedLeft\": true, \"freeCancellation\": true, \"duration\": \"5d\", \"discountPercentage\": null, \"directionality\": \"\", \"availabilityInfo\": [{\"availabilities\": {\"displayDate\": \"Tue, Feb 24\", \"valueDate\": \"2015-02-24 07:30:00\", \"allDayActivity\": false }, \"tickets\": [{\"code\": \"Adult\", \"ticketId\": \"90054\", \"name\": \"Adult\", \"restrictionText\": \"13+ years\", \"price\": \"$210\", \"originalPrice\": \"\", \"amount\": \"210\", \"displayName\": null, \"defaultTicketCount\": 2 }, {\"code\": \"Child\", \"ticketId\": \"90055\", \"name\": \"Child\", \"restrictionText\": \"4-12 years\", \"price\": \"$155\", \"originalPrice\": \"\", \"amount\": \"155\", \"displayName\": null, \"defaultTicketCount\": 0 } ] } ], \"direction\": null }, {\"id\": \"183625\", \"title\": \"7-Day New York Pass\", \"description\": \"\", \"currencySymbol\": \"$\", \"currencyDisplayedLeft\": true, \"freeCancellation\": true, \"duration\": \"7d\", \"discountPercentage\": \"8\", \"directionality\": \"\", \"availabilityInfo\": [{\"availabilities\": {\"displayDate\": \"Tue, Feb 24\", \"valueDate\": \"2015-02-24 07:30:00\", \"allDayActivity\": false }, \"tickets\": [{\"code\": \"Adult\", \"ticketId\": \"90062\", \"name\": \"Adult\", \"restrictionText\": \"13+ years\", \"price\": \"$210\", \"originalPrice\": \"$230\", \"amount\": \"210\", \"displayName\": null, \"defaultTicketCount\": 2 }, {\"code\": \"Child\", \"ticketId\": \"90063\", \"name\": \"Child\", \"restrictionText\": \"4-12 years\", \"price\": \"$155\", \"originalPrice\": \"$165\", \"amount\": \"155\", \"displayName\": null, \"defaultTicketCount\": 0 } ] } ], \"direction\": null } ], \"priceFootnote\": \"*Taxes included\", \"sameDateSearch\": true }, \"dateAdjusted\": false, \"typeGT\": false, \"passengers\": null, \"bags\": null, \"metaDescription\": \"Whether you know exactly where you want to visit or you're improvising, The New York Pass offers something just right for you.\", \"metaKeywords\": \"New York Pass: Visit up to 80 Attractions, Museums & Tours, New York, United States, New York Attractions, New York Cruises & Water Tours, New York Sightseeing Passes, New York Tours & Sightseeing, New York Activities, New York Things To Do , Book New York Activities , Book New York Things To Do , Activities, Things To Do\", \"pageTitle\": \"New York Pass: Visit up to 80 Attractions, Museums &amp; Tours\", \"category\": \"Attractions\"}", LXActivity::class.java)
            lxActivity.originalPrice = Money(10, "USD")
            lxActivity.price = Money(10 + index, "USD")
            lxActivities.add(lxActivity)
        }
        lxSearchResponse.activities = lxActivities
        lxSearchResponse.filterCategories = filterCategories
        buildSearchParams()
        return lxSearchResponse
    }

    fun buildSearchParams(): LxSearchParams {
        val searchParams = LxSearchParams.Builder()
                .location("New York")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(14))
                .build() as LxSearchParams;


        lxResultsPresenter.lxState = LXState()
        lxResultsPresenter.lxState.searchParams = searchParams;

        lxResultsPresenter.onLXNewSearchParamsAvailable(Events.LXNewSearchParamsAvailable(searchParams))
        return searchParams
    }
}
