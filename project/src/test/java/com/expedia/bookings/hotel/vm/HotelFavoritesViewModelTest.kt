package com.expedia.bookings.hotel.vm

import android.content.Intent
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.data.Codes
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.shortlist.HotelShortlistItem
import com.expedia.bookings.data.hotels.shortlist.HotelShortlistResponse
import com.expedia.bookings.data.hotels.shortlist.HotelShortlistResult
import com.expedia.bookings.data.hotels.shortlist.ShortlistItem
import com.expedia.bookings.data.hotels.shortlist.ShortlistItemMetadata
import com.expedia.bookings.data.user.UserLoyaltyMembershipInformation
import com.expedia.bookings.hotel.deeplink.HotelExtras
import com.expedia.bookings.hotel.util.HotelCalendarRules
import com.expedia.bookings.hotel.util.HotelFavoritesCache
import com.expedia.bookings.hotel.util.HotelFavoritesManager
import com.expedia.bookings.services.HotelShortlistServices
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.UserLoginTestUtil.Companion.setupUserAndMockLogin
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.HotelsV2DataUtil
import com.expedia.bookings.utils.Ui
import com.expedia.ui.HotelActivity
import org.hamcrest.Matchers
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
@Config(shadows = [ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class])
class HotelFavoritesViewModelTest {

    var shortlistServicesRule = ServicesRule(HotelShortlistServices::class.java)
        @Rule get
    lateinit var mockAnalyticsProvider: AnalyticsProvider

    private val context = RuntimeEnvironment.application
    private val userStateManager = Ui.getApplication(context).appComponent().userStateManager()

    private lateinit var viewModel: HotelFavoritesViewModel
    private lateinit var favoritesManager: HotelFavoritesManager
    private lateinit var hotelShortlistItem: HotelShortlistItem

    private lateinit var twoDaysAgo: LocalDate
    private lateinit var yesterday: LocalDate
    private lateinit var today: LocalDate
    private lateinit var tomorrow: LocalDate
    private lateinit var twoDaysAhead: LocalDate

    private lateinit var minDate: LocalDate
    private lateinit var maxDate: LocalDate
    private var maxDuration = 0

    @Before
    fun setup() {
        twoDaysAgo = LocalDate.now().minusDays(2)
        yesterday = LocalDate.now().minusDays(1)
        today = LocalDate.now()
        tomorrow = LocalDate.now().plusDays(1)
        twoDaysAhead = LocalDate.now().plusDays(2)

        val calendarRules = HotelCalendarRules(context)
        minDate = calendarRules.getFirstAvailableDate()
        maxDate = LocalDate.now().plusDays(calendarRules.getMaxDateRange())
        maxDuration = calendarRules.getMaxSearchDurationDays()

        userStateManager.signOut()
        favoritesManager = HotelFavoritesManager(shortlistServicesRule.services!!)
        viewModel = HotelFavoritesViewModel(context, userStateManager, favoritesManager)
        hotelShortlistItem = createHotelShortlistItem()

        HotelFavoritesCache.clearFavorites(context)
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
    }

    @Test
    fun testLoggedIn() {
        signInUserWithLoyalty()
        val viewModel = HotelFavoritesViewModel(context, userStateManager, favoritesManager)
        val t = TestObserver<Unit>()
        viewModel.receivedResponseSubject.subscribe(t)
        t.assertValueCount(1)
        assertTrue(viewModel.shouldShowList())

        assertNotEquals(0, viewModel.compositeDisposable.size())
        viewModel.onClear()
        assertEquals(0, viewModel.compositeDisposable.size())
    }

    @Test
    fun testPageLoadedAnalyticsLoggedIn() {
        signInUserWithLoyalty()
        HotelFavoritesViewModel(context, userStateManager, favoritesManager)
        OmnitureTestUtils.assertStateTracked("App.Lists.Saved.Hotel",
                OmnitureMatchers.withEventsString("event347=4"),
                mockAnalyticsProvider)
    }

    @Test
    fun testLoggedOut() {
        userStateManager.signOut()
        val t = TestObserver<Unit>()
        viewModel.receivedResponseSubject.subscribe(t)
        t.assertValueCount(0)
        assertFalse(viewModel.shouldShowList())
    }

    @Test
    fun testPageLoadedAnalyticsLoggedOut() {
        userStateManager.signOut()
        viewModel = HotelFavoritesViewModel(context, userStateManager, favoritesManager)
        OmnitureTestUtils.assertStateTracked("App.Lists.Saved.Hotel",
                OmnitureMatchers.withEventsString("event346"),
                mockAnalyticsProvider)
    }

    @Test
    fun testMultipleResultListsCombined() {
        val response = HotelShortlistResponse<HotelShortlistItem>()
        val resultOneItem = HotelShortlistResult<HotelShortlistItem>().apply {
            items = arrayListOf(HotelShortlistItem())
        }
        response.results = arrayListOf(resultOneItem, resultOneItem)
        viewModel.response = response
        assertEquals(viewModel.favoritesList.size, 2)
    }

    @Test
    fun testCreateHotelIntent() {
        val intent = viewModel.createHotelIntent(hotelShortlistItem)!!
        val searchParams = assertIntent(intent)
        assertSearchParams(searchParams!!)
    }

    @Test
    fun testCreateHotelIntentNullHotelId() {
        hotelShortlistItem.shortlistItem!!.metaData!!.hotelId = null
        val intent = viewModel.createHotelIntent(hotelShortlistItem)!!
        val searchParams = assertIntent(intent)
        assertSearchParams(searchParams!!, gaiaId = "itemId", hotelId = "itemId")
    }

    @Test
    fun testCreateHotelIntentBlankHotelId() {
        hotelShortlistItem.shortlistItem!!.metaData!!.hotelId = ""
        val intent = viewModel.createHotelIntent(hotelShortlistItem)!!
        val searchParams = assertIntent(intent)
        assertSearchParams(searchParams!!, gaiaId = "itemId", hotelId = "itemId")
    }

    @Test
    fun testCreateHotelIntentNullHotelIdAndNullItemId() {
        hotelShortlistItem.shortlistItem!!.metaData!!.hotelId = null
        hotelShortlistItem.shortlistItem!!.itemId = null
        val intent = viewModel.createHotelIntent(hotelShortlistItem)
        assertNull(intent)
    }

    @Test
    fun testCreateHotelIntentNullHotelIdAndBlankItemId() {
        hotelShortlistItem.shortlistItem!!.metaData!!.hotelId = null
        hotelShortlistItem.shortlistItem!!.itemId = ""
        val intent = viewModel.createHotelIntent(hotelShortlistItem)
        assertNull(intent)
    }

    @Test
    fun testCreateHotelIntentInvalidAdultsRoomConfiguration() {
        hotelShortlistItem.shortlistItem!!.metaData!!.roomConfiguration = "x|0-100"
        val intent = viewModel.createHotelIntent(hotelShortlistItem)!!
        val searchParams = assertIntent(intent)
        assertSearchParams(searchParams!!, adults = 1, children = listOf(0, 100))
    }

    @Test
    fun testCreateHotelIntentInvalidChildrenRoomConfiguration() {
        hotelShortlistItem.shortlistItem!!.metaData!!.roomConfiguration = "999|x-x"
        val intent = viewModel.createHotelIntent(hotelShortlistItem)!!
        val searchParams = assertIntent(intent)
        assertSearchParams(searchParams!!, adults = 999, children = emptyList())
    }

    @Test
    fun testCreateHotelIntentShopWithPoints() {
        viewModel.useShopWithPoints = true
        val intent = viewModel.createHotelIntent(hotelShortlistItem)!!
        val searchParams = assertIntent(intent)
        assertSearchParams(searchParams!!, shopWithPoints = true)
    }

    @Test
    fun testCreateHotelIntentNotShopWithPoints() {
        viewModel.useShopWithPoints = false
        val intent = viewModel.createHotelIntent(hotelShortlistItem)!!
        val searchParams = assertIntent(intent)
        assertSearchParams(searchParams!!)
    }

    @Test
    fun testRemoveHotel() {
        viewModel.favoritesList.add(createHotelShortlistItem("hotel1"))
        viewModel.favoritesList.add(createHotelShortlistItem("hotel2"))
        HotelFavoritesCache.saveFavorites(context, setOf("hotel1", "hotel2"))
        val testRemoveObserver = TestObserver<Int>()
        val testEmptyObserver = TestObserver<Unit>()
        viewModel.favoriteRemovedAtIndexSubject.subscribe(testRemoveObserver)
        viewModel.favoritesEmptySubject.subscribe(testEmptyObserver)
        viewModel.removeFavoriteHotelAtIndex(0)
        viewModel.removeFavoriteHotelAtIndex(0)
        testRemoveObserver.assertValueCount(2)
        testEmptyObserver.assertValueCount(1)
    }

    @Test
    fun testRemoveHotelOutOfBounds() {
        val testRemoveObserver = TestObserver<Int>()
        val testEmptyObserver = TestObserver<Unit>()
        viewModel.favoriteRemovedAtIndexSubject.subscribe(testRemoveObserver)
        viewModel.favoritesEmptySubject.subscribe(testEmptyObserver)
        viewModel.removeFavoriteHotelAtIndex(0)
        testRemoveObserver.assertValueCount(0)
        testEmptyObserver.assertValueCount(0)
    }

    @Test
    fun testRemoveHotelBadId() {
        val nullIdShortlistItem = createHotelShortlistItem()
        nullIdShortlistItem.shortlistItem!!.metaData!!.hotelId = null
        nullIdShortlistItem.shortlistItem!!.itemId = null
        viewModel.favoritesList.add(nullIdShortlistItem)

        val emptyIdShortlistItem = createHotelShortlistItem()
        emptyIdShortlistItem.shortlistItem!!.metaData!!.hotelId = ""
        emptyIdShortlistItem.shortlistItem!!.itemId = ""

        val testRemoveObserver = TestObserver<Int>()
        val testEmptyObserver = TestObserver<Unit>()
        viewModel.favoriteRemovedAtIndexSubject.subscribe(testRemoveObserver)
        viewModel.favoritesEmptySubject.subscribe(testEmptyObserver)
        viewModel.removeFavoriteHotelAtIndex(0)

        testRemoveObserver.assertValueCount(0)
        testEmptyObserver.assertValueCount(0)
    }

    @Test
    fun testUndoRemove() {
        viewModel.favoritesList.add(hotelShortlistItem)
        val testAddObserver = TestObserver<Int>()
        viewModel.favoriteAddedAtIndexSubject.subscribe(testAddObserver)
        viewModel.removeFavoriteHotelAtIndex(0)
        assertEquals(viewModel.favoritesList.size, 0)
        viewModel.undoLastRemove()
        testAddObserver.assertValue(0)
        assertEquals(viewModel.favoritesList.size, 1)

        shortlistServicesRule.server.takeRequest().path.contains("save/hotelId")
        shortlistServicesRule.server.takeRequest().path.contains("remove/hotelId")
    }

    @Test
    fun testUndoBadIndex() {
        viewModel.favoritesList.add(hotelShortlistItem)
        viewModel.favoritesList.add(hotelShortlistItem)
        val testAddObserver = TestObserver<Int>()
        viewModel.favoriteAddedAtIndexSubject.subscribe(testAddObserver)

        viewModel.undoLastRemove()
        assertEquals(viewModel.favoritesList.size, 2)
        testAddObserver.assertValueCount(0)

        viewModel.removeFavoriteHotelAtIndex(1)
        viewModel.favoritesList.clear()
        viewModel.undoLastRemove()
        testAddObserver.assertValueCount(0)
    }

    @Test
    fun testFavoriteRemovedFromCacheSubject() {
        val testObserver = TestObserver<Unit>()
        viewModel.favoriteRemovedFromCacheSubject.subscribe(testObserver)

        viewModel.favoritesList.add(createHotelShortlistItem())

        HotelFavoritesCache.cacheChangedSubject.onNext(emptySet())

        assertTrue(viewModel.favoritesList.isEmpty())
        testObserver.assertValueCount(1)
    }

    @Test
    fun testFavoriteRemovedFromCacheSubjectEmptyList() {
        val testObserver = TestObserver<Unit>()
        viewModel.favoriteRemovedFromCacheSubject.subscribe(testObserver)

        HotelFavoritesCache.cacheChangedSubject.onNext(emptySet())

        assertTrue(viewModel.favoritesList.isEmpty())
        testObserver.assertValueCount(0)
    }

    @Test
    fun testFavoriteRemovedFromCacheSubjectFavoriteAdded() {
        val testObserver = TestObserver<Unit>()
        viewModel.favoriteRemovedFromCacheSubject.subscribe(testObserver)

        HotelFavoritesCache.cacheChangedSubject.onNext(setOf("hot"))

        assertTrue(viewModel.favoritesList.isEmpty())
        testObserver.assertValueCount(0)
    }

    @Test
    fun testFavoriteRemovedFromCacheSubjectNothingRemoved() {
        val testObserver = TestObserver<Unit>()
        viewModel.favoriteRemovedFromCacheSubject.subscribe(testObserver)

        val hotelShortlistItem = createHotelShortlistItem()
        viewModel.favoritesList.add(hotelShortlistItem)

        HotelFavoritesCache.cacheChangedSubject.onNext(setOf(hotelShortlistItem.getHotelId()!!))

        assertEquals(1, viewModel.favoritesList.count())
        assertEquals("hotelId", viewModel.favoritesList[0].getHotelId())
        testObserver.assertValueCount(0)
    }

    @Test
    fun testFavoriteRemovedFromCacheSubjectMultipleFavorites() {
        val testObserver = TestObserver<Unit>()
        viewModel.favoriteRemovedFromCacheSubject.subscribe(testObserver)

        viewModel.favoritesList.add(createHotelShortlistItem("hotel1"))
        viewModel.favoritesList.add(createHotelShortlistItem("hotel2"))
        viewModel.favoritesList.add(createHotelShortlistItem("hotel3"))

        HotelFavoritesCache.cacheChangedSubject.onNext(setOf("hotel2"))

        assertEquals(1, viewModel.favoritesList.count())
        assertEquals("hotel2", viewModel.favoritesList[0].getHotelId())
        testObserver.assertValueCount(1)
    }

    @Test
    fun testFavoriteRemovedFromCacheSubjectNullHotelId() {
        val testObserver = TestObserver<Unit>()
        viewModel.favoriteRemovedFromCacheSubject.subscribe(testObserver)

        viewModel.favoritesList.add(createHotelShortlistItem(null, null))
        viewModel.favoritesList.add(createHotelShortlistItem(null, "hotel"))
        viewModel.favoritesList.add(createHotelShortlistItem("hotel", null))

        HotelFavoritesCache.cacheChangedSubject.onNext(setOf("hotel"))

        assertEquals(3, viewModel.favoritesList.count())
        assertNull(viewModel.favoritesList[0].getHotelId())
        assertEquals("hotel", viewModel.favoritesList[1].getHotelId())
        assertEquals("hotel", viewModel.favoritesList[2].getHotelId())
        testObserver.assertValueCount(0)
    }

    @Test
    fun testFavoriteRemovedFromCacheSubjectNoIdMatch() {
        val testObserver = TestObserver<Unit>()
        viewModel.favoriteRemovedFromCacheSubject.subscribe(testObserver)

        viewModel.favoritesList.add(createHotelShortlistItem("hotel5"))
        viewModel.favoritesList.add(createHotelShortlistItem("hotel4"))

        HotelFavoritesCache.cacheChangedSubject.onNext(setOf("hotelId3", "hotelId2", "hotelId1", "hotelId0"))

        assertTrue(viewModel.favoritesList.isEmpty())
        testObserver.assertValueCount(1)
    }

    @Test
    fun testRemoveAnalytics() {
        viewModel.favoritesList.add(hotelShortlistItem)
        viewModel.removeFavoriteHotelAtIndex(0)
        OmnitureTestUtils.assertStateTracked("App.Lists.Saved.Hotel",
                Matchers.allOf(
                        OmnitureMatchers.withEventsString("event149"),
                        OmnitureMatchers.withProductsString(";Hotel:hotelId;;"),
                        OmnitureMatchers.withProps(mapOf(16 to "SP.Shortlist.0.HOTEL.UNFAV")),
                        OmnitureMatchers.withEvars(mapOf(28 to "SP.Shortlist.0.HOTEL.UNFAV"))
                ),
                mockAnalyticsProvider)
    }

    @Test
    fun testUndoAnalytics() {
        viewModel.favoritesList.add(hotelShortlistItem)
        viewModel.removeFavoriteHotelAtIndex(0)
        viewModel.undoLastRemove()
        OmnitureTestUtils.assertStateTracked("App.Lists.Saved.Hotel",
                Matchers.allOf(
                        OmnitureMatchers.withEventsString("event148"),
                        OmnitureMatchers.withProductsString(";Hotel:hotelId;;"),
                        OmnitureMatchers.withProps(mapOf(16 to "SP.Shortlist.0.HOTEL.FAV")),
                        OmnitureMatchers.withEvars(mapOf(28 to "SP.Shortlist.0.HOTEL.FAV"))
                ),
                mockAnalyticsProvider)
    }

    private fun createHotelShortlistItem(hotelId: String? = "hotelId", itemId: String? = "itemId"): HotelShortlistItem {
        return HotelShortlistItem().apply {
            regionId = "regionId"
            shortlistItem = ShortlistItem().apply {
                this.itemId = itemId
                metaData = ShortlistItemMetadata().apply {
                    this.hotelId = hotelId
                    chkIn = tomorrow.toString("yyyyMMdd")
                    chkOut = twoDaysAhead.toString("yyyyMMdd")
                    roomConfiguration = "1|3-3-7"
                }
            }
        }
    }

    private fun assertIntent(intent: Intent): HotelSearchParams? {
        assertTrue(intent.getBooleanExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS, false))
        assertNull(intent.getStringExtra(HotelExtras.LANDING_PAGE))
        val searchParams = HotelsV2DataUtil.getHotelV2SearchParamsFromJSON(intent.getStringExtra(HotelExtras.EXTRA_HOTEL_SEARCH_PARAMS))!!
        assertTrue(intent.getBooleanExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS, false))
        assertEquals(HotelActivity::class.java.name, intent.component.className)
        assertTrue(intent.getBooleanExtra(Codes.INFOSITE_DEEPLINK_DONT_BACK_TO_SEARCH, false))
        assertTrue(intent.getBooleanExtra(Codes.KEEP_HOTEL_MODULE_ON_DESTROY, false))

        return searchParams
    }

    private fun assertSearchParams(searchParams: HotelSearchParams,
                                   gaiaId: String = "hotelId", hotelId: String = "hotelId",
                                   checkIn: LocalDate = tomorrow, checkOut: LocalDate = twoDaysAhead,
                                   adults: Int = 1, children: List<Int> = listOf(3, 3, 7),
                                   shopWithPoints: Boolean = false, isDatelessSearch: Boolean = false) {
        assertSuggestion(gaiaId, hotelId, searchParams.suggestion)
        assertEquals(checkIn, searchParams.checkIn)
        assertEquals(checkOut, searchParams.checkOut)
        assertEquals(adults, searchParams.adults)
        assertEquals(children, searchParams.children)
        assertEquals(shopWithPoints, searchParams.shopWithPoints)
        assertEquals(isDatelessSearch, searchParams.isDatelessSearch)
        assertFalse(searchParams.updateSearchDestination)
    }

    private fun assertSuggestion(gaiaId: String, hotelId: String, suggestion: SuggestionV4) {
        assertEquals(gaiaId, suggestion.gaiaId)
        assertNull(suggestion.regionNames.fullName)
        assertEquals("Hotel $hotelId", suggestion.regionNames.displayName)
        assertEquals("Hotel $hotelId", suggestion.regionNames.shortName)
        assertNull(suggestion.regionNames.lastSearchName)
        assertEquals(hotelId, suggestion.hotelId)
        assertEquals(0.0, suggestion.coordinates.lat)
        assertEquals(0.0, suggestion.coordinates.lng)
    }

    private fun signInUserWithLoyalty() {
        val loyaltyInfo = UserLoyaltyMembershipInformation()
        loyaltyInfo.isAllowedToShopWithPoints = true
        val user = UserLoginTestUtil.mockUser()
        user.loyaltyMembershipInformation = loyaltyInfo

        setupUserAndMockLogin(user, userStateManager)
    }
}
