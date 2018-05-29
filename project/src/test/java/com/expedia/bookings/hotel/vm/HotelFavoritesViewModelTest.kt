package com.expedia.bookings.hotel.vm

import android.content.Intent
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
import com.expedia.bookings.services.HotelShortlistServices
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.UserLoginTestUtil.Companion.setupUserAndMockLogin
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.HotelsV2DataUtil
import com.expedia.ui.HotelActivity
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
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
class HotelFavoritesViewModelTest {
    var shortlistServicesRule = ServicesRule(HotelShortlistServices::class.java)
        @Rule get

    private val context = RuntimeEnvironment.application
    private val userStateManager = UserLoginTestUtil.getUserStateManager()

    private lateinit var viewModel: HotelFavoritesViewModel
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

        viewModel = HotelFavoritesViewModel(context, userStateManager, shortlistServicesRule.services!!)
        hotelShortlistItem = createHotelShortlistItem()
    }

    @Test
    fun testLoggedIn() {
        signInUserWithLoyalty()
        val viewModel = HotelFavoritesViewModel(context, userStateManager, shortlistServicesRule.services!!)
        val t = TestObserver<Unit>()
        viewModel.receivedResponseSubject.subscribe(t)
        t.assertValueCount(1)
        assertTrue(viewModel.shouldShowList())

        assertNotEquals(0, viewModel.compositeDisposable.size())
        viewModel.onClear()
        assertEquals(0, viewModel.compositeDisposable.size())
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

    private fun createHotelShortlistItem(): HotelShortlistItem {
        return HotelShortlistItem().apply {
            regionId = "regionId"
            shortlistItem = ShortlistItem().apply {
                itemId = "itemId"
                metaData = ShortlistItemMetadata().apply {
                    hotelId = "hotelId"
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
