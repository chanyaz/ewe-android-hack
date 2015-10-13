package com.expedia.bookings.test

import android.app.Activity
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.SuggestionV4
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.widget.HotelDetailView
import com.expedia.util.endlessObserver
import com.expedia.vm.HotelDetailViewModel
import com.squareup.phrase.Phrase
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import java.util.ArrayList
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
public class HotelDetailsTest {
    public var service: HotelServicesRule = HotelServicesRule()
        @Rule get

    private var vm: HotelDetailViewModel by Delegates.notNull()
    private var hotelDetailView: HotelDetailView by Delegates.notNull()
    private var activity: Activity by Delegates.notNull()
    private var offers : HotelOffersResponse by Delegates.notNull()

    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        hotelDetailView = android.view.LayoutInflater.from(activity).inflate(R.layout.test_hotel_details_widget, null) as HotelDetailView
        vm = HotelDetailViewModel(activity.applicationContext, service.hotelServices(), endlessObserver { /*ignore*/ })
        hotelDetailView.viewmodel = vm

        offers = HotelOffersResponse()
        offers.hotelGuestRating = 5.0
        offers.hotelStarRating = 5.0
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
        return hotel
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

        assertEquals(View.VISIBLE, hotelDetailView.hotelMessagingContainer.getVisibility())
        val expectedDiscountMessage = Phrase.from(activity.getResources(), R.string.hotel_discount_percent_Template).put("discount", -20).format().toString()
        assertEquals(expectedDiscountMessage, hotelDetailView.discountPercentage.getText())
        assertEquals(View.VISIBLE, hotelDetailView.discountPercentage.getVisibility())
        assertEquals(View.VISIBLE, hotelDetailView.vipAccessMessage.getVisibility())
        assertEquals(View.VISIBLE, hotelDetailView.promoMessage.getVisibility())
        assertEquals(activity.getResources().getString(R.string.tonight_only), hotelDetailView.promoMessage.getText())
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

        val checkIn = LocalDate.now();
        val checkOut = checkIn.plusDays(1)
        val suggestion = SuggestionV4()
        suggestion.gaiaId = ""
        val searchBuilder = HotelSearchParams.Builder()
                .suggestion(suggestion)
                .adults(2)
                .children(listOf(10,10,10))
                .checkIn(checkIn)
                .checkOut(checkOut)

        val searchParams = searchBuilder.build();

        vm.hotelOffersSubject.onNext(offers)
        vm.paramsSubject.onNext(searchParams)

        val string = Phrase.from(activity, R.string.calendar_instructions_date_range_with_guests_TEMPLATE).put("startdate",
                DateUtils.localDateToMMMd(checkIn)).put("enddate",
                DateUtils.localDateToMMMd(checkOut)).put("guests",
                activity.resources.getQuantityString(R.plurals.number_of_guests, searchParams.children.size() + searchParams.adults, searchParams.children.size() + searchParams.adults))
                .format()
                .toString()

        assertEquals("$300", hotelDetailView.price.getText())

        assertEquals(string, hotelDetailView.searchInfo.getText())
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

        assertEquals(View.VISIBLE, hotelDetailView.hotelMessagingContainer.getVisibility())
        val expectedDiscountMessage = Phrase.from(activity.getResources(), R.string.hotel_discount_percent_Template).put("discount", -20).format().toString()
        assertEquals(expectedDiscountMessage, hotelDetailView.discountPercentage.getText())
        assertEquals(View.VISIBLE, hotelDetailView.discountPercentage.getVisibility())
        assertEquals(View.GONE, hotelDetailView.vipAccessMessage.getVisibility())
        assertEquals("", hotelDetailView.promoMessage.getText())
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

        assertEquals(View.VISIBLE, hotelDetailView.hotelMessagingContainer.getVisibility())
        assertEquals(View.GONE, hotelDetailView.discountPercentage.getVisibility())
        assertEquals(View.VISIBLE, hotelDetailView.vipAccessMessage.getVisibility())
        assertEquals("", hotelDetailView.promoMessage.getText())
    }

    @Test
    fun testOnlyMobileExclusive() {
        var hotel = makeHotel()
        hotel.currentAllotment = "0"
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

        assertEquals(View.VISIBLE, hotelDetailView.hotelMessagingContainer.getVisibility())
        assertEquals(View.GONE, hotelDetailView.discountPercentage.getVisibility())
        assertEquals(View.GONE, hotelDetailView.vipAccessMessage.getVisibility())
        assertEquals(View.VISIBLE, hotelDetailView.promoMessage.getVisibility())
        assertEquals(activity.getResources().getString(R.string.mobile_exclusive), hotelDetailView.promoMessage.getText())
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
        assertEquals(View.VISIBLE, hotelDetailView.hotelMessagingContainer.getVisibility())
        assertEquals(View.GONE, hotelDetailView.discountPercentage.getVisibility())
        assertEquals(View.GONE, hotelDetailView.vipAccessMessage.getVisibility())
        assertEquals(View.VISIBLE, hotelDetailView.promoMessage.getVisibility())
        assertEquals(activity.getResources().getQuantityString(R.plurals.num_rooms_left, roomsLeft,
                roomsLeft), hotelDetailView.promoMessage.getText())
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

        assertEquals(View.VISIBLE, hotelDetailView.strikeThroughPrice.getVisibility())
        assertEquals("$100", hotelDetailView.strikeThroughPrice.text)
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

        assertEquals(View.GONE, hotelDetailView.strikeThroughPrice.getVisibility())
    }
}
