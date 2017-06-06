package com.expedia.bookings.tracking

import android.content.Context
import com.expedia.bookings.ADMS_Measurement
import com.expedia.bookings.hotel.tracking.SuggestionTrackingData
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.DebugInfoUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

// TODO re-enable these tests when implementing methods in the new Omniture SDK implementation
@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
class OmnitureTrackingTest {

//    private val USER_EMAIL = "testuser@expedia.com"
//    private val USER_EMAIL_HASH = "1941c6bff303b2fb1af6801a7eb809e657bc611e8e2d76c44961b90aec193f5a"
//
//    private lateinit var context: Context;
//    private lateinit var adms: ADMS_Measurement;
//
//    private val suggestionBehaviorLinkDefaults = "HTL.UpdateSearch.H.en_US.1"
//
//    @Before
//    fun setup() {
//        context = RuntimeEnvironment.application
//        adms = ADMS_Measurement.sharedInstance(context)
//    }
//
//    @Test
//    fun guidSentInProp23() {
//        OmnitureTracking.trackAccountPageLoad()
//
//        val expectedGuid = DebugInfoUtils.getMC1CookieStr(context).replace("GUID=", "")
//        assertEquals(expectedGuid, adms.getProp(23))
//    }
//
//    @Test
//    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
//    fun emailHashedWithSHA256() {
//        givenUserIsSignedIn()
//
//        OmnitureTracking.trackAccountPageLoad()
//
//        assertEquals(USER_EMAIL_HASH, adms.getProp(11))
//    }
//
//    @Test
//    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
//    fun testHotelSuggestionEvar48Prop73_withChild() {
//        val dataBuilder = TestTrackingDataBuilder()
//        dataBuilder.shown(false).selected(false).child(true).gaiaId("178286").suggestionType("MULTICITY").displayName("Miami, Florida")
//        val data = dataBuilder.build()
//
//        val expectedChild = "GAI:${data.suggestionGaiaId}|${data.suggestionType}|R#child|${data.displayName}"
//
//        OmnitureTracking.trackHotelSuggestionBehavior(data)
//        assertEquals(expectedChild, adms.getProp(73))
//        assertEquals(expectedChild, adms.getEvar(48))
//    }
//
//    @Test
//    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
//    fun testHotelSuggestionEvar48Prop73_noChild() {
//        val dataBuilder = TestTrackingDataBuilder()
//        dataBuilder.shown(false).selected(false).child(false).gaiaId("178286").suggestionType("MULTICITY").displayName("Miami, Florida")
//        val data = dataBuilder.build()
//
//        val expectedNoChild = "GAI:${data.suggestionGaiaId}|${data.suggestionType}|R#-|${data.displayName}"
//
//        OmnitureTracking.trackHotelSuggestionBehavior(data)
//        assertEquals(expectedNoChild, adms.getProp(73))
//        assertEquals(expectedNoChild, adms.getEvar(48))
//    }
//
//    @Test
//    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
//    fun testHotelSuggestionEvar48Prop73_userHistory() {
//        val dataBuilder = TestTrackingDataBuilder()
//        dataBuilder.shown(false).selected(false).history(true).gaiaId("178286").suggestionType("MULTICITY").displayName("Miami, Florida")
//        val data = dataBuilder.build()
//
//        val expectedText = "GAI:${data.suggestionGaiaId}|${data.suggestionType}|R#-|${data.displayName}|USERHISTORY"
//
//        OmnitureTracking.trackHotelSuggestionBehavior(data)
//        assertEquals(expectedText, adms.getProp(73))
//        assertEquals(expectedText, adms.getEvar(48))
//    }
//
//    @Test
//    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
//    fun testHotelSuggestionEvents() {
//        val dataBuilder = TestTrackingDataBuilder()
//        dataBuilder.shown(false).selected(false).charTyped(4).selectedPosition(2)
//        val data = dataBuilder.build()
//
//        val expectedEventText = "event45,event44=${data.selectedSuggestionPosition},event46=${data.charactersTypedCount}"
//
//        OmnitureTracking.trackHotelSuggestionBehavior(data)
//        assertEquals(expectedEventText, adms.events)
//
//        data.charactersTypedCount = 0
//        val expectedNoCharText = "event45,event44=${data.selectedSuggestionPosition},event46=0"
//        OmnitureTracking.trackHotelSuggestionBehavior(data)
//        assertEquals(expectedNoCharText, adms.events)
//    }
//
//    @Test
//    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
//    fun testHotelSuggestionLinkEvar28Prop16_noShowPreviousSearch() {
//        val dataBuilder = TestTrackingDataBuilder()
//        dataBuilder.shown(false).selected(false).charTyped(0).gaiaId("12356")
//        val data = dataBuilder.build()
//
//        val expectedText = "$suggestionBehaviorLinkDefaults.TANoShow.TAPrevSearch.P0C0L0.ESS#0.UH#0"
//
//        OmnitureTracking.trackHotelSuggestionBehavior(data)
//        assertEquals(expectedText, adms.getProp(16), "FAILURE: If User did not focus or select but GAIA is present " +
//                "assume previous search")
//        assertEquals(expectedText, adms.getEvar(28), "FAILURE: If User did not focus or select but GAIA is present " +
//                "assume previous search")
//    }
//
//    @Test
//    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
//    fun testHotelSuggestionLinkEvar28Prop16_showPreviousSearch() {
//        val dataBuilder = TestTrackingDataBuilder()
//        dataBuilder.shown(true).selected(false).charTyped(3).gaiaId("12356")
//        val data = dataBuilder.build()
//
//        val expectedText = "$suggestionBehaviorLinkDefaults.TAShow.TAPrevSearch.P0C0L0.ESS#0.UH#0"
//
//        OmnitureTracking.trackHotelSuggestionBehavior(data)
//        assertEquals(expectedText, adms.getProp(16), "FAILURE: If User focus but no select and GAIA is present" +
//                " assume previous search")
//        assertEquals(expectedText, adms.getEvar(28), "FAILURE: If User focus but no select and GAIA is present" +
//                " assume previous search")
//    }
//
//    @Test
//    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
//    fun testHotelSuggestionLinkEvar28Prop16_showTypeSelection() {
//        val dataBuilder = TestTrackingDataBuilder()
//        dataBuilder.shown(true).selected(true).charTyped(3).gaiaId("12356")
//        val data = dataBuilder.build()
//
//        val expectedText = "$suggestionBehaviorLinkDefaults.TAShow.TASelection.P0C0L0.ESS#0.UH#0"
//
//        OmnitureTracking.trackHotelSuggestionBehavior(data)
//        assertEquals(expectedText, adms.getProp(16), "FAILURE: If User focus and select and has typed something," +
//                " behavior==TAShow.TASelection")
//        assertEquals(expectedText, adms.getEvar(28), "FAILURE: If User focus and select and has typed something," +
//                " behavior==TAShow.TASelection")
//    }
//
//    @Test
//    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
//    fun testHotelSuggestionLinkEvar28Prop16_showNoTypeFocus() {
//        val dataBuilder = TestTrackingDataBuilder()
//        dataBuilder.shown(true).selected(true).charTyped(0).gaiaId("12356")
//        val data = dataBuilder.build()
//
//        val focusExpectedText = "$suggestionBehaviorLinkDefaults.TAShow.TAFocus.P0C0L0.ESS#0.UH#0"
//
//        OmnitureTracking.trackHotelSuggestionBehavior(data)
//        assertEquals(focusExpectedText, adms.getProp(16), "FAILURE: If User focus and select but has not typed anything," +
//                " behavior==TAShow.TAFocus")
//        assertEquals(focusExpectedText, adms.getEvar(28), "FAILURE: If User focus and select but has not typed anything," +
//                " behavior==TAShow.TAFocus")
//    }
//
//    @Test
//    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
//    fun testHotelSuggestionLinkEvar28Prop16_depth() {
//        val dataBuilder = TestTrackingDataBuilder()
//        dataBuilder.shown(true).selected(true).essCount(10).charTyped(3).selectedPosition(4)
//        val data = dataBuilder.build()
//
//        val expectedText = "$suggestionBehaviorLinkDefaults.TAShow.TASelection.P0C0L4.ESS#10.UH#0"
//
//        OmnitureTracking.trackHotelSuggestionBehavior(data)
//        assertEquals(expectedText, adms.getProp(16), "FAILURE: L# should represent depth")
//        assertEquals(expectedText, adms.getEvar(28), "FAILURE: L# should represent depth")
//    }
//
//    @Test
//    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
//    fun testHotelSuggestionLinkEvar28Prop16_isParentFalse() {
//        val dataBuilder = TestTrackingDataBuilder()
//        dataBuilder.shown(true).selected(true).parent(false).essCount(10).charTyped(3).selectedPosition(0)
//        val data = dataBuilder.build()
//
//        val noParentText = "$suggestionBehaviorLinkDefaults.TAShow.TASelection.P0C0L0.ESS#10.UH#0"
//
//        OmnitureTracking.trackHotelSuggestionBehavior(data)
//        assertEquals(noParentText, adms.getProp(16), "FAILURE: P0 should represent isParent=false")
//        assertEquals(noParentText, adms.getEvar(28), "FAILURE: P0 should represent isParent=false")
//    }
//
//    @Test
//    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
//    fun testHotelSuggestionLinkEvar28Prop16_isParentTrue() {
//        val dataBuilder = TestTrackingDataBuilder()
//        dataBuilder.shown(true).selected(true).parent(true).essCount(10).charTyped(3).selectedPosition(0)
//        val data = dataBuilder.build()
//
//        val parentText = "$suggestionBehaviorLinkDefaults.TAShow.TASelection.P1C0L0.ESS#10.UH#0"
//
//        OmnitureTracking.trackHotelSuggestionBehavior(data)
//        assertEquals(parentText, adms.getProp(16), "FAILURE: P1 should represent isParent=true")
//        assertEquals(parentText, adms.getEvar(28), "FAILURE: P1 should represent isParent=true")
//    }
//
//    @Test
//    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
//    fun testHotelSuggestionLinkEvar28Prop16_isChildFalse() {
//        val dataBuilder = TestTrackingDataBuilder()
//        dataBuilder.shown(true).selected(true).child(false).essCount(10).charTyped(3).selectedPosition(0)
//        val data = dataBuilder.build()
//
//        val noChildText = "$suggestionBehaviorLinkDefaults.TAShow.TASelection.P0C0L0.ESS#10.UH#0"
//
//        OmnitureTracking.trackHotelSuggestionBehavior(data)
//        assertEquals(noChildText, adms.getProp(16), "FAILURE: C0 should represent isChild=false")
//        assertEquals(noChildText, adms.getEvar(28), "FAILURE: C0 should represent isChild=false")
//    }
//
//    @Test
//    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
//    fun testHotelSuggestionLinkEvar28Prop16_isChildTrue() {
//        val dataBuilder = TestTrackingDataBuilder()
//        dataBuilder.shown(true).selected(true).child(true).essCount(10).charTyped(3).selectedPosition(0)
//        val data = dataBuilder.build()
//
//        val childText = "$suggestionBehaviorLinkDefaults.TAShow.TASelection.P0C1L0.ESS#10.UH#0"
//
//        OmnitureTracking.trackHotelSuggestionBehavior(data)
//        assertEquals(childText, adms.getProp(16), "FAILURE: C1 should represent isChild=true")
//        assertEquals(childText, adms.getEvar(28), "FAILURE: C1 should represent isChild=true")
//    }
//
//    @Test
//    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
//    fun testHotelSuggestionLinkEvar28Prop16_withRecents() {
//        val dataBuilder = TestTrackingDataBuilder()
//        dataBuilder.shown(true).selected(true).essCount(10).historyShownCount(3).charTyped(3)
//        val data = dataBuilder.build()
//
//        val essCount = data.suggestionsShownCount - data.previousSuggestionsShownCount
//        val expectedText = "$suggestionBehaviorLinkDefaults.TAShow.TASelection.P0C0L0.ESS#$essCount.UH#${data.previousSuggestionsShownCount}"
//
//        OmnitureTracking.trackHotelSuggestionBehavior(data)
//        assertEquals(expectedText, adms.getProp(16), "FAILURE: UH# should represent number of previous suggestions shown")
//        assertEquals(expectedText, adms.getEvar(28), "FAILURE: UH# should represent number of previous suggestions shown")
//    }
//
//    @Test
//    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
//    fun testHotelSuggestionLinkEvar28Prop16_subtractRecentsFromESS() {
//        val dataBuilder = TestTrackingDataBuilder()
//        dataBuilder.shown(true).selected(true).essCount(10).historyShownCount(3).charTyped(3)
//        val data = dataBuilder.build()
//
//        val essCount = data.suggestionsShownCount - data.previousSuggestionsShownCount
//        val expectedText = "$suggestionBehaviorLinkDefaults.TAShow.TASelection.P0C0L0.ESS#$essCount.UH#${data.previousSuggestionsShownCount}"
//
//        OmnitureTracking.trackHotelSuggestionBehavior(data)
//        assertEquals(expectedText, adms.getProp(16), "FAILURE: ESS# should not include previous user history item counts")
//        assertEquals(expectedText, adms.getEvar(28), "FAILURE: ESS# should not include previous user history item counts")
//    }
//
//    @Test
//    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
//    fun testHotelSuggestionLinkEvar28Prop16_historyCantBeChild() {
//        val dataBuilder = TestTrackingDataBuilder()
//        dataBuilder.shown(true).selected(true).child(true).history(true).essCount(10).charTyped(3).selectedPosition(0)
//        val data = dataBuilder.build()
//
//        val childText = "$suggestionBehaviorLinkDefaults.TAShow.TASelection.P0C0L0.ESS#10.UH#0"
//
//        OmnitureTracking.trackHotelSuggestionBehavior(data)
//        assertEquals(childText, adms.getProp(16), "FAILURE: If an item is from user history it can't be a child")
//        assertEquals(childText, adms.getEvar(28), "FAILURE: If an item is from user history it can't be a child")
//    }
//
//    @Test
//    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
//    fun testHotelSuggestionLinkEvar28Prop16_historyCantBeParent() {
//        val dataBuilder = TestTrackingDataBuilder()
//        dataBuilder.shown(true).selected(true).parent(true).history(true).essCount(10).charTyped(3).selectedPosition(0)
//        val data = dataBuilder.build()
//
//        val noParentText = "$suggestionBehaviorLinkDefaults.TAShow.TASelection.P0C0L0.ESS#10.UH#0"
//
//        OmnitureTracking.trackHotelSuggestionBehavior(data)
//        assertEquals(noParentText, adms.getProp(16), "FAILURE: If an item is from user history it can't be a parent")
//        assertEquals(noParentText, adms.getEvar(28), "FAILURE: If an item is from user history it can't be a parent")
//    }
//
//    private fun givenUserIsSignedIn() {
//        val user = UserLoginTestUtil.mockUser()
//        user.primaryTraveler.email = USER_EMAIL
//        UserLoginTestUtil.setupUserAndMockLogin(user)
//    }
//
//    private class TestTrackingDataBuilder {
//        private val data = SuggestionTrackingData()
//
//        fun shown(show: Boolean) : TestTrackingDataBuilder{
//            data.suggestionsFocused = show
//            return this
//        }
//
//        fun selected(selected: Boolean) : TestTrackingDataBuilder {
//            data.suggestionSelected = selected
//            return this
//        }
//
//        fun selectedPosition(position: Int) : TestTrackingDataBuilder {
//            data.selectedSuggestionPosition = position
//            return this
//        }
//
//        fun gaiaId(id: String) : TestTrackingDataBuilder {
//            data.suggestionGaiaId = id
//            return this
//        }
//
//        fun essCount(count: Int) : TestTrackingDataBuilder {
//            data.suggestionsShownCount = count
//            return this
//        }
//
//        fun historyShownCount(count: Int) : TestTrackingDataBuilder {
//            data.previousSuggestionsShownCount = count
//            return this
//        }
//
//        fun charTyped(count: Int) : TestTrackingDataBuilder {
//            data.charactersTypedCount = count
//            return this
//        }
//
//        fun displayName(name: String) : TestTrackingDataBuilder {
//            data.displayName = name
//            return this
//        }
//
//        fun suggestionType(type: String) : TestTrackingDataBuilder {
//            data.suggestionType = type
//            return this
//        }
//
//        fun child(child: Boolean) : TestTrackingDataBuilder {
//            data.isChild = child
//            return this
//        }
//
//        fun history(history: Boolean) : TestTrackingDataBuilder {
//            data.isHistory = history
//            return this
//        }
//
//        fun parent(parent: Boolean) : TestTrackingDataBuilder {
//            data.isParent = parent
//            return this
//        }
//
//        fun build() : SuggestionTrackingData {
//            return data
//        }
//    }
}