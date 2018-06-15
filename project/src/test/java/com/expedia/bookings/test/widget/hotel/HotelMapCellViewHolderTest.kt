package com.expedia.bookings.test.widget.hotel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.hotel.widget.HotelMapCellViewHolder
import com.expedia.bookings.packages.vm.PackageHotelViewModel
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.util.LoyaltyUtil
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelMapCellViewHolderTest {

    private val context = RuntimeEnvironment.application
    private lateinit var hotelCellView: ViewGroup

    @Before
    fun before() {
        hotelCellView = LayoutInflater.from(context).inflate(R.layout.horizontal_hotel_cell, null, false) as ViewGroup
    }

    @Test
    fun testCreateHotelViewModel() {
        val hotel = makeHotel()
        val viewHolder = HotelMapCellViewHolder(hotelCellView, false)
        viewHolder.bindHotelData(hotel)
        if (LoyaltyUtil.isEarnMessageEnabled(hotel.isPackage)) {
            assertEquals(View.INVISIBLE, viewHolder.viewModel.earnMessageVisibility)
        } else {
            assertEquals(View.GONE, viewHolder.viewModel.earnMessageVisibility)
        }
    }

    @Test
    fun testCreateHotelViewModelPackage() {
        val hotel = makeHotel()
        hotel.isPackage = true
        val viewHolder = HotelMapCellViewHolder(hotelCellView, true)
        viewHolder.bindHotelData(hotel)
        assertTrue(viewHolder.viewModel is PackageHotelViewModel)
        if (LoyaltyUtil.isEarnMessageEnabled(hotel.isPackage)) {
            assertEquals(View.INVISIBLE, viewHolder.viewModel.earnMessageVisibility)
        } else {
            assertEquals(View.GONE, viewHolder.viewModel.earnMessageVisibility)
        }
    }

    private fun makeHotel(): Hotel {
        val hotel = Hotel()
        hotel.hotelId = "happy"
        hotel.localizedName = "happy hotel"
        hotel.lowRateInfo = HotelRate()
        hotel.lowRateInfo.currencyCode = "USD"
        return hotel
    }
}
