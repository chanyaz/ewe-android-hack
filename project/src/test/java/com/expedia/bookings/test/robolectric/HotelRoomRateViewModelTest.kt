package com.expedia.bookings.test.robolectric

import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.payment.LoyaltyEarnInfo
import com.expedia.bookings.data.payment.LoyaltyInformation
import com.expedia.bookings.data.payment.PointsEarnInfo
import com.expedia.bookings.data.payment.PriceEarnInfo
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.test.MockHotelServiceTestRule
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.util.endlessObserver
import com.expedia.vm.HotelDetailViewModel
import com.expedia.vm.HotelRoomRateViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import rx.observers.TestSubscriber
import java.text.DecimalFormat
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
class HotelRoomRateViewModelTest {

    val mockHotelServiceTestRule: MockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get
    private val context = RuntimeEnvironment.application
    lateinit private var sut: HotelRoomRateViewModel
    lateinit private var mockHotelDetailViewModel: HotelDetailViewModel
    lateinit private var hotelRoomResponse: HotelOffersResponse.HotelRoomResponse
    lateinit private var hotelOfferResponse: HotelOffersResponse

    private var expectedAmenity = ""

    @Before
    fun before() {
        hotelOfferResponse = mockHotelServiceTestRule.getHappyOfferResponse()
        hotelRoomResponse = hotelOfferResponse.hotelRoomResponse.first()
    }

    @Test
    fun happy() {
        setupNonSoldOutRoomUnderTest()

        assertEquals("-20%", sut.discountPercentage.value)
        assertFalse(sut.onlyShowTotalPrice.value)
        assertEquals("", sut.strikeThroughPriceObservable.value)
        assertEquals("$109", sut.dailyPricePerNightObservable.value)
        assertTrue(sut.perNightPriceVisibleObservable.value)
        assertEquals("One King Bed", sut.collapsedBedTypeObservable.value)
        assertEquals("One King Bed", sut.expandedBedTypeObservable.value)

        assertEquals("Non-refundable", sut.expandedMessageObservable.value.first)
        assertEquals(R.drawable.room_non_refundable, sut.expandedMessageObservable.value.second)

        assertEquals("Non-refundable", sut.collapsedUrgencyObservable.value)
        assertEquals(expectedAmenity, sut.expandedAmenityObservable.value)
    }

    @Test
    fun hasFreeCancellation() {
        givenOfferHasFreeCancellation()
        setupNonSoldOutRoomUnderTest()

        assertEquals("Free Cancellation", sut.expandedMessageObservable.value.first)
        assertEquals(R.drawable.room_checkmark, sut.expandedMessageObservable.value.second)
    }

    @Test
    fun payLaterOfferShowPerNightPrice() {
        givenOfferIsPayLater()
        setupNonSoldOutRoomUnderTest()

        assertEquals("$109/night", sut.strikeThroughPriceObservable.value)

    }

    @Test
    fun discountPercentIsShown() {
        setupNonSoldOutRoomUnderTest()

        assertTrue(sut.shouldShowDiscountPercentage.value)
        assertEquals("-20%", sut.discountPercentage.value)
        assertEquals("", sut.strikeThroughPriceObservable.value)
    }

    @Test
    fun zeroDiscountPercentIsNotShown() {
        givenDiscountPercentIsZero()
        setupNonSoldOutRoomUnderTest()

        assertFalse(sut.shouldShowDiscountPercentage.value)
    }

    @Test
    fun discountPercentNotShownWithSWP() {
        val loyaltyInfo = LoyaltyInformation(null, LoyaltyEarnInfo(null, null), true)
        hotelRoomResponse.rateInfo.chargeableRateInfo.loyaltyInfo = loyaltyInfo

        setupNonSoldOutRoomUnderTest()

        assertFalse(sut.shouldShowDiscountPercentage.value)
    }

    @Test
    fun showStrikeThroughPrice() {
        val chargeableRateInfo = hotelRoomResponse.rateInfo.chargeableRateInfo
        val newValidStrikeThroughPrice = chargeableRateInfo.priceToShowUsers + 10f
        val df = DecimalFormat("#")
        givenWeHaveValidStrikeThroughPrice(newValidStrikeThroughPrice)
        setupNonSoldOutRoomUnderTest()

        assertEquals("$" + df.format(newValidStrikeThroughPrice).toString(), sut.strikeThroughPriceObservable.value.toString())
    }

    @Test
    fun strikeThroughPriceNotShownToUser() {
        val chargeableRateInfo = hotelRoomResponse.rateInfo.chargeableRateInfo
        chargeableRateInfo.strikethroughPriceToShowUsers = chargeableRateInfo.priceToShowUsers
        setupNonSoldOutRoomUnderTest()

        assertEquals("", sut.strikeThroughPriceObservable.value.toString())
    }

    private fun givenWeHaveValidStrikeThroughPrice(strikeThroughPrice: Float) {
        hotelRoomResponse.rateInfo.chargeableRateInfo.strikethroughPriceToShowUsers = strikeThroughPrice
    }

    @Test
    fun soldOutButtonLabelEmitsOnSoldOut() {
        setupSoldOutRoomUnderTest()

        sut.roomSoldOut.onNext(true)

        val testSubscriber = TestSubscriber.create<CharSequence>()
        sut.soldOutButtonLabelObservable.subscribe(testSubscriber)
        testSubscriber.assertValue("Sold Out")
    }

    @Test
    fun userSeesZeroPriceWhenPriceToShowUsersIsNegative() {
        givenPriceToShowUsersIsNegative()
        setupNonSoldOutRoomUnderTest()
        assertEquals("$0", sut.dailyPricePerNightObservable.value)
    }

    @Test
    fun earnMessageIsShown() {
        PointOfSale.getPointOfSale().isEarnMessageEnabledForHotels = true
        UserLoginTestUtil.setupUserAndMockLogin(UserLoginTestUtil.mockUser())
        val loyaltyInfo = LoyaltyInformation(null, LoyaltyEarnInfo(null, PriceEarnInfo(Money("320", "USD"), Money("0", "USD"), Money("320", "USD"))), true)
        hotelRoomResponse.rateInfo.chargeableRateInfo.loyaltyInfo = loyaltyInfo
        setupNonSoldOutRoomUnderTest()


        assertTrue(sut.collapsedEarnMessageVisibilityObservable.value)
        assertEquals("Earn $320", sut.collapsedEarnMessageObservable.value.toString())
    }

    @Test
    fun earnMessageIsNotShown() {
        PointOfSale.getPointOfSale().isEarnMessageEnabledForHotels = false
        val loyaltyInfo = LoyaltyInformation(null, LoyaltyEarnInfo(PointsEarnInfo(320, 0, 320), null), true)
        hotelRoomResponse.rateInfo.chargeableRateInfo.loyaltyInfo = loyaltyInfo

        setupNonSoldOutRoomUnderTest()

        assertFalse(sut.collapsedEarnMessageVisibilityObservable.value)
    }

    private fun givenDiscountPercentIsZero() {
        hotelRoomResponse.rateInfo.chargeableRateInfo.discountPercent = 0f
    }

    private fun givenOfferIsPayLater() {
        hotelRoomResponse.isPayLater = true
    }

    private fun givenOfferHasFreeCancellation() {
        hotelRoomResponse.hasFreeCancellation = true
    }

    private fun givenPriceToShowUsersIsNegative() {
        hotelRoomResponse.rateInfo.chargeableRateInfo.priceToShowUsers = -100.12f
    }

    private fun setupNonSoldOutRoomUnderTest() {
        val rowIndex = 0
        expectedAmenity = "Free wifi"
        mockHotelDetailViewModel = HotelDetailViewModel(context, mockHotelServiceTestRule.services!!, endlessObserver { /*ignore*/ })

        sut = HotelRoomRateViewModel(context, hotelOfferResponse.hotelId, hotelRoomResponse, expectedAmenity, rowIndex, mockHotelDetailViewModel.rowExpandingObservable, endlessObserver { }, false)
    }

    private fun setupSoldOutRoomUnderTest() {
        val rowIndex = 0
        expectedAmenity = "Free wifi"
        mockHotelDetailViewModel = HotelDetailViewModel(context, mockHotelServiceTestRule.services!!, endlessObserver { /*ignore*/ })

        sut = HotelRoomRateViewModel(context, hotelOfferResponse.hotelId, hotelRoomResponse, expectedAmenity, rowIndex, mockHotelDetailViewModel.rowExpandingObservable, endlessObserver { }, false)
    }
}
