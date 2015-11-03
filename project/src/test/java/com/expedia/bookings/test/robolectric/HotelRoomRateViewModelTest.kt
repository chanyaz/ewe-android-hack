package com.expedia.bookings.test.robolectric

import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.test.MockHotelServiceTestRule
import com.expedia.util.endlessObserver
import com.expedia.vm.HotelDetailViewModel
import com.expedia.vm.HotelRoomRateViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.text.DecimalFormat
import rx.observers.TestSubscriber
import rx.subjects.BehaviorSubject
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
public class HotelRoomRateViewModelTest {

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
        assertNull(sut.strikeThroughPriceObservable.value)
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
    fun payLaterOfferDontShowStrikeThroughPrice() {
        givenOfferIsPayLater()
        setupNonSoldOutRoomUnderTest()

        assertNull(sut.strikeThroughPriceObservable.value)
    }

    @Test
    fun discountLessThanTenPercentDontShow() {
        givenDiscountLessThanTenPercent()
        setupNonSoldOutRoomUnderTest()

        assertNull(sut.discountPercentage.value)
        assertNull(sut.strikeThroughPriceObservable.value)
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

    private fun givenDiscountLessThanTenPercent() {
        hotelRoomResponse.rateInfo.chargeableRateInfo.discountPercent = 9f
    }

    private fun givenOfferIsPayLater() {
        hotelRoomResponse.isPayLater = true
    }

    private fun givenOfferHasFreeCancellation() {
        hotelRoomResponse.hasFreeCancellation = true
    }

    private fun setupNonSoldOutRoomUnderTest() {
        val rowIndex = 0
        expectedAmenity = "Free wifi"
        mockHotelDetailViewModel = HotelDetailViewModel(context, mockHotelServiceTestRule.service, endlessObserver { /*ignore*/ })

        sut = HotelRoomRateViewModel(context, hotelOfferResponse.hotelId, hotelRoomResponse, expectedAmenity, rowIndex, mockHotelDetailViewModel.rowExpandingObservable)
    }

    private fun setupSoldOutRoomUnderTest() {
        val rowIndex = 0
        expectedAmenity = "Free wifi"
        mockHotelDetailViewModel = HotelDetailViewModel(context, mockHotelServiceTestRule.service, endlessObserver { /*ignore*/ })

        sut = HotelRoomRateViewModel(context, hotelOfferResponse.hotelId, hotelRoomResponse, expectedAmenity, rowIndex, mockHotelDetailViewModel.rowExpandingObservable)
    }
}
