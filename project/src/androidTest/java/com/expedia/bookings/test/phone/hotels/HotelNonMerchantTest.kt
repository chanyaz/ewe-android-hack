package com.expedia.bookings.test.phone.hotels

import android.support.test.espresso.action.ViewActions.scrollTo
import android.support.test.espresso.action.ViewActions.swipeUp
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.withText
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.HotelTestCase
import com.expedia.bookings.test.pagemodels.common.SearchScreen
import com.expedia.bookings.test.pagemodels.hotels.HotelInfoSiteScreen
import com.expedia.bookings.test.pagemodels.hotels.HotelScreen
import org.junit.Test

class HotelNonMerchantTest : HotelTestCase() {

    @Test
    fun testNonMerchantHotel() {
        SearchScreen.doGenericHotelSearch()
        // Check to make sure non merchant shows up in result list
        HotelScreen.selectHotel("Non Merchant Hotel")
        Common.delay(1)

        HotelInfoSiteScreen.clickStickySelectRoom()
        Common.delay(1)
        var roomType = "One Bed in 10-Bed Mixed Sex Dormitory"
        // Check bed type and free cancellation
        HotelInfoSiteScreen.roomCardViewForRoomType("One Bed in 6-Bed Mixed Sex Dormitory").perform(scrollTo(), swipeUp())
        HotelInfoSiteScreen.bedTypeViewForRoomType(roomType).check(matches(withText("1 bed")))
        HotelInfoSiteScreen.freeCancellationViewForRoomType(roomType).check(matches(withText("Free cancellation")))
        // Check other bed type and non refundable room
        roomType = "One Bed in 6-Bed Mixed Sex Dormitory"
        HotelInfoSiteScreen.roomCardViewForRoomType(roomType).perform(scrollTo(), swipeUp())
        HotelInfoSiteScreen.bedTypeViewForRoomType(roomType).check(matches(withText("1 bunk bed")))
        HotelInfoSiteScreen.freeCancellationViewForRoomType(roomType).check(matches(withText("Non-refundable")))
    }
}
