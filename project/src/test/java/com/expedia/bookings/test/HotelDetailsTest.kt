package com.expedia.bookings.test

import android.app.Activity
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.widget.HotelDetailView
import com.expedia.util.endlessObserver
import com.expedia.vm.HotelDetailViewModel
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
    var service = ServicesRule(HotelServices::class.java)
        @Rule get

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
        vm = HotelDetailViewModel(activity.applicationContext, service.services, endlessObserver { /*ignore*/ })
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
    fun testDiscountPercentageVipAccessTonightOnly() {
        var hotel = makeHotel()
        hotel.currentAllotment = "0"
        offers.isVipAccess = true
        hotel.isSameDayDRR = true

        var lowRateInfo = HotelRate()
        lowRateInfo.discountPercent = -20f
        lowRateInfo.currencyCode = "USD"

        var rateInfo = HotelOffersResponse.RateInfo()
        rateInfo.chargeableRateInfo = lowRateInfo
        hotel.rateInfo = rateInfo

        var rooms = ArrayList<HotelOffersResponse.HotelRoomResponse>();
        rooms.add(hotel)

        offers.hotelRoomResponse = rooms

        vm.hotelOffersSubject.onNext(offers)

        assertEquals(View.VISIBLE, hotelDetailView.hotelMessagingContainer.visibility)
        val expectedDiscountMessage = Phrase.from(activity.resources, R.string.hotel_discount_percent_Template).put("discount", -20).format().toString()
        assertEquals(expectedDiscountMessage, hotelDetailView.discountPercentage.text)
        assertEquals(View.VISIBLE, hotelDetailView.discountPercentage.visibility)
        assertEquals(View.VISIBLE, hotelDetailView.vipAccessMessage.visibility)
        assertEquals(View.VISIBLE, hotelDetailView.promoMessage.visibility)
        assertEquals(activity.resources.getString(R.string.tonight_only), hotelDetailView.promoMessage.text)
    }

    @Test
    fun testNightlyPriceGuestCount() {
        var hotel = makeHotel()
        var lowRateInfo = HotelRate()
        lowRateInfo.averageRate = 300f
        lowRateInfo.discountPercent = -20f
        lowRateInfo.currencyCode = "USD"

        var rateInfo = HotelOffersResponse.RateInfo()
        rateInfo.chargeableRateInfo = lowRateInfo
        hotel.rateInfo = rateInfo

        var rooms = ArrayList<HotelOffersResponse.HotelRoomResponse>();
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
        var hotel = makeHotel()
        hotel.currentAllotment = "0"
        offers.isVipAccess = false
        hotel.isSameDayDRR = false

        var lowRateInfo = HotelRate()
        lowRateInfo.discountPercent = -20f
        lowRateInfo.currencyCode = "USD"

        var rateInfo = HotelOffersResponse.RateInfo()
        rateInfo.chargeableRateInfo = lowRateInfo
        hotel.rateInfo = rateInfo

        var rooms = ArrayList<HotelOffersResponse.HotelRoomResponse>();
        rooms.add(hotel)

        offers.hotelRoomResponse = rooms

        vm.hotelOffersSubject.onNext(offers)

        assertEquals(View.VISIBLE, hotelDetailView.hotelMessagingContainer.visibility)
        val expectedDiscountMessage = Phrase.from(activity.resources, R.string.hotel_discount_percent_Template).put("discount", -20).format().toString()
        assertEquals(expectedDiscountMessage, hotelDetailView.discountPercentage.text)
        assertEquals(View.VISIBLE, hotelDetailView.discountPercentage.visibility)
        assertEquals(View.GONE, hotelDetailView.vipAccessMessage.visibility)
        assertEquals("", hotelDetailView.promoMessage.text)
    }

    @Test
    fun testOnlyVipAccess() {
        var hotel = makeHotel()
        hotel.currentAllotment = "0"
        offers.isVipAccess = true
        hotel.isSameDayDRR = false

        var lowRateInfo = HotelRate()
        lowRateInfo.discountPercent = 0f
        lowRateInfo.currencyCode = "USD"

        var rateInfo = HotelOffersResponse.RateInfo()
        rateInfo.chargeableRateInfo = lowRateInfo
        hotel.rateInfo = rateInfo

        var rooms = ArrayList<HotelOffersResponse.HotelRoomResponse>();
        rooms.add(hotel)

        offers.hotelRoomResponse = rooms

        vm.hotelOffersSubject.onNext(offers)

        assertEquals(View.VISIBLE, hotelDetailView.hotelMessagingContainer.visibility)
        assertEquals(View.GONE, hotelDetailView.discountPercentage.visibility)
        assertEquals(View.VISIBLE, hotelDetailView.vipAccessMessage.visibility)
        assertEquals("", hotelDetailView.promoMessage.text)
    }

    @Test
    fun testOnlyMobileExclusive() {
        var hotel = makeHotel()
        hotel.currentAllotment = "0"
        offers.isVipAccess = false
        hotel.isSameDayDRR = false
        hotel.isDiscountRestrictedToCurrentSourceType = true

        var lowRateInfo = HotelRate()
        lowRateInfo.discountPercent = 0f
        lowRateInfo.currencyCode = "USD"

        var rateInfo = HotelOffersResponse.RateInfo()
        rateInfo.chargeableRateInfo = lowRateInfo
        hotel.rateInfo = rateInfo

        var rooms = ArrayList<HotelOffersResponse.HotelRoomResponse>();
        rooms.add(hotel)

        offers.hotelRoomResponse = rooms

        vm.hotelOffersSubject.onNext(offers)

        assertEquals(View.VISIBLE, hotelDetailView.hotelMessagingContainer.visibility)
        assertEquals(View.GONE, hotelDetailView.discountPercentage.visibility)
        assertEquals(View.GONE, hotelDetailView.vipAccessMessage.visibility)
        assertEquals(View.VISIBLE, hotelDetailView.promoMessage.visibility)
        assertEquals(activity.resources.getString(R.string.mobile_exclusive), hotelDetailView.promoMessage.text)
    }

    @Test
    fun testRoomsLeftOnly() {
        var hotel = makeHotel()
        offers.isVipAccess = false
        hotel.isSameDayDRR = true
        hotel.isDiscountRestrictedToCurrentSourceType = true

        var lowRateInfo = HotelRate()
        lowRateInfo.discountPercent = 0f
        lowRateInfo.currencyCode = "USD"

        var rateInfo = HotelOffersResponse.RateInfo()
        rateInfo.chargeableRateInfo = lowRateInfo
        hotel.rateInfo = rateInfo

        var rooms = ArrayList<HotelOffersResponse.HotelRoomResponse>();
        rooms.add(hotel)

        offers.hotelRoomResponse = rooms

        vm.hotelOffersSubject.onNext(offers)

        var roomsLeft = hotel.currentAllotment.toInt()
        assertEquals(View.VISIBLE, hotelDetailView.hotelMessagingContainer.visibility)
        assertEquals(View.GONE, hotelDetailView.discountPercentage.visibility)
        assertEquals(View.GONE, hotelDetailView.vipAccessMessage.visibility)
        assertEquals(View.VISIBLE, hotelDetailView.promoMessage.visibility)
        assertEquals(activity.resources.getQuantityString(R.plurals.num_rooms_left, roomsLeft,
                roomsLeft), hotelDetailView.promoMessage.text)
    }

    @Test
    fun testStrikethroughPriceAvailable() {
        var hotel = makeHotel()
        offers.isVipAccess = false
        hotel.isSameDayDRR = true
        hotel.isDiscountRestrictedToCurrentSourceType = true

        var lowRateInfo = HotelRate()
        lowRateInfo.discountPercent = -20f
        lowRateInfo.strikethroughPriceToShowUsers = 100f
        lowRateInfo.currencyCode = "USD"

        var rateInfo = HotelOffersResponse.RateInfo()
        rateInfo.chargeableRateInfo = lowRateInfo
        hotel.rateInfo = rateInfo

        var rooms = ArrayList<HotelOffersResponse.HotelRoomResponse>();
        rooms.add(hotel)

        offers.hotelRoomResponse = rooms

        vm.hotelOffersSubject.onNext(offers)

        assertEquals(View.VISIBLE, hotelDetailView.strikeThroughPrice.visibility)
        assertEquals("$100", hotelDetailView.strikeThroughPrice.text.toString())
    }

    @Test
    fun testStrikethroughPriceNotAvailable() {
        var hotel = makeHotel()
        offers.isVipAccess = false
        hotel.isSameDayDRR = true
        hotel.isDiscountRestrictedToCurrentSourceType = true

        var lowRateInfo = HotelRate()
        lowRateInfo.discountPercent = 0f
        lowRateInfo.currencyCode = "USD"

        var rateInfo = HotelOffersResponse.RateInfo()
        rateInfo.chargeableRateInfo = lowRateInfo
        hotel.rateInfo = rateInfo

        var rooms = ArrayList<HotelOffersResponse.HotelRoomResponse>();
        rooms.add(hotel)

        offers.hotelRoomResponse = rooms

        vm.hotelOffersSubject.onNext(offers)

        assertEquals(View.GONE, hotelDetailView.strikeThroughPrice.visibility)
    }

    @Test
    fun testResortFeeIncludedInPrice() {
        var hotel = makeHotel()

        var lowRateInfo = HotelRate()
        lowRateInfo.totalMandatoryFees = 12.5f
        lowRateInfo.discountPercent = 0f
        lowRateInfo.currencyCode = "USD"
        lowRateInfo.resortFeeInclusion = true
        lowRateInfo.showResortFeeMessage = true

        var rateInfo = HotelOffersResponse.RateInfo()
        rateInfo.chargeableRateInfo = lowRateInfo
        hotel.rateInfo = rateInfo

        var rooms = ArrayList<HotelOffersResponse.HotelRoomResponse>();
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
        var hotel = makeHotel()

        var lowRateInfo = HotelRate()
        lowRateInfo.totalMandatoryFees = 12.5f
        lowRateInfo.discountPercent = 0f
        lowRateInfo.currencyCode = "USD"
        lowRateInfo.resortFeeInclusion = false
        lowRateInfo.showResortFeeMessage = true

        var rateInfo = HotelOffersResponse.RateInfo()
        rateInfo.chargeableRateInfo = lowRateInfo
        hotel.rateInfo = rateInfo

        var rooms = ArrayList<HotelOffersResponse.HotelRoomResponse>();
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
        var hotel = makeHotel()

        var lowRateInfo = HotelRate()
        lowRateInfo.totalMandatoryFees = 12.5f
        lowRateInfo.discountPercent = 0f
        lowRateInfo.currencyCode = "USD"
        lowRateInfo.showResortFeeMessage = false

        var rateInfo = HotelOffersResponse.RateInfo()
        rateInfo.chargeableRateInfo = lowRateInfo
        hotel.rateInfo = rateInfo

        var rooms = ArrayList<HotelOffersResponse.HotelRoomResponse>();
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
        var hotel = makeHotel()

        var lowRateInfo = HotelRate()
        lowRateInfo.discountPercent = -20f
        lowRateInfo.strikethroughPriceToShowUsers = 100f
        lowRateInfo.currencyCode = "USD"

        var rateInfo = HotelOffersResponse.RateInfo()
        rateInfo.chargeableRateInfo = lowRateInfo
        hotel.rateInfo = rateInfo

        var rooms = ArrayList<HotelOffersResponse.HotelRoomResponse>();
        rooms.add(hotel)

        offers.hotelRoomResponse = rooms

        givenHotelSearchParams()

        vm.hotelOffersSubject.onNext(offers)
        vm.addViewsAfterTransition()

        testDefaultDetailView()
    }

    @Test
    fun testSoldOutReverseVisibility() {
        var hotel = makeHotel()

        var lowRateInfo = HotelRate()
        lowRateInfo.totalMandatoryFees = 12.5f
        lowRateInfo.discountPercent = -20f
        lowRateInfo.currencyCode = "USD"
        lowRateInfo.showResortFeeMessage = false

        var rateInfo = HotelOffersResponse.RateInfo()
        rateInfo.chargeableRateInfo = lowRateInfo
        hotel.rateInfo = rateInfo

        var rooms = ArrayList<HotelOffersResponse.HotelRoomResponse>();
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

        var hotel = makeHotel()

        var lowRateInfo = HotelRate()
        lowRateInfo.discountPercent = -20f
        lowRateInfo.currencyCode = "USD"

        var rateInfo = HotelOffersResponse.RateInfo()
        rateInfo.chargeableRateInfo = lowRateInfo
        hotel.rateInfo = rateInfo

        var rooms = ArrayList<HotelOffersResponse.HotelRoomResponse>();
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
                .suggestion(suggestion)
                .adults(2)
                .children(listOf(10,10,10))
                .checkIn(checkIn)
                .checkOut(checkOut).build()
        vm.paramsSubject.onNext(searchParams)
    }

    private fun makeHotel() : HotelOffersResponse.HotelRoomResponse {
        var hotel = HotelOffersResponse.HotelRoomResponse()
        var valueAdds = ArrayList<HotelOffersResponse.ValueAdds>()
        var valueAdd = HotelOffersResponse.ValueAdds()
        valueAdd.description = "Value Add"
        valueAdds.add(valueAdd)
        hotel.valueAdds = valueAdds

        var bedTypes = ArrayList<HotelOffersResponse.BedTypes>()
        var bedType = HotelOffersResponse.BedTypes()
        bedType.id = "1"
        bedType.description = "King Bed"
        bedTypes.add(bedType)
        hotel.bedTypes = bedTypes

        hotel.currentAllotment = "1"
        hotel.payLaterOffer = hotel

        return hotel
    }
}
