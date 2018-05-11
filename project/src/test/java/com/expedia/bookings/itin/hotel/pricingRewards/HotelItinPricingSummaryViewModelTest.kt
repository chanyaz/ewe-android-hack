package com.expedia.bookings.itin.hotel.pricingRewards

import android.arch.lifecycle.LifecycleOwner
import com.expedia.bookings.R
import com.expedia.bookings.itin.helpers.ItinMocker
import com.expedia.bookings.itin.helpers.MockActivityLauncher
import com.expedia.bookings.itin.helpers.MockHotelRepo
import com.expedia.bookings.itin.helpers.MockLifecycleOwner
import com.expedia.bookings.itin.helpers.MockStringProvider
import com.expedia.bookings.itin.hotel.repositories.ItinHotelRepoInterface
import com.expedia.bookings.itin.scopes.HasActivityLauncher
import com.expedia.bookings.itin.scopes.HasHotelRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.tripstore.extensions.firstHotel
import com.expedia.bookings.itin.utils.IActivityLauncher
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.utils.FontCache
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class HotelItinPricingSummaryViewModelTest {
    private val INR = "\u20b9"
    private val mockItinSingleRoom = ItinMocker.hotelDetailsHappy
    private val mockItinMultipleRoom = ItinMocker.hotelDetailsHappyMultipleRooms
    private val mockItinPosSameAsPosu = ItinMocker.hotelDetailsPosSameAsPoSu
    private val mockItinPartialPoints = ItinMocker.hotelDetailsPaidWithPointsPartial
    private val mockItinFullPoints = ItinMocker.hotelDetailsPaidWithPointsFull
    private val mockItinExpediaCollect = ItinMocker.hotelDetailsExpediaCollect
    private val mockHotelSingleRoom = mockItinSingleRoom.firstHotel()
    private val mockHotelMultipleRooms = mockItinMultipleRoom.firstHotel()
    private val mockHotelPosSameAsPosu = mockItinPosSameAsPosu.firstHotel()
    private val mockHotelExpediaCollect = mockItinExpediaCollect.firstHotel()

    private lateinit var roomItemObserver: TestObserver<HotelItinPriceLineItem>
    private lateinit var roomContainerClearObserver: TestObserver<Unit>
    private lateinit var multipleGuestItemObserver: TestObserver<HotelItinPriceLineItem>
    private lateinit var taxesAndFeesItemObserver: TestObserver<HotelItinPriceLineItem>
    private lateinit var couponItemObserver: TestObserver<HotelItinPriceLineItem>
    private lateinit var pointsItemObserver: TestObserver<HotelItinPriceLineItem>
    private lateinit var currencyDisclaimerObserver: TestObserver<String>
    private lateinit var totalPriceObserver: TestObserver<HotelItinPriceLineItem>
    private lateinit var totalPricePosCurrencyObserver: TestObserver<HotelItinPriceLineItem>
    private lateinit var additionalPriceInfoObserver: TestObserver<Unit>

    @Before
    fun setup() {
        roomItemObserver = TestObserver()
        multipleGuestItemObserver = TestObserver()
        taxesAndFeesItemObserver = TestObserver()
        couponItemObserver = TestObserver()
        pointsItemObserver = TestObserver()
        currencyDisclaimerObserver = TestObserver()
        totalPriceObserver = TestObserver()
        totalPricePosCurrencyObserver = TestObserver()
        roomContainerClearObserver = TestObserver()
        additionalPriceInfoObserver = TestObserver()
    }

    @After
    fun tearDown() {
        roomItemObserver.dispose()
        multipleGuestItemObserver.dispose()
        taxesAndFeesItemObserver.dispose()
        couponItemObserver.dispose()
        pointsItemObserver.dispose()
        currencyDisclaimerObserver.dispose()
        totalPriceObserver.dispose()
        totalPricePosCurrencyObserver.dispose()
        roomContainerClearObserver.dispose()
        additionalPriceInfoObserver.dispose()
    }

    @Test
    fun testSingleRoomOutputMultipleItems() {
        val viewModel = getViewModel()
        roomItemObserver.assertEmpty()

        viewModel.hotelObserver.onChanged(mockHotelSingleRoom)

        assertEquals(5, roomItemObserver.valueCount())
    }

    @Test
    fun testMultipleRoomsOutputsMultipleItems() {
        val viewModel = getViewModel()
        roomItemObserver.assertEmpty()

        viewModel.hotelObserver.onChanged(mockHotelMultipleRooms)

        assertEquals(17, roomItemObserver.valueCount())
    }

    @Test
    fun testMultipleRoomItemCountsAndValues() {
        val viewModel = getViewModel()
        roomItemObserver.assertEmpty()

        viewModel.hotelObserver.onChanged(mockHotelMultipleRooms)

        val roomItems = roomItemObserver.values()
        assertNotNull(roomItems)
        assertEquals(17, roomItems?.size)
        //Room Total Price Item
        assertEquals((R.string.itin_hotel_details_cost_summary_room_price_text).toString(), roomItems[0].labelString)
        assertEquals("${INR}3,500.00", roomItems[0].priceString)
        assertEquals(R.color.itin_price_summary_label_gray_dark, roomItems[0].colorRes)

        //Room Price Per Day Item
        assertEquals("Mon, Mar 12", roomItems[1].labelString)
        assertEquals("${INR}875.00", roomItems[1].priceString)
        assertEquals(R.color.itin_price_summary_label_gray_light, roomItems[1].colorRes)

        //Room Property Fee
        assertEquals((R.string.itin_hotel_price_summary_property_fee_label).toString(), roomItems[5].labelString)
        assertEquals("$22.60", roomItems[5].priceString)
        assertEquals(R.color.itin_price_summary_label_gray_light, roomItems[5].colorRes)
    }

    @Test
    fun testPriceLineItemsWithoutFees() {
        val noFeesViewModel = getViewModel()
        multipleGuestItemObserver.assertEmpty()
        taxesAndFeesItemObserver.assertEmpty()
        couponItemObserver.assertEmpty()
        pointsItemObserver.assertEmpty()

        noFeesViewModel.hotelObserver.onChanged(mockHotelSingleRoom)
        noFeesViewModel.itinObserver.onChanged(mockItinSingleRoom)
        multipleGuestItemObserver.assertEmpty()
        taxesAndFeesItemObserver.assertEmpty()
        couponItemObserver.assertEmpty()
        pointsItemObserver.assertEmpty()
    }

    @Test
    fun testMultipleGuestFeeSubject() {
        val feesViewModel = getViewModel()
        multipleGuestItemObserver.assertEmpty()

        feesViewModel.hotelObserver.onChanged(mockHotelMultipleRooms)

        multipleGuestItemObserver.assertValueCount(1)
        val multiGuestFeeItem = multipleGuestItemObserver.values()
        assertEquals("${INR}8.50", multiGuestFeeItem[0].priceString)
        assertEquals((R.string.itin_hotel_price_summary_multiple_guest_fees_label).toString(), multiGuestFeeItem[0].labelString)
        assertEquals(R.color.itin_price_summary_label_gray_light, multiGuestFeeItem[0].colorRes)
    }

    @Test
    fun testTaxesAndFeesSubject() {
        val feesViewModel = getViewModel()
        taxesAndFeesItemObserver.assertEmpty()

        feesViewModel.hotelObserver.onChanged(mockHotelMultipleRooms)

        taxesAndFeesItemObserver.assertValueCount(1)
        val taxesAndFeesItem = taxesAndFeesItemObserver.values()
        assertEquals("${INR}3.50", taxesAndFeesItem[0].priceString)
        assertEquals((R.string.itin_hotel_price_summary_taxes_and_fees_label).toString(), taxesAndFeesItem[0].labelString)
        assertEquals(R.color.itin_price_summary_label_gray_dark, taxesAndFeesItem[0].colorRes)
    }

    @Test
    fun testCouponItemSubject() {
        val feesViewModel = getViewModel()
        couponItemObserver.assertEmpty()

        feesViewModel.hotelObserver.onChanged(mockHotelExpediaCollect)

        couponItemObserver.assertValueCount(1)
        val couponItem = couponItemObserver.values()
        assertEquals("-$54.00", couponItem[0].priceString)
        assertEquals((R.string.itin_hotel_price_summary_coupons_label).toString(), couponItem[0].labelString)
        assertEquals(R.color.itin_price_summary_label_green, couponItem[0].colorRes)
    }

    @Test
    fun testPointsItemSubject() {
        val feesViewModel = getViewModel()
        pointsItemObserver.assertEmpty()

        feesViewModel.itinObserver.onChanged(mockItinPartialPoints)

        pointsItemObserver.assertValueCount(1)
        val pointsItem = pointsItemObserver.values()
        val expected = (R.string.itin_hotel_price_summary_points_value_TEMPLATE).toString().plus(mapOf("points" to "$2.00"))
        assertEquals(expected, pointsItem[0].priceString)
        assertEquals((R.string.itin_hotel_price_summary_points_label).toString(), pointsItem[0].labelString)
        assertEquals(R.color.itin_price_summary_label_green, pointsItem[0].colorRes)
    }

    @Test
    fun testCurrencyDisclaimerSubjectPosDiffFromPosu() {
        val feesViewModel = getViewModel()
        currencyDisclaimerObserver.assertEmpty()

        feesViewModel.hotelObserver.onChanged(mockHotelMultipleRooms)

        currencyDisclaimerObserver.assertValueCount(1)
        val currencyText = currencyDisclaimerObserver.values()[0]
        assertEquals("Rate quotes in USD are based on current exchange rates, which may vary at time of travel. Final payment will be settled in local currency directly with the hotel.", currencyText)
    }

    @Test
    fun testCurrencyDisclaimerSubjectPosSameAsPosu() {
        val feesViewModel = getViewModel()
        currencyDisclaimerObserver.assertEmpty()

        feesViewModel.hotelObserver.onChanged(mockHotelPosSameAsPosu)

        currencyDisclaimerObserver.assertValueCount(1)
        val currencyText = currencyDisclaimerObserver.values()[0]
        assertEquals("Unless specified otherwise, <span class=\"rr-bold\">rates are quoted in </span><span class=\"rr-bold\" id=\"hotel_id-default-currency\">US dollars</span>.", currencyText)
    }

    @Test
    fun testTotalPriceSubjectHotelCollect() {
        val feesViewModel = getViewModel()
        totalPriceObserver.assertEmpty()

        feesViewModel.itinObserver.onChanged(mockItinMultipleRoom)

        totalPriceObserver.assertValueCount(1)
        val totalPriceItem = totalPriceObserver.values()
        assertEquals("₹3,500.00", totalPriceItem[0].priceString)
        assertEquals((R.string.itin_hotel_price_summary_total_amount_due_label).toString(), totalPriceItem[0].labelString)
        assertEquals(R.color.itin_price_summary_label_gray_dark, totalPriceItem[0].colorRes)
        assertEquals(16.0f, totalPriceItem[0].textSize)
        assertEquals(FontCache.Font.ROBOTO_MEDIUM, totalPriceItem[0].font)
    }

    @Test
    fun testTotalPriceSubjectExpediaCollectPartialPoints() {
        val feesViewModel = getViewModel()
        totalPriceObserver.assertEmpty()

        feesViewModel.itinObserver.onChanged(mockItinPartialPoints)

        totalPriceObserver.assertValueCount(1)
        val totalPriceItem = totalPriceObserver.values()
        assertEquals("$90.01", totalPriceItem[0].priceString)
        assertEquals((R.string.itin_hotel_price_summary_total_amount_paid_label).toString(), totalPriceItem[0].labelString)
        assertEquals(R.color.itin_price_summary_label_gray_dark, totalPriceItem[0].colorRes)
        assertEquals(16.0f, totalPriceItem[0].textSize)
        assertEquals(FontCache.Font.ROBOTO_MEDIUM, totalPriceItem[0].font)
    }

    @Test
    fun testTotalPriceSubjectExpediaCollectFullPoints() {
        val feesViewModel = getViewModel()
        totalPriceObserver.assertEmpty()

        feesViewModel.itinObserver.onChanged(mockItinFullPoints)

        totalPriceObserver.assertValueCount(1)
        val totalPriceItem = totalPriceObserver.values()
        assertEquals("$0.00", totalPriceItem[0].priceString)
        assertEquals((R.string.itin_hotel_price_summary_total_amount_paid_label).toString(), totalPriceItem[0].labelString)
        assertEquals(R.color.itin_price_summary_label_gray_dark, totalPriceItem[0].colorRes)
        assertEquals(16.0f, totalPriceItem[0].textSize)
        assertEquals(FontCache.Font.ROBOTO_MEDIUM, totalPriceItem[0].font)
    }

    @Test
    fun testTotalPriceSubjectExpediaCollect() {
        val feesViewModel = getViewModel()
        totalPriceObserver.assertEmpty()

        feesViewModel.itinObserver.onChanged(mockItinExpediaCollect)

        totalPriceObserver.assertValueCount(1)
        val totalPriceItem = totalPriceObserver.values()
        assertEquals("$260.18", totalPriceItem[0].priceString)
        assertEquals((R.string.itin_hotel_price_summary_total_amount_paid_label).toString(), totalPriceItem[0].labelString)
        assertEquals(R.color.itin_price_summary_label_gray_dark, totalPriceItem[0].colorRes)
        assertEquals(16.0f, totalPriceItem[0].textSize)
        assertEquals(FontCache.Font.ROBOTO_MEDIUM, totalPriceItem[0].font)
    }

    @Test
    fun testAdditionalPriceInfoClick() {
        val scope = MockHotelItinPricingSummaryScope()
        val viewModel = HotelItinPricingSummaryViewModel(scope)
        viewModel.additionalPricingInfoSubject.subscribe(additionalPriceInfoObserver)
        viewModel.itinObserver.onChanged(mockItinExpediaCollect)

        additionalPriceInfoObserver.assertEmpty()
        assertFalse(scope.mockActivityLauncher.launched)

        viewModel.additionalPricingInfoSubject.onNext(Unit)
        additionalPriceInfoObserver.assertValueCount(1)
        assertTrue(scope.mockActivityLauncher.launched)
    }

    private fun getViewModel(): HotelItinPricingSummaryViewModel<MockHotelItinPricingSummaryScope> {
        val viewModel = HotelItinPricingSummaryViewModel(MockHotelItinPricingSummaryScope())

        viewModel.roomContainerItemSubject.subscribe(roomItemObserver)
        viewModel.roomContainerClearSubject.subscribe(roomContainerClearObserver)
        viewModel.multipleGuestItemSubject.subscribe(multipleGuestItemObserver)
        viewModel.taxesAndFeesItemSubject.subscribe(taxesAndFeesItemObserver)
        viewModel.couponsItemSubject.subscribe(couponItemObserver)
        viewModel.pointsItemSubject.subscribe(pointsItemObserver)
        viewModel.currencyDisclaimerSubject.subscribe(currencyDisclaimerObserver)
        viewModel.totalPriceItemSubject.subscribe(totalPriceObserver)
        viewModel.totalPriceInPosCurrencyItemSubject.subscribe(totalPricePosCurrencyObserver)

        return viewModel
    }

    class MockHotelItinPricingSummaryScope : HasHotelRepo, HasStringProvider, HasActivityLauncher, HasLifecycleOwner {
        val mockActivityLauncher = MockActivityLauncher()
        override val strings: StringSource = MockStringProvider()
        override val itinHotelRepo: ItinHotelRepoInterface = MockHotelRepo()
        override val activityLauncher: IActivityLauncher = mockActivityLauncher
        override val lifecycleOwner: LifecycleOwner = MockLifecycleOwner()
    }
}
