package com.expedia.bookings.test.robolectric

import android.content.Context
import android.support.v4.content.ContextCompat
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotel.ValueAddsEnum
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.payment.LoyaltyEarnInfo
import com.expedia.bookings.data.payment.LoyaltyInformation
import com.expedia.bookings.data.payment.PointsEarnInfo
import com.expedia.bookings.data.payment.PriceEarnInfo
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RoboTestHelper.getContext
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.testutils.JSONResourceReader
import com.expedia.util.LoyaltyUtil
import com.expedia.vm.HotelRoomDetailViewModel
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.util.ArrayList
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
class HotelRoomDetailViewModelTest {

    private val context: Context = RuntimeEnvironment.application

    @Test
    fun testBreakfastValueAddsToShow() {
        val ids = intArrayOf(512, 2, 8, 4, 8192, 4096, 16777216, 33554432, 67108864, 1073742786, 1073742857, 2111, 2085, 4363,
                2102, 2207, 2206, 2194, 2193, 2205, 2103, 2105, 2104, 3969, 4647, 4646, 4648, 4649, 4650, 4651, 2001).asList()
        testValueAddsToShow(ids, ValueAddsEnum.BREAKFAST)
    }

    @Test
    fun testInternetValueAddsToShow() {
        val ids = intArrayOf(2048, 1024, 1073742787, 4347, 2403, 4345, 2405, 2407, 4154, 2191, 2192, 2404, 2406).asList()
        testValueAddsToShow(ids, ValueAddsEnum.INTERNET)
    }

    @Test
    fun testParkingValueAddsToShow() {
        val ids = intArrayOf(16384, 128, 2195, 2109, 4449, 4447, 4445, 4443, 3863, 3861, 2011).asList()
        testValueAddsToShow(ids, ValueAddsEnum.PARKING)
    }

    @Test
    fun testAirportShuttleValueAddsToShow() {
        val ids = intArrayOf(2196, 32768, 10).sorted()
        testValueAddsToShow(ids, ValueAddsEnum.FREE_AIRPORT_SHUTTLE)
    }

    @Test
    fun testInvalidValueAddsToShow() {
        val ids = intArrayOf(946, 42, 420, 69, 34, 404, 7242728).toList()

        val roomResponse = createRoomResponse(ids)
        val vm = createViewModel(roomResponse, 0)
        val toShow = vm.getValueAdds()

        assertEquals(0, toShow.count())
    }

    @Test
    fun testValueAddsToShowPriority() {
        val ids = intArrayOf(128, 1024, 2, 2196).toList()

        val roomResponse = createRoomResponse(ids)
        val vm = createViewModel(roomResponse, 0)
        val toShow = vm.getValueAdds()

        assertEquals(ids.count(), toShow.count())

        assertEquals(ValueAddsEnum.INTERNET, toShow[0].valueAddsEnum)
        assertEquals(ValueAddsEnum.BREAKFAST, toShow[1].valueAddsEnum)
        assertEquals(ValueAddsEnum.PARKING, toShow[2].valueAddsEnum)
        assertEquals(ValueAddsEnum.FREE_AIRPORT_SHUTTLE, toShow[3].valueAddsEnum)
    }

    @Test
    fun testRoomPriceContentDescription() {
        var roomResponse = createRoomResponse()
        roomResponse.rateInfo.chargeableRateInfo.strikethroughPriceToShowUsers = 200f

        var viewModel = createViewModel(roomResponse, -1)

        val expectedStrikeThrough = Money(200, "USD").getFormattedMoney(Money.F_ALWAYS_TWO_PLACES_AFTER_DECIMAL)
        val expectedPrice = Money(109, "USD").getFormattedMoney(Money.F_ALWAYS_TWO_PLACES_AFTER_DECIMAL)

        assertEquals("Regularly " + expectedStrikeThrough + ", now " + expectedPrice + "/night.\u0020Original price discounted -20%.\u0020", viewModel.getRoomPriceContentDescription())
    }

    @Test
    fun testRoomPriceContentDescriptionDontShowPerNight() {
        var roomResponse = createRoomResponse()
        roomResponse.rateInfo.chargeableRateInfo.strikethroughPriceToShowUsers = 200f
        roomResponse.rateInfo.chargeableRateInfo.userPriceType = "RateForWholeStayWithTaxes"

        var viewModel = createViewModel(roomResponse, 0)

        val expectedStrikeThrough = Money(200, "USD").getFormattedMoney(Money.F_ALWAYS_TWO_PLACES_AFTER_DECIMAL)
        val expectedPrice = Money(109, "USD").getFormattedMoney(Money.F_ALWAYS_TWO_PLACES_AFTER_DECIMAL)

        assertEquals("Regularly " + expectedStrikeThrough + ", now " + expectedPrice + ".\u0020Original price discounted -20%.\u0020", viewModel.getRoomPriceContentDescription())
    }

    @Test
    fun testRoomPriceContentDescriptionNoDiscountPercentage() {
        var roomResponse = createRoomResponse()
        roomResponse.rateInfo.chargeableRateInfo.strikethroughPriceToShowUsers = 200f
        roomResponse.rateInfo.chargeableRateInfo.discountPercent = 0f

        var viewModel = createViewModel(roomResponse, 1)

        val expectedStrikeThrough = Money(200, "USD").getFormattedMoney(Money.F_ALWAYS_TWO_PLACES_AFTER_DECIMAL)
        val expectedPrice = Money(109, "USD").getFormattedMoney(Money.F_ALWAYS_TWO_PLACES_AFTER_DECIMAL)

        assertEquals("Regularly " + expectedStrikeThrough + ", now " + expectedPrice + "/night.\u0020", viewModel.getRoomPriceContentDescription())
    }

    @Test
    fun testRoomPriceContentDescriptionNoStrikeThrough() {
        var roomResponse = createRoomResponse()
        roomResponse.rateInfo.chargeableRateInfo.strikethroughPriceToShowUsers = 0f
        var viewModel = createViewModel(roomResponse, 4)

        val expectedPrice = Money(109, "USD").getFormattedMoney(Money.F_ALWAYS_TWO_PLACES_AFTER_DECIMAL)

        assertEquals(expectedPrice + "/night", viewModel.getRoomPriceContentDescription())
    }

    @Test
    fun testDontShowExactDuplicateValueAdds() {
        val valueAdds = ArrayList<HotelOffersResponse.ValueAdds>()

        var valueAdd = HotelOffersResponse.ValueAdds()
        valueAdd.id = "2"
        valueAdd.description = "1"
        valueAdds.add(valueAdd)

        valueAdd = HotelOffersResponse.ValueAdds()
        valueAdd.id = "2"
        valueAdd.description = "2"
        valueAdds.add(valueAdd)

        valueAdd = HotelOffersResponse.ValueAdds()
        valueAdd.id = "2"
        valueAdd.description = "1"
        valueAdds.add(valueAdd)

        val roomResponse = createRoomResponse(ArrayList())
        roomResponse.valueAdds = valueAdds
        val vm = createViewModel(roomResponse, 0)
        val toShow = vm.getValueAdds()

        assertEquals(2, toShow.count())

        assertEquals(ValueAddsEnum.BREAKFAST, toShow[0].valueAddsEnum)
        assertEquals("1", toShow[0].apiDescription)
        assertEquals(ValueAddsEnum.BREAKFAST, toShow[1].valueAddsEnum)
        assertEquals("2", toShow[1].apiDescription)
    }

    @Test
    fun testGroupedInternetValueAdds() {
        val ids = intArrayOf(2048, 1024, 1073742787, 4347, 2403, 4345, 2405, 2407, 4154, 2191, 2192, 2404, 2406).toList()

        val roomResponse = createRoomResponse(ids)
        val vm = createViewModel(roomResponse, 0)
        val toShow = vm.getValueAdds()

        assertEquals(1, toShow.count())

        assertEquals(ValueAddsEnum.INTERNET, toShow[0].valueAddsEnum)
        assertEquals(context.resources.getString(ValueAddsEnum.INTERNET.descriptionId), toShow[0].apiDescription)
    }

    @Test
    fun testIsPackage() {
        var roomResponse = createRoomResponse()
        var viewModel = createViewModel(roomResponse, 3)
        assertFalse(viewModel.isPackage)

        roomResponse.packageHotelDeltaPrice = Money(10, "USD")
        viewModel = createViewModel(roomResponse, -5)
        assertTrue(viewModel.isPackage)
    }

    @Test
    fun testOptionStringIndexZero() {
        var roomResponse = createRoomResponse()
        var viewModel = createViewModel(roomResponse, -1)
        assertNull(viewModel.optionString)
    }

    @Test
    fun testOptionStringNoRoomDescription() {
        var roomResponse = createRoomResponse()
        roomResponse.roomTypeDescription = null
        var viewModel = createViewModel(roomResponse, 0)

        assertEquals("Option 1", viewModel.optionString.toString())
    }

    @Test
    fun testOptionStringWithRoomDescriptionDetail() {
        var roomResponse = createRoomResponse()
        roomResponse.roomTypeDescription = "Room Description - Some Details"
        var viewModel = createViewModel(roomResponse, 1)

        assertEquals("Option 2  (Some Details)", viewModel.optionString.toString())
    }

    @Test
    fun testOptionStringWithRoomDescriptionNoDetail() {
        var roomResponse = createRoomResponse()
        roomResponse.roomTypeDescription = "Room Description"
        var viewModel = createViewModel(roomResponse, 2)

        assertEquals("Option 3", viewModel.optionString.toString())
    }

    @Test
    fun testCancellationString() {
        var roomResponse = createRoomResponse()
        var viewModel = createViewModel(roomResponse, 7)

        assertEquals("Non-refundable", viewModel.cancellationString)

        roomResponse.hasFreeCancellation = true
        viewModel = createViewModel(roomResponse, 7)

        assertEquals("Free cancellation", viewModel.cancellationString)
    }

    @Test
    fun testFreeCancellationTimeStringNotFreeCancellation() {
        var roomResponse = createRoomResponse()
        var viewModel = createViewModel(roomResponse, -1)
        assertNull(viewModel.cancellationTimeString)
    }

    @Test
    fun testFreeCancellationTimeStringNoCancellationWindow() {
        var roomResponse = createRoomResponse()
        roomResponse.hasFreeCancellation = true
        var viewModel = createViewModel(roomResponse, -1)
        assertNull(viewModel.cancellationTimeString)
    }

    @Test
    fun testFreeCancellationTimeString() {
        var roomResponse = createRoomResponse()
        roomResponse.hasFreeCancellation = true
        roomResponse.freeCancellationWindowDate = "1993-04-10 12:34"
        var viewModel = createViewModel(roomResponse, -1)

        val dateTime = DateUtils.yyyyMMddHHmmToDateTime("1993-04-10 12:34").toLocalDate()
        val cancellationDate = LocaleBasedDateFormatUtils.localDateToEEEMMMd(dateTime)
        assertEquals("before $cancellationDate", viewModel.cancellationTimeString)
    }

    @Test
    fun testNoEarnMessage() {
        val roomResponse = createRoomResponse()
        val viewModel = createViewModel(roomResponse, -1)
        assertNull(viewModel.earnMessageString)
    }

    @Test
    fun testPointEarnMessage() {
        val roomResponse = createRoomResponse()
        val loyaltyInfo = createLoyaltyInformation()
        roomResponse.rateInfo.chargeableRateInfo.loyaltyInfo = loyaltyInfo
        val viewModel = createViewModel(roomResponse, -1)

        val shouldShowEarnMessage = LoyaltyUtil.shouldShowEarnMessage("any string", viewModel.isPackage)
        if (shouldShowEarnMessage) {

            val earnMessage = LoyaltyUtil.getEarnMessagingString(context, viewModel.isPackage, loyaltyInfo.earn, null)
            assertEquals(earnMessage, viewModel.earnMessageString)
        } else {
            assertNull(viewModel.earnMessageString)
        }
    }

    @Test
    fun testPriceEarnMessage() {
        val roomResponse = createRoomResponse()
        val loyaltyInfo = createPriceLoyaltyInformation()
        roomResponse.rateInfo.chargeableRateInfo.loyaltyInfo = loyaltyInfo
        val viewModel = createViewModel(roomResponse, -1)

        val shouldShowEarnMessage = LoyaltyUtil.shouldShowEarnMessage("any string", viewModel.isPackage)
        if (shouldShowEarnMessage) {
            val earnMessage = LoyaltyUtil.getEarnMessagingString(context, viewModel.isPackage, loyaltyInfo.earn, null)
            assertEquals(earnMessage, viewModel.earnMessageString)
        } else {
            assertNull(viewModel.earnMessageString)
        }
    }

    @Test
    fun testPackagePointEarnMessage() {
        val roomResponse = createRoomResponse()
        val loyaltyInfo = createLoyaltyInformation()
        roomResponse.packageHotelDeltaPrice = Money(10, "USD")
        roomResponse.packageLoyaltyInformation = loyaltyInfo
        val viewModel = createViewModel(roomResponse, -1)

        val shouldShowEarnMessage = LoyaltyUtil.shouldShowEarnMessage("any string", viewModel.isPackage)
        if (shouldShowEarnMessage) {
            val earnMessage = LoyaltyUtil.getEarnMessagingString(context, viewModel.isPackage, null, loyaltyInfo.earn)
            assertEquals(earnMessage, viewModel.earnMessageString)
        } else {
            assertNull(viewModel.earnMessageString)
        }
    }

    @Test
    fun testPackagePriceEarnMessage() {
        val roomResponse = createRoomResponse()
        val loyaltyInfo = createPriceLoyaltyInformation()
        roomResponse.packageHotelDeltaPrice = Money(10, "USD")
        roomResponse.packageLoyaltyInformation = loyaltyInfo
        val viewModel = createViewModel(roomResponse, -1)

        val shouldShowEarnMessage = LoyaltyUtil.shouldShowEarnMessage("any string", viewModel.isPackage)
        if (shouldShowEarnMessage) {
            val earnMessage = LoyaltyUtil.getEarnMessagingString(context, viewModel.isPackage, null, loyaltyInfo.earn)
            assertEquals(earnMessage, viewModel.earnMessageString)
        } else {
            assertNull(viewModel.earnMessageString)
        }
    }

    @Test
    fun testZeroMandatoryFeeString() {
        val roomResponse = createRoomResponse()
        roomResponse.rateInfo.chargeableRateInfo.dailyMandatoryFee = 0.0.toFloat()
        val viewModel = createViewModel(roomResponse, 5)

        assertNull(viewModel.mandatoryFeeString)
    }

    @Test
    fun testMandatoryFeeString() {
        val roomResponse = createRoomResponse()
        roomResponse.rateInfo.chargeableRateInfo.dailyMandatoryFee = 10.0.toFloat()
        val viewModel = createViewModel(roomResponse, 9)

        val expectedMoney = Money(10, "USD").formattedMoney
        assertEquals("Excludes $expectedMoney daily resort fee", viewModel.mandatoryFeeString)
    }

    @Test
    fun testPackageMandatoryFeeString() {
        val roomResponse = createRoomResponse()
        roomResponse.packageHotelDeltaPrice = Money(10, "USD")
        roomResponse.rateInfo.chargeableRateInfo.dailyMandatoryFee = 10.0.toFloat()
        val viewModel = createViewModel(roomResponse, 9)

        assertNull(viewModel.mandatoryFeeString)
    }

    @Test
    fun testShowMemberOnlyDealTag() {
        mockSignIn()
        val roomResponse = createRoomResponse()
        roomResponse.isMemberDeal = true
        val viewModel = createViewModel(roomResponse, -1)
        assertTrue(viewModel.showMemberOnlyDealTag)
    }

    @Test
    fun testShowMemberOnlyDealTagNotSignedIn() {
        val roomResponse = createRoomResponse()
        roomResponse.isMemberDeal = true
        val viewModel = createViewModel(roomResponse, -1)
        assertFalse(viewModel.showMemberOnlyDealTag)
    }

    @Test
    fun testShowMemberOnlyDealTagNotMemberDeal() {
        mockSignIn()
        val roomResponse = createRoomResponse()
        val viewModel = createViewModel(roomResponse, -1)
        assertFalse(viewModel.showMemberOnlyDealTag)
    }

    @Test
    fun testZeroDiscountPercentageString() {
        val roomResponse = createRoomResponse()
        roomResponse.rateInfo.chargeableRateInfo.loyaltyInfo = LoyaltyInformation(null, LoyaltyEarnInfo(null, null), false)
        roomResponse.rateInfo.chargeableRateInfo.airAttached = false
        roomResponse.rateInfo.chargeableRateInfo.discountPercent = 0.0.toFloat()
        val viewModel = createViewModel(roomResponse, 3)

        assertNull(viewModel.discountPercentageString)
    }

    @Test
    fun testLoyaltyBurnDiscountPercentageString() {
        val roomResponse = createRoomResponse()
        roomResponse.rateInfo.chargeableRateInfo.loyaltyInfo = LoyaltyInformation(null, LoyaltyEarnInfo(null, null), true)
        roomResponse.rateInfo.chargeableRateInfo.airAttached = false
        roomResponse.rateInfo.chargeableRateInfo.discountPercent = 10.0.toFloat()
        val viewModel = createViewModel(roomResponse, 3)

        assertNull(viewModel.discountPercentageString)
    }

    @Test
    fun testAirAttachDiscountPercentageString() {
        val roomResponse = createRoomResponse()
        roomResponse.rateInfo.chargeableRateInfo.loyaltyInfo = LoyaltyInformation(null, LoyaltyEarnInfo(null, null), false)
        roomResponse.rateInfo.chargeableRateInfo.airAttached = true
        roomResponse.rateInfo.chargeableRateInfo.discountPercent = 10.0.toFloat()
        val viewModel = createViewModel(roomResponse, 3)

        assertNull(viewModel.discountPercentageString)
    }

    @Test
    fun testDiscountPercentageString() {
        val roomResponse = createRoomResponse()
        roomResponse.rateInfo.chargeableRateInfo.loyaltyInfo = LoyaltyInformation(null, LoyaltyEarnInfo(null, null), false)
        roomResponse.rateInfo.chargeableRateInfo.airAttached = false
        roomResponse.rateInfo.chargeableRateInfo.discountPercent = 10.0.toFloat()
        val viewModel = createViewModel(roomResponse, 3)

        assertEquals("-10%", viewModel.discountPercentageString)
    }

    @Test
    fun testPackageDiscountPercentageString() {
        val roomResponse = createRoomResponse()
        roomResponse.packageHotelDeltaPrice = Money(10, "USD")
        val viewModel = createViewModel(roomResponse, 4)

        assertNull(viewModel.discountPercentageString)
    }

    @Test
    fun testDiscountPercentageBackground() {
        val roomResponse = createRoomResponse()
        val viewModel = createViewModel(roomResponse, -1)
        val expectedBackground = ContextCompat.getDrawable(context, R.drawable.discount_percentage_background)
        assertEquals(expectedBackground, viewModel.discountPercentageBackground)
    }

    @Test
    fun testDiscountPercentageBackgroundMemberOnly() {
        mockSignIn()
        val roomResponse = createRoomResponse()
        roomResponse.isMemberDeal = true
        val viewModel = createViewModel(roomResponse, -1)
        val expectedBackground = ContextCompat.getDrawable(context, R.drawable.member_only_discount_percentage_background)
        assertEquals(expectedBackground, viewModel.discountPercentageBackground)
    }

    @Test
    fun testDiscountPercentageTextColor() {
        val roomResponse = createRoomResponse()
        val viewModel = createViewModel(roomResponse, -1)
        val expectedColor = ContextCompat.getColor(context, R.color.white)
        assertEquals(expectedColor, viewModel.discountPercentageTextColor)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testDiscountPercentageTextColorMemberOnly() {
        mockSignIn()
        val roomResponse = createRoomResponse()
        roomResponse.isMemberDeal = true
        val viewModel = createViewModel(roomResponse, -1)
        val expectedColor = ContextCompat.getColor(context, R.color.brand_primary)
        assertEquals(expectedColor, viewModel.discountPercentageTextColor)
    }

    @Test
    fun testPayLaterPriceStringNotPayLater() {
        val roomResponse = createRoomResponse()
        roomResponse.isPayLater = false
        val viewModel = createViewModel(roomResponse, 9)

        assertNull(viewModel.payLaterPriceString)
    }

    @Test
    fun testPayLaterTotalPriceString() {
        val roomResponse = createRoomResponse()
        roomResponse.isPayLater = true
        roomResponse.rateInfo.chargeableRateInfo.userPriceType = "RateForWholeStayWithTaxes"
        val viewModel = createViewModel(roomResponse, 7)

        val expectedMoney = Money(109, "USD").getFormattedMoney(Money.F_ALWAYS_TWO_PLACES_AFTER_DECIMAL)
        assertEquals("$expectedMoney", viewModel.payLaterPriceString)
    }

    @Test
    fun testPayLaterPriceString() {
        val roomResponse = createRoomResponse()
        roomResponse.isPayLater = true
        roomResponse.rateInfo.chargeableRateInfo.userPriceType = "PerNightRateNoTaxes"
        val viewModel = createViewModel(roomResponse, 5)

        val expectedMoney = Money(109, "USD").getFormattedMoney(Money.F_ALWAYS_TWO_PLACES_AFTER_DECIMAL)
        assertEquals("$expectedMoney/night", viewModel.payLaterPriceString)
    }

    @Test
    fun testShowDepositTermNotPayLater() {
        val roomResponse = createRoomResponse()
        roomResponse.isPayLater = false
        roomResponse.depositPolicy = List(2, { "policy" })
        val viewModel = createViewModel(roomResponse, 6)

        assertFalse(viewModel.showDepositTerm)
    }

    @Test
    fun testShowDepositTermNoDepositTerm() {
        val roomResponse = createRoomResponse()
        roomResponse.isPayLater = true
        roomResponse.depositPolicy = null
        var viewModel = createViewModel(roomResponse, 9)

        assertFalse(viewModel.showDepositTerm)

        roomResponse.depositPolicy = List(0, { "policy" })
        viewModel = createViewModel(roomResponse, 9)

        assertFalse(viewModel.showDepositTerm)
    }

    @Test
    fun testShowDepositTerm() {
        val roomResponse = createRoomResponse()
        roomResponse.isPayLater = true
        roomResponse.depositPolicy = List(2, { "policy" })
        val viewModel = createViewModel(roomResponse, 6)

        assertTrue(viewModel.showDepositTerm)
    }

    @Test
    fun testPayLaterStrikeThroughString() {
        val roomResponse = createRoomResponse()
        roomResponse.isPayLater = true
        val viewModel = createViewModel(roomResponse, 7)
        assertNull(viewModel.strikeThroughString)
    }

    @Test
    fun testNegativeStrikeThroughString() {
        val roomResponse = createRoomResponse()
        roomResponse.rateInfo.chargeableRateInfo.strikethroughPriceToShowUsers = -100f
        val viewModel = createViewModel(roomResponse, 7)
        assertNull(viewModel.strikeThroughString)
    }

    @Test
    fun testStrikeThroughStringLesserThanPrice() {
        val roomResponse = createRoomResponse()
        roomResponse.rateInfo.chargeableRateInfo.strikethroughPriceToShowUsers = 100f
        roomResponse.rateInfo.chargeableRateInfo.priceToShowUsers = 200f
        val viewModel = createViewModel(roomResponse, 7)
        assertNull(viewModel.strikeThroughString)
    }

    @Test
    fun testStrikeThroughStringEqualPrice() {
        val roomResponse = createRoomResponse()
        roomResponse.rateInfo.chargeableRateInfo.strikethroughPriceToShowUsers = 100f
        roomResponse.rateInfo.chargeableRateInfo.priceToShowUsers = 100f
        val viewModel = createViewModel(roomResponse, 7)
        assertNull(viewModel.strikeThroughString)
    }

    @Test
    fun testStrikeThroughString() {
        val roomResponse = createRoomResponse()
        roomResponse.rateInfo.chargeableRateInfo.strikethroughPriceToShowUsers = 200f
        val viewModel = createViewModel(roomResponse, 7)

        val expectedMoney = Money(200, "USD").getFormattedMoney(Money.F_ALWAYS_TWO_PLACES_AFTER_DECIMAL)
        assertEquals("$expectedMoney", viewModel.strikeThroughString)
    }

    @Test
    fun testPriceStringPayLaterWithDeposit() {
        val roomResponse = createRoomResponse()
        roomResponse.isPayLater = true
        roomResponse.depositPolicy = List(2, { "policy" })
        val viewModel = createViewModel(roomResponse, 1)

        assertNull(viewModel.priceString)
    }

    @Test
    fun testPriceStringPayLaterNoDepositNullDepositAmount() {
        val roomResponse = createRoomResponse()
        roomResponse.isPayLater = true
        roomResponse.depositPolicy = null
        roomResponse.rateInfo.chargeableRateInfo.depositAmountToShowUsers = null
        val viewModel = createViewModel(roomResponse, 1)

        val expectedMoney = Money(0, "USD").formattedMoney
        assertEquals("$expectedMoney Due Now", viewModel.priceString)
    }

    @Test
    fun testPriceStringPayLaterNoDeposit() {
        val roomResponse = createRoomResponse()
        roomResponse.isPayLater = true
        roomResponse.depositPolicy = null
        roomResponse.rateInfo.chargeableRateInfo.depositAmountToShowUsers = "10"
        val viewModel = createViewModel(roomResponse, 1)

        val expectedMoney = Money(10, "USD").formattedMoney
        assertEquals("$expectedMoney Due Now", viewModel.priceString)
    }

    @Test
    fun testPriceString() {
        val roomResponse = createRoomResponse()
        val viewModel = createViewModel(roomResponse, 1)

        val expectedMoney = Money(109, "USD").getFormattedMoney(Money.F_ALWAYS_TWO_PLACES_AFTER_DECIMAL)
        assertEquals("$expectedMoney", viewModel.priceString)
    }

    @Test
    fun testPackagePriceString() {
        val roomResponse = createRoomResponse()
        roomResponse.packageHotelDeltaPrice = Money(10, "USD")
        roomResponse.rateInfo.chargeableRateInfo.priceToShowUsers = 100f
        val viewModel = createViewModel(roomResponse, 1)

        val expectedMoney = Money(100, "USD").getFormattedMoney(Money.F_ALWAYS_TWO_PLACES_AFTER_DECIMAL)
        assertEquals("+$expectedMoney", viewModel.priceString)
    }

    @Test
    fun testPackageNegativePriceString() {
        val roomResponse = createRoomResponse()
        roomResponse.packageHotelDeltaPrice = Money(10, "USD")
        roomResponse.rateInfo.chargeableRateInfo.priceToShowUsers = -100f
        val viewModel = createViewModel(roomResponse, 1)

        val expectedMoney = Money(-100, "USD").getFormattedMoney(Money.F_ALWAYS_TWO_PLACES_AFTER_DECIMAL)
        assertEquals(expectedMoney, viewModel.priceString)
    }

    @Test
    fun testNotShowPerNightForTotal() {
        val roomResponse = createRoomResponse()
        roomResponse.rateInfo.chargeableRateInfo.userPriceType = "RateForWholeStayWithTaxes"
        val viewModel = createViewModel(roomResponse, -1)

        assertFalse(viewModel.showPerNight)
    }

    @Test
    fun testShowPerNight() {
        val roomResponse = createRoomResponse()
        roomResponse.rateInfo.chargeableRateInfo.userPriceType = "PerNightRateNoTaxes"
        val viewModel = createViewModel(roomResponse, -1)

        assertTrue(viewModel.showPerNight)
    }

    @Test
    fun testPricePerDescriptorStringPerNight() {
        val roomResponse = createRoomResponse()
        val viewModel = createViewModel(roomResponse, -1)

        assertEquals("/night", viewModel.pricePerDescriptorString)
    }

    @Test
    fun testPricePerDescriptorStringPerPerson() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(getContext(), AbacusUtils.EBAndroidAppPackagesMidApi)

        val roomResponse = createRoomResponse()
        val viewModel = createViewModel(roomResponse, -1)
        roomResponse.packageHotelDeltaPrice = Money("23", "USD")

        assertTrue(roomResponse.isPackage)
        assertEquals("/person", viewModel.pricePerDescriptorString)
    }

    @Test
    fun testPricePerDescriptorStringPerNightForPSS() {
        val roomResponse = createRoomResponse()
        val viewModel = createViewModel(roomResponse, -1)
        roomResponse.packageHotelDeltaPrice = Money("23", "USD")

        assertTrue(roomResponse.isPackage)
        assertEquals("/night", viewModel.pricePerDescriptorString)
    }

    @Test
    fun testHotelRoomRowButtonString() {
        val roomResponse = createRoomResponse()
        val viewModel = createViewModel(roomResponse, 1)
        assertEquals("Select", viewModel.hotelRoomRowButtonString)
    }

    @Test
    fun testPackageHotelRoomRowButtonString() {
        val roomResponse = createRoomResponse()
        roomResponse.packageHotelDeltaPrice = Money(10, "USD")
        val viewModel = createViewModel(roomResponse, 2)
        assertEquals("Book", viewModel.hotelRoomRowButtonString)
    }

    @Test
    fun testInvalidRoomLeftString() {
        val roomResponse = createRoomResponse()
        roomResponse.currentAllotment = "a"
        val viewModel = createViewModel(roomResponse, 0)

        assertNull(viewModel.roomLeftString)
    }

    @Test
    fun testZeroRoomLeftString() {
        val roomResponse = createRoomResponse()
        roomResponse.currentAllotment = "0"
        val viewModel = createViewModel(roomResponse, 0)

        assertNull(viewModel.roomLeftString)
    }

    @Test
    fun testTooManyRoomLeftString() {
        val roomResponse = createRoomResponse()
        roomResponse.currentAllotment = "10"
        val viewModel = createViewModel(roomResponse, 0)

        assertNull(viewModel.roomLeftString)
    }

    @Test
    fun testOneRoomLeftString() {
        val roomResponse = createRoomResponse()
        roomResponse.currentAllotment = "1"
        val viewModel = createViewModel(roomResponse, 0)

        assertEquals("We have 1 room left!", viewModel.roomLeftString)
    }

    @Test
    fun testFiveRoomLeftString() {
        val roomResponse = createRoomResponse()
        roomResponse.currentAllotment = "5"
        val viewModel = createViewModel(roomResponse, 0)

        assertEquals("We have 5 rooms left", viewModel.roomLeftString)
    }

    private fun createRoomResponse(): HotelOffersResponse.HotelRoomResponse {
        val resourceReader = JSONResourceReader("../lib/mocked/templates/m/api/hotel/offers/happypath.json")
        val offerResponse = resourceReader.constructUsingGson(HotelOffersResponse::class.java)

        val roomResponse = offerResponse.hotelRoomResponse[0]

        return roomResponse
    }

    private fun createRoomResponse(valueAddIds: List<Int>): HotelOffersResponse.HotelRoomResponse {
        val roomResponse = createRoomResponse()

        val valueAdds = ArrayList<HotelOffersResponse.ValueAdds>()
        valueAddIds.forEachIndexed { _, id ->
            val valueAdd = HotelOffersResponse.ValueAdds()
            valueAdd.id = id.toString()
            valueAdd.description = id.toString()

            valueAdds.add(valueAdd)
        }
        roomResponse.valueAdds = valueAdds

        return roomResponse
    }

    private fun createViewModel(roomResponse: HotelOffersResponse.HotelRoomResponse, optionIndex: Int): HotelRoomDetailViewModel {
        return HotelRoomDetailViewModel(context, roomResponse, "id", 0, optionIndex, false)
    }

    private fun createLoyaltyInformation(): LoyaltyInformation {
        val earnInfo = PointsEarnInfo(10, 5, 15)
        val loyaltyEarnInfo = LoyaltyEarnInfo(earnInfo, null)
        return LoyaltyInformation(null, loyaltyEarnInfo, false)
    }

    private fun createPriceLoyaltyInformation(): LoyaltyInformation {
        val priceEarnInfo = PriceEarnInfo(Money("1", "USD"), Money("0.5", "USD"), Money("1.5", "USD"))
        val loyaltyEarnInfo = LoyaltyEarnInfo(null, priceEarnInfo)
        return LoyaltyInformation(null, loyaltyEarnInfo, false)
    }

    private fun testValueAddsToShow(ids: List<Int>, enum: ValueAddsEnum) {
        for (id in ids) {
            val roomResponse = createRoomResponse(intArrayOf(id).toList())
            val vm = createViewModel(roomResponse, 0)
            val toShow = vm.getValueAdds()

            assertEquals(1, toShow.count())

            assertEquals(enum, toShow[0].valueAddsEnum)
        }
    }

    private fun mockSignIn() {
        val user = UserLoginTestUtil.mockUser()
        UserLoginTestUtil.setupUserAndMockLogin(user)
    }
}
