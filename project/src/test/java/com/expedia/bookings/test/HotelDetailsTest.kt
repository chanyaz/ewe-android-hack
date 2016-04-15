package com.expedia.bookings.test

import android.app.Activity
import android.support.v4.content.ContextCompat
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.payment.LoyaltyBurnInfo
import com.expedia.bookings.data.payment.LoyaltyEarnInfo
import com.expedia.bookings.data.payment.LoyaltyInformation
import com.expedia.bookings.data.payment.LoyaltyType
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.widget.HotelDetailView
import com.expedia.util.endlessObserver
import com.expedia.vm.hotel.HotelDetailViewModel
import com.squareup.phrase.Phrase
import org.joda.time.LocalDate
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import java.util.ArrayList
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
class HotelDetailsTest {

    private var vm: HotelDetailViewModel by Delegates.notNull()
    private var hotelDetailView: HotelDetailView by Delegates.notNull()
    private var activity: Activity by Delegates.notNull()
    private var offers : HotelOffersResponse by Delegates.notNull()

    lateinit private var checkIn: LocalDate
    lateinit private var checkOut: LocalDate
    lateinit private var searchParams: HotelSearchParams

    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Hotels)
        hotelDetailView = android.view.LayoutInflater.from(activity).inflate(R.layout.test_hotel_details_widget, null) as HotelDetailView
        vm = HotelDetailViewModel(activity.applicationContext, endlessObserver { /*ignore*/ })
        hotelDetailView.viewmodel = vm

        offers = HotelOffersResponse()
        offers.hotelId = "happyPath"
        offers.hotelCity = "San Francisco"
        offers.hotelStateProvince = "CA"
        offers.hotelCountry = "US of A"
        offers.checkInDate = LocalDate.now().toString()
        offers.checkOutDate = LocalDate.now().plusDays(2).toString()
        offers.hotelGuestRating = 5.0
        offers.hotelStarRating = 5.0
        offers.deskTopOverrideNumber = false
        offers.telesalesNumber = "1-800-766-6658"
    }

    @Test
    fun testReviewContainerDrawable() {
        vm.hotelOffersSubject.onNext(offers)
        Assert.assertEquals(ContextCompat.getDrawable(activity, R.drawable.hotel_detail_ripple), hotelDetailView.ratingContainer.background)
        offers.hotelGuestRating = 0.0
        vm.hotelOffersSubject.onNext(offers)
        Assert.assertEquals(ContextCompat.getDrawable(activity, R.color.search_results_list_bg_gray), hotelDetailView.ratingContainer.background)
        offers.hotelGuestRating = 5.0
    }

    @Test
    fun testDiscountPercentageVipAccessTonightOnly() {
        val hotel = makeHotel()
        hotel.currentAllotment = "0"
        offers.isVipAccess = true
        hotel.isSameDayDRR = true

        val lowRateInfo = HotelRate()
        lowRateInfo.discountPercent = -20f
        lowRateInfo.currencyCode = "USD"

        val rateInfo = HotelOffersResponse.RateInfo()
        rateInfo.chargeableRateInfo = lowRateInfo
        hotel.rateInfo = rateInfo

        val rooms = ArrayList<HotelOffersResponse.HotelRoomResponse>();
        rooms.add(hotel)

        offers.hotelRoomResponse = rooms

        vm.hotelOffersSubject.onNext(offers)

        assertEquals(View.VISIBLE, hotelDetailView.hotelMessagingContainer.visibility)
        val expectedDiscountMessage = Phrase.from(activity.resources, R.string.hotel_discount_percent_Template).put("discount", -20).format().toString()
        assertEquals(expectedDiscountMessage, hotelDetailView.discountPercentage.text)
        assertEquals(View.VISIBLE, hotelDetailView.discountPercentage.visibility)
        assertEquals(View.VISIBLE, hotelDetailView.vipAccessMessageContainer.visibility)
        assertEquals(View.VISIBLE, hotelDetailView.promoMessage.visibility)
        assertEquals(activity.resources.getString(R.string.tonight_only), hotelDetailView.promoMessage.text)
    }

    @Test
    fun testNightlyPriceGuestCount() {
        val hotel = makeHotel()
        val lowRateInfo = HotelRate()
        lowRateInfo.averageRate = 300f
        lowRateInfo.discountPercent = -20f
        lowRateInfo.currencyCode = "USD"

        val rateInfo = HotelOffersResponse.RateInfo()
        rateInfo.chargeableRateInfo = lowRateInfo
        hotel.rateInfo = rateInfo

        val rooms = ArrayList<HotelOffersResponse.HotelRoomResponse>();
        rooms.add(hotel)

        offers.hotelRoomResponse = rooms

        givenHotelSearchParams()


        vm.hotelOffersSubject.onNext(offers)

        val string = Phrase.from(activity, R.string.calendar_instructions_date_range_with_guests_TEMPLATE).put("startdate",
                DateUtils.localDateToMMMd(checkIn)).put("enddate",
                DateUtils.localDateToMMMd(checkOut)).put("guests",
                activity.resources.getQuantityString(R.plurals.number_of_guests, searchParams.children.size + searchParams.adults, searchParams.children.size + searchParams.adults))
                .format()
                .toString()

        assertEquals("$300", hotelDetailView.price.text)

        assertEquals(string, hotelDetailView.searchInfo.text)
    }

    @Test
    fun testOnlyDiscountPercentage() {
        val hotel = makeHotel()
        hotel.currentAllotment = "0"
        offers.isVipAccess = false
        hotel.isSameDayDRR = false

        val lowRateInfo = HotelRate()
        lowRateInfo.discountPercent = -20f
        lowRateInfo.currencyCode = "USD"

        val rateInfo = HotelOffersResponse.RateInfo()
        rateInfo.chargeableRateInfo = lowRateInfo
        hotel.rateInfo = rateInfo

        val rooms = ArrayList<HotelOffersResponse.HotelRoomResponse>();
        rooms.add(hotel)

        offers.hotelRoomResponse = rooms

        vm.hotelOffersSubject.onNext(offers)

        assertEquals(View.VISIBLE, hotelDetailView.hotelMessagingContainer.visibility)
        val expectedDiscountMessage = Phrase.from(activity.resources, R.string.hotel_discount_percent_Template).put("discount", -20).format().toString()
        assertEquals(expectedDiscountMessage, hotelDetailView.discountPercentage.text)
        assertEquals(View.VISIBLE, hotelDetailView.discountPercentage.visibility)
        assertEquals(View.GONE, hotelDetailView.vipAccessMessageContainer.visibility)
        assertEquals("", hotelDetailView.promoMessage.text)
    }

    @Test
    fun testOnlyVipAccess() {
        val hotel = makeHotel()
        hotel.currentAllotment = "0"
        offers.isVipAccess = true
        hotel.isSameDayDRR = false

        val lowRateInfo = HotelRate()
        lowRateInfo.discountPercent = 0f
        lowRateInfo.currencyCode = "USD"

        val rateInfo = HotelOffersResponse.RateInfo()
        rateInfo.chargeableRateInfo = lowRateInfo
        hotel.rateInfo = rateInfo

        val rooms = ArrayList<HotelOffersResponse.HotelRoomResponse>();
        rooms.add(hotel)

        offers.hotelRoomResponse = rooms

        vm.hotelOffersSubject.onNext(offers)

        assertEquals(View.VISIBLE, hotelDetailView.hotelMessagingContainer.visibility)
        assertEquals(View.GONE, hotelDetailView.discountPercentage.visibility)
        assertEquals(View.VISIBLE, hotelDetailView.vipAccessMessageContainer.visibility)
        assertEquals(View.GONE, hotelDetailView.vipLoyaltyMessage.visibility)
        assertEquals(View.GONE, hotelDetailView.roomRateHeader.visibility)
        assertEquals(View.GONE, hotelDetailView.roomRateVIPLoyaltyAppliedContainer.visibility)
        assertEquals("", hotelDetailView.promoMessage.text)
    }

    @Test
    fun testVipAccessWithLoyaltyApplied() {
        val hotel = makeHotel()
        hotel.currentAllotment = "0"
        offers.isVipAccess = true
        offers.doesAnyHotelRateOfAnyRoomHaveLoyaltyInfo = true
        hotel.isSameDayDRR = false

        val lowRateInfo = HotelRate()
        lowRateInfo.discountPercent = 0f
        lowRateInfo.currencyCode = "USD"
        lowRateInfo.loyaltyInfo = LoyaltyInformation(LoyaltyBurnInfo(LoyaltyType.VIP, Money()), LoyaltyEarnInfo(null, null), true)

        val rateInfo = HotelOffersResponse.RateInfo()
        rateInfo.chargeableRateInfo = lowRateInfo
        hotel.rateInfo = rateInfo

        val rooms = ArrayList<HotelOffersResponse.HotelRoomResponse>();
        rooms.add(hotel)

        offers.hotelRoomResponse = rooms

        vm.hotelOffersSubject.onNext(offers)

        assertEquals(View.VISIBLE, hotelDetailView.hotelMessagingContainer.visibility)
        assertEquals(View.GONE, hotelDetailView.discountPercentage.visibility)
        assertEquals(View.VISIBLE, hotelDetailView.vipAccessMessageContainer.visibility)
        assertEquals(View.VISIBLE, hotelDetailView.vipLoyaltyMessage.visibility)
        assertEquals(View.VISIBLE, hotelDetailView.roomRateHeader.visibility)
        assertEquals(View.VISIBLE, hotelDetailView.roomRateVIPLoyaltyAppliedContainer.visibility)
        assertEquals("", hotelDetailView.promoMessage.text)
    }

    @Test
    fun testOnlyLoyaltyAppliedWithNoVipAccess() {
        val hotel = makeHotel()
        hotel.currentAllotment = "0"
        offers.isVipAccess = false
        offers.doesAnyHotelRateOfAnyRoomHaveLoyaltyInfo = true
        hotel.isSameDayDRR = false

        val lowRateInfo = HotelRate()
        lowRateInfo.discountPercent = 0f
        lowRateInfo.currencyCode = "USD"
        lowRateInfo.loyaltyInfo = LoyaltyInformation(LoyaltyBurnInfo(LoyaltyType.VIP, Money()), LoyaltyEarnInfo(null, null), true)

        val rateInfo = HotelOffersResponse.RateInfo()
        rateInfo.chargeableRateInfo = lowRateInfo
        hotel.rateInfo = rateInfo

        val rooms = ArrayList<HotelOffersResponse.HotelRoomResponse>();
        rooms.add(hotel)

        offers.hotelRoomResponse = rooms

        vm.hotelOffersSubject.onNext(offers)

        assertEquals(View.VISIBLE, hotelDetailView.hotelMessagingContainer.visibility)
        assertEquals(View.GONE, hotelDetailView.discountPercentage.visibility)
        assertEquals(View.GONE, hotelDetailView.vipAccessMessageContainer.visibility)
        assertEquals(View.GONE, hotelDetailView.vipLoyaltyMessage.visibility)
        assertEquals(View.VISIBLE, hotelDetailView.regularLoyaltyMessage.visibility)
        assertEquals(View.VISIBLE, hotelDetailView.roomRateHeader.visibility)
        assertEquals(View.VISIBLE, hotelDetailView.roomRateRegularLoyaltyAppliedView.visibility)
        assertEquals("", hotelDetailView.promoMessage.text)
    }

    @Test
    fun testOnlyMobileExclusive() {
        val hotel = makeHotel()
        hotel.currentAllotment = "0"
        offers.isVipAccess = false
        hotel.isSameDayDRR = false
        hotel.isDiscountRestrictedToCurrentSourceType = true

        val lowRateInfo = HotelRate()
        lowRateInfo.discountPercent = 0f
        lowRateInfo.currencyCode = "USD"

        val rateInfo = HotelOffersResponse.RateInfo()
        rateInfo.chargeableRateInfo = lowRateInfo
        hotel.rateInfo = rateInfo

        val rooms = ArrayList<HotelOffersResponse.HotelRoomResponse>();
        rooms.add(hotel)

        offers.hotelRoomResponse = rooms

        vm.hotelOffersSubject.onNext(offers)

        assertEquals(View.VISIBLE, hotelDetailView.hotelMessagingContainer.visibility)
        assertEquals(View.GONE, hotelDetailView.discountPercentage.visibility)
        assertEquals(View.GONE, hotelDetailView.vipAccessMessageContainer.visibility)
        assertEquals(View.VISIBLE, hotelDetailView.promoMessage.visibility)
        assertEquals(activity.resources.getString(R.string.mobile_exclusive), hotelDetailView.promoMessage.text)
    }

    @Test
    fun testRoomsLeftOnly() {
        val hotel = makeHotel()
        offers.isVipAccess = false
        hotel.isSameDayDRR = true
        hotel.isDiscountRestrictedToCurrentSourceType = true

        val lowRateInfo = HotelRate()
        lowRateInfo.discountPercent = 0f
        lowRateInfo.currencyCode = "USD"

        val rateInfo = HotelOffersResponse.RateInfo()
        rateInfo.chargeableRateInfo = lowRateInfo
        hotel.rateInfo = rateInfo

        val rooms = ArrayList<HotelOffersResponse.HotelRoomResponse>();
        rooms.add(hotel)

        offers.hotelRoomResponse = rooms

        vm.hotelOffersSubject.onNext(offers)

        val roomsLeft = hotel.currentAllotment.toInt()
        assertEquals(View.VISIBLE, hotelDetailView.hotelMessagingContainer.visibility)
        assertEquals(View.GONE, hotelDetailView.discountPercentage.visibility)
        assertEquals(View.GONE, hotelDetailView.vipAccessMessageContainer.visibility)
        assertEquals(View.VISIBLE, hotelDetailView.promoMessage.visibility)
        assertEquals(activity.resources.getQuantityString(R.plurals.num_rooms_left, roomsLeft,
                roomsLeft), hotelDetailView.promoMessage.text)
    }

    @Test
    fun testStrikethroughPriceAvailable() {
        val hotel = makeHotel()
        offers.isVipAccess = false
        hotel.isSameDayDRR = true
        hotel.isDiscountRestrictedToCurrentSourceType = true

        val lowRateInfo = HotelRate()
        lowRateInfo.discountPercent = -20f
        lowRateInfo.strikethroughPriceToShowUsers = 100f
        lowRateInfo.currencyCode = "USD"

        val rateInfo = HotelOffersResponse.RateInfo()
        rateInfo.chargeableRateInfo = lowRateInfo
        hotel.rateInfo = rateInfo

        val rooms = ArrayList<HotelOffersResponse.HotelRoomResponse>();
        rooms.add(hotel)

        offers.hotelRoomResponse = rooms

        vm.hotelOffersSubject.onNext(offers)

        assertEquals(View.VISIBLE, hotelDetailView.strikeThroughPrice.visibility)
        assertEquals("$100", hotelDetailView.strikeThroughPrice.text.toString())
    }

    @Test
    fun testStrikethroughPriceNotAvailable() {
        val hotel = makeHotel()
        offers.isVipAccess = false
        hotel.isSameDayDRR = true
        hotel.isDiscountRestrictedToCurrentSourceType = true

        val lowRateInfo = HotelRate()
        lowRateInfo.discountPercent = 0f
        lowRateInfo.currencyCode = "USD"

        val rateInfo = HotelOffersResponse.RateInfo()
        rateInfo.chargeableRateInfo = lowRateInfo
        hotel.rateInfo = rateInfo

        val rooms = ArrayList<HotelOffersResponse.HotelRoomResponse>();
        rooms.add(hotel)

        offers.hotelRoomResponse = rooms

        vm.hotelOffersSubject.onNext(offers)

        assertEquals(View.GONE, hotelDetailView.strikeThroughPrice.visibility)
    }

    @Test
    fun testResortFeeIncludedInPrice() {
        val hotel = makeHotel()

        val lowRateInfo = HotelRate()
        lowRateInfo.totalMandatoryFees = 12.5f
        lowRateInfo.discountPercent = 0f
        lowRateInfo.currencyCode = "USD"
        lowRateInfo.resortFeeInclusion = true
        lowRateInfo.showResortFeeMessage = true

        val rateInfo = HotelOffersResponse.RateInfo()
        rateInfo.chargeableRateInfo = lowRateInfo
        hotel.rateInfo = rateInfo

        val rooms = ArrayList<HotelOffersResponse.HotelRoomResponse>();
        rooms.add(hotel)

        offers.hotelRoomResponse = rooms

        givenHotelSearchParams()

        vm.hotelOffersSubject.onNext(offers)
        vm.addViewsAfterTransition()

        assertEquals("Included in the price", vm.hotelResortFeeIncludedTextObservable.value)
        assertEquals("$12.50", vm.hotelResortFeeObservable.value)
    }

    @Test
    fun testResortFeeNotIncludedInPrice() {
        val hotel = makeHotel()

        val lowRateInfo = HotelRate()
        lowRateInfo.totalMandatoryFees = 12.5f
        lowRateInfo.discountPercent = 0f
        lowRateInfo.currencyCode = "USD"
        lowRateInfo.resortFeeInclusion = false
        lowRateInfo.showResortFeeMessage = true

        val rateInfo = HotelOffersResponse.RateInfo()
        rateInfo.chargeableRateInfo = lowRateInfo
        hotel.rateInfo = rateInfo

        val rooms = ArrayList<HotelOffersResponse.HotelRoomResponse>();
        rooms.add(hotel)

        offers.hotelRoomResponse = rooms

        givenHotelSearchParams()

        vm.hotelOffersSubject.onNext(offers)
        vm.addViewsAfterTransition()

        assertEquals("Not included in the price", vm.hotelResortFeeIncludedTextObservable.value)
        assertEquals("$12.50", vm.hotelResortFeeObservable.value)
    }

    @Test
    fun testNoResortFee() {
        val hotel = makeHotel()

        val lowRateInfo = HotelRate()
        lowRateInfo.totalMandatoryFees = 12.5f
        lowRateInfo.discountPercent = 0f
        lowRateInfo.currencyCode = "USD"
        lowRateInfo.showResortFeeMessage = false

        val rateInfo = HotelOffersResponse.RateInfo()
        rateInfo.chargeableRateInfo = lowRateInfo
        hotel.rateInfo = rateInfo

        val rooms = ArrayList<HotelOffersResponse.HotelRoomResponse>();
        rooms.add(hotel)

        offers.hotelRoomResponse = rooms

        givenHotelSearchParams()

        vm.hotelOffersSubject.onNext(offers)
        vm.addViewsAfterTransition()

        assertNull(vm.hotelResortFeeIncludedTextObservable.value)
        assertNull(vm.hotelResortFeeObservable.value)
    }

    @Test
    fun testNotSoldOutVisibility() {
        val hotel = makeHotel()

        val lowRateInfo = HotelRate()
        lowRateInfo.discountPercent = -20f
        lowRateInfo.strikethroughPriceToShowUsers = 100f
        lowRateInfo.currencyCode = "USD"

        val rateInfo = HotelOffersResponse.RateInfo()
        rateInfo.chargeableRateInfo = lowRateInfo
        hotel.rateInfo = rateInfo

        val rooms = ArrayList<HotelOffersResponse.HotelRoomResponse>();
        rooms.add(hotel)

        offers.hotelRoomResponse = rooms

        givenHotelSearchParams()

        vm.hotelOffersSubject.onNext(offers)
        vm.addViewsAfterTransition()

        testDefaultDetailView()
    }

    @Test
    fun testSoldOutReverseVisibility() {
        val hotel = makeHotel()

        val lowRateInfo = HotelRate()
        lowRateInfo.totalMandatoryFees = 12.5f
        lowRateInfo.discountPercent = -20f
        lowRateInfo.currencyCode = "USD"
        lowRateInfo.showResortFeeMessage = false
        lowRateInfo.priceToShowUsers = 200f
        lowRateInfo.strikethroughPriceToShowUsers = 210f

        val rateInfo = HotelOffersResponse.RateInfo()
        rateInfo.chargeableRateInfo = lowRateInfo
        hotel.rateInfo = rateInfo

        val rooms = ArrayList<HotelOffersResponse.HotelRoomResponse>();
        rooms.add(hotel)

        offers.hotelRoomResponse = rooms

        givenHotelSearchParams()

        vm.hotelOffersSubject.onNext(offers)
        vm.addViewsAfterTransition()

        vm.hotelSoldOut.onNext(true)

        vm.hotelSoldOut.onNext(false)

        testDefaultDetailView()
    }

    @Test
    fun testSoldOutVisibility() {

        val hotel = makeHotel()

        val lowRateInfo = HotelRate()
        lowRateInfo.discountPercent = -20f
        lowRateInfo.currencyCode = "USD"

        val rateInfo = HotelOffersResponse.RateInfo()
        rateInfo.chargeableRateInfo = lowRateInfo
        hotel.rateInfo = rateInfo

        val rooms = ArrayList<HotelOffersResponse.HotelRoomResponse>();
        rooms.add(hotel)

        offers.hotelRoomResponse = rooms

        givenHotelSearchParams()

        vm.hotelOffersSubject.onNext(offers)
        vm.addViewsAfterTransition()

        vm.hotelSoldOut.onNext(true)

        Assert.assertEquals(View.VISIBLE, hotelDetailView.changeDatesButton.visibility)
        Assert.assertEquals(View.VISIBLE, hotelDetailView.detailsSoldOut.visibility)
        Assert.assertEquals(activity.resources.getColor(android.R.color.white), hotelDetailView.hotelDetailsToolbar.toolBarRating.getStarColor())
        Assert.assertEquals(View.GONE, hotelDetailView.selectRoomButton.visibility)
        Assert.assertEquals(View.GONE, hotelDetailView.roomContainer.visibility)
        Assert.assertEquals(View.GONE, hotelDetailView.price.visibility)
        Assert.assertEquals(View.GONE, hotelDetailView.strikeThroughPrice.visibility)
        Assert.assertEquals(View.GONE, hotelDetailView.payByPhoneContainer.visibility)
        Assert.assertEquals(View.GONE, hotelDetailView.hotelMessagingContainer.visibility)
        Assert.assertEquals(View.GONE, hotelDetailView.etpContainer.visibility)
        Assert.assertEquals(View.GONE, hotelDetailView.etpAndFreeCancellationMessagingContainer.visibility)
        Assert.assertEquals(View.GONE, hotelDetailView.stickySelectRoomContainer.visibility)

    }

    private fun testDefaultDetailView() {
        Assert.assertEquals(View.GONE, hotelDetailView.changeDatesButton.visibility)
        Assert.assertEquals(View.GONE, hotelDetailView.detailsSoldOut.visibility)
        Assert.assertEquals(activity.resources.getColor(R.color.hotelsv2_detail_star_color), hotelDetailView.hotelDetailsToolbar.toolBarRating.getStarColor())
        Assert.assertEquals(View.VISIBLE, hotelDetailView.selectRoomButton.visibility)
        Assert.assertEquals(View.VISIBLE, hotelDetailView.roomContainer.visibility)
        Assert.assertEquals(View.VISIBLE, hotelDetailView.price.visibility)
        Assert.assertEquals(View.VISIBLE, hotelDetailView.stickySelectRoomContainer.visibility)

        Assert.assertEquals(View.VISIBLE, hotelDetailView.strikeThroughPrice.visibility)
        Assert.assertEquals(View.VISIBLE, hotelDetailView.payByPhoneContainer.visibility)
        Assert.assertEquals(View.VISIBLE, hotelDetailView.hotelMessagingContainer.visibility)
        Assert.assertEquals(View.VISIBLE, hotelDetailView.etpContainer.visibility)
        Assert.assertEquals(View.VISIBLE, hotelDetailView.etpAndFreeCancellationMessagingContainer.visibility)

    }

    private fun givenHotelSearchParams() {
        checkIn = LocalDate.now();
        checkOut = checkIn.plusDays(1)
        val suggestion = SuggestionV4()
        suggestion.gaiaId = ""
        searchParams = HotelSearchParams.Builder(activity.resources.getInteger(R.integer.calendar_max_days_hotel_stay))
                .departure(suggestion)
                .adults(2)
                .children(listOf(10,10,10))
                .startDate(checkIn)
                .endDate(checkOut).build() as HotelSearchParams
        vm.paramsSubject.onNext(searchParams)
    }

    private fun makeHotel() : HotelOffersResponse.HotelRoomResponse {
        val hotel = HotelOffersResponse.HotelRoomResponse()
        val valueAdds = ArrayList<HotelOffersResponse.ValueAdds>()
        val valueAdd = HotelOffersResponse.ValueAdds()
        valueAdd.description = "Value Add"
        valueAdds.add(valueAdd)
        hotel.valueAdds = valueAdds

        val bedTypes = ArrayList<HotelOffersResponse.BedTypes>()
        val bedType = HotelOffersResponse.BedTypes()
        bedType.id = "1"
        bedType.description = "King Bed"
        bedTypes.add(bedType)
        hotel.bedTypes = bedTypes

        hotel.currentAllotment = "1"
        hotel.payLaterOffer = hotel

        return hotel
    }
}
