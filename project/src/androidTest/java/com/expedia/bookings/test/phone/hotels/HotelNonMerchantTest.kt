package com.expedia.bookings.test.phone.hotels

import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.withText
import com.expedia.bookings.test.espresso.Common
import com.expedia.bookings.test.espresso.HotelTestCase
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen
import org.junit.Test

class HotelNonMerchantTest: HotelTestCase() {

    @Test
    fun testNonMerchantHotel() {
        SearchScreen.doGenericHotelSearch()
        // Check to make sure non merchant shows up in result list
        HotelScreen.selectHotel("Non Merchant Hotel")
        Common.delay(1)

        HotelScreen.selectRoomButton().perform(click())
        Common.delay(1)
        // Check bed type and free cancellation
        HotelScreen.expandedBedType().check(matches(withText("1 bed")))
        HotelScreen.expandedFreeCancellation().check(matches(withText("Free Cancellation")))
        // Check other bed type and non refundable room
        HotelScreen.viewRoom("One Bed in 6-Bed Mixed Sex Dormitory").perform(click())
        Common.delay(1)
        HotelScreen.expandedBedType().check(matches(withText("1 bunk bed")))
        HotelScreen.expandedFreeCancellation().check(matches(withText("Non-refundable")))
    }
}
