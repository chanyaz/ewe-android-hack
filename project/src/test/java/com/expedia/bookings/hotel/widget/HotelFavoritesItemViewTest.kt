package com.expedia.bookings.test.widget.hotel

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.shortlist.HotelShortlistItem
import com.expedia.bookings.data.hotels.shortlist.ShortlistItem
import com.expedia.bookings.data.hotels.shortlist.ShortlistItemMetadata
import com.expedia.bookings.hotel.widget.viewholder.HotelFavoritesItemViewHolder
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.properties.Delegates

@RunWith(RobolectricRunner::class)
class HotelFavoritesItemViewTest {
    private var hotelCellView: ViewGroup by Delegates.notNull()
    private var hotelFavoritesItemViewHolder: HotelFavoritesItemViewHolder by Delegates.notNull()
    private var activity: Activity by Delegates.notNull()

    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Default)
        hotelCellView = LayoutInflater.from(activity).inflate(R.layout.hotel_cell, null, false) as ViewGroup
        hotelFavoritesItemViewHolder = HotelFavoritesItemViewHolder(hotelCellView)
    }

    @Test
    fun testBindHappy() {
        val shortlistItem = makeHotelShortlistItem()
        hotelFavoritesItemViewHolder.bind(shortlistItem)

        Assert.assertEquals(View.VISIBLE, hotelFavoritesItemViewHolder.hotelNameStarAmenityDistance.hotelNameTextView.visibility)
        Assert.assertEquals("happy", hotelFavoritesItemViewHolder.hotelNameStarAmenityDistance.hotelNameTextView.text)

        Assert.assertEquals(View.VISIBLE, hotelFavoritesItemViewHolder.hotelPriceTopAmenity.pricePerNightTextView.visibility)
        Assert.assertEquals("$123", hotelFavoritesItemViewHolder.hotelPriceTopAmenity.pricePerNightTextView.text)

        Assert.assertEquals(View.VISIBLE, hotelFavoritesItemViewHolder.hotelPriceTopAmenity.checkInOutDateTextView.visibility)
        Assert.assertEquals("Oct 11 - Oct 12", hotelFavoritesItemViewHolder.hotelPriceTopAmenity.checkInOutDateTextView.text)

        Assert.assertEquals(View.VISIBLE, hotelFavoritesItemViewHolder.guestRating.visibility)
        Assert.assertEquals(View.VISIBLE, hotelFavoritesItemViewHolder.guestRatingRecommendedText.visibility)
        Assert.assertEquals(View.GONE, hotelFavoritesItemViewHolder.noGuestRating.visibility)
        Assert.assertEquals("4.6", hotelFavoritesItemViewHolder.guestRating.text)
        Assert.assertEquals("/5 - Wonderful!", hotelFavoritesItemViewHolder.guestRatingRecommendedText.text)

        Assert.assertEquals(View.GONE, hotelFavoritesItemViewHolder.hotelPriceTopAmenity.soldOutTextView.visibility)
        Assert.assertEquals(View.GONE, hotelFavoritesItemViewHolder.airAttachContainer.visibility)
        Assert.assertEquals(View.GONE, hotelFavoritesItemViewHolder.airAttachSWPImage.visibility)
        Assert.assertEquals(View.GONE, hotelFavoritesItemViewHolder.vipMessageContainer.visibility)
        Assert.assertEquals(View.GONE, hotelFavoritesItemViewHolder.hotelNameStarAmenityDistance.starRatingBar.visibility)
    }

    @Test
    fun testUpdateCheckInOutDatePassed() {
        val shortlistItem = makeHotelShortlistItem()
        givenHotelCheckInOutDatePassed(shortlistItem)

        hotelFavoritesItemViewHolder.bind(shortlistItem)

        Assert.assertEquals(View.GONE, hotelFavoritesItemViewHolder.hotelPriceTopAmenity.checkInOutDateTextView.visibility)
    }

    @Test
    fun testUpdateCheckInOutDateEmpty() {
        val shortlistItem = makeHotelShortlistItem()
        givenHotelCheckInOutDateEmpty(shortlistItem)

        hotelFavoritesItemViewHolder.bind(shortlistItem)

        Assert.assertEquals(View.GONE, hotelFavoritesItemViewHolder.hotelPriceTopAmenity.checkInOutDateTextView.visibility)
    }

    @Test
    fun testUpdateCheckInOutDateInvalid() {
        val shortlistItem = makeHotelShortlistItem()
        givenHotelCheckInOutDateInvalid(shortlistItem)

        hotelFavoritesItemViewHolder.bind(shortlistItem)

        Assert.assertEquals(View.GONE, hotelFavoritesItemViewHolder.hotelPriceTopAmenity.checkInOutDateTextView.visibility)
    }

    @Test
    fun testUpdateCheckInOutDateNull() {
        val shortlistItem = makeHotelShortlistItem()
        givenHotelCheckInOutDateNull(shortlistItem)

        hotelFavoritesItemViewHolder.bind(shortlistItem)

        Assert.assertEquals(View.GONE, hotelFavoritesItemViewHolder.hotelPriceTopAmenity.checkInOutDateTextView.visibility)
    }

    @Test
    fun testUpdatePricePerNightEmptyPrice() {
        val shortlistItem = makeHotelShortlistItem()
        givenHotelPricePerNightEmpty(shortlistItem)

        hotelFavoritesItemViewHolder.bind(shortlistItem)

        Assert.assertEquals(View.GONE, hotelFavoritesItemViewHolder.hotelPriceTopAmenity.pricePerNightTextView.visibility)
        Assert.assertEquals("", hotelFavoritesItemViewHolder.hotelPriceTopAmenity.pricePerNightTextView.text)
    }

    @Test
    fun testUpdatePricePerNightZero() {
        val shortlistItem = makeHotelShortlistItem()
        givenHotelPricePerNightZero(shortlistItem)

        hotelFavoritesItemViewHolder.bind(shortlistItem)

        Assert.assertEquals(View.GONE, hotelFavoritesItemViewHolder.hotelPriceTopAmenity.pricePerNightTextView.visibility)
        Assert.assertEquals("", hotelFavoritesItemViewHolder.hotelPriceTopAmenity.pricePerNightTextView.text)
    }

    @Test
    fun testUpdatePricePerNightInvalid() {
        val shortlistItem = makeHotelShortlistItem()
        givenHotelPricePerNightInvalid(shortlistItem)

        hotelFavoritesItemViewHolder.bind(shortlistItem)

        Assert.assertEquals(View.GONE, hotelFavoritesItemViewHolder.hotelPriceTopAmenity.pricePerNightTextView.visibility)
        Assert.assertEquals("", hotelFavoritesItemViewHolder.hotelPriceTopAmenity.pricePerNightTextView.text)
    }

    @Test
    fun testUpdatePricePerNightNull() {
        val shortlistItem = makeHotelShortlistItem()
        givenHotelPricePerNightNull(shortlistItem)

        hotelFavoritesItemViewHolder.bind(shortlistItem)

        Assert.assertEquals(View.GONE, hotelFavoritesItemViewHolder.hotelPriceTopAmenity.pricePerNightTextView.visibility)
        Assert.assertEquals("", hotelFavoritesItemViewHolder.hotelPriceTopAmenity.pricePerNightTextView.text)
    }

    @Test
    fun testUpdateHotelGuestRatingInvalid() {
        val shortlistItem = makeHotelShortlistItem()
        givenGuestRatingInvalid(shortlistItem)

        hotelFavoritesItemViewHolder.bind(shortlistItem)

        Assert.assertEquals(View.GONE, hotelFavoritesItemViewHolder.guestRating.visibility)
        Assert.assertEquals(View.GONE, hotelFavoritesItemViewHolder.guestRatingRecommendedText.visibility)
        Assert.assertEquals(View.VISIBLE, hotelFavoritesItemViewHolder.noGuestRating.visibility)
        Assert.assertEquals("", hotelFavoritesItemViewHolder.guestRating.text)
    }

    @Test
    fun testUpdateHotelGuestRatingEmpty() {
        val shortlistItem = makeHotelShortlistItem()
        givenGuestRatingEmpty(shortlistItem)

        hotelFavoritesItemViewHolder.bind(shortlistItem)

        Assert.assertEquals(View.GONE, hotelFavoritesItemViewHolder.guestRating.visibility)
        Assert.assertEquals(View.GONE, hotelFavoritesItemViewHolder.guestRatingRecommendedText.visibility)
        Assert.assertEquals(View.VISIBLE, hotelFavoritesItemViewHolder.noGuestRating.visibility)
        Assert.assertEquals("", hotelFavoritesItemViewHolder.guestRating.text)
    }

    @Test
    fun testUpdateHotelGuestRatingNull() {
        val shortlistItem = makeHotelShortlistItem()
        givenGuestRatingNull(shortlistItem)

        hotelFavoritesItemViewHolder.bind(shortlistItem)

        Assert.assertEquals(View.GONE, hotelFavoritesItemViewHolder.guestRating.visibility)
        Assert.assertEquals(View.GONE, hotelFavoritesItemViewHolder.guestRatingRecommendedText.visibility)
        Assert.assertEquals(View.VISIBLE, hotelFavoritesItemViewHolder.noGuestRating.visibility)
        Assert.assertEquals("", hotelFavoritesItemViewHolder.guestRating.text)
    }

    @Test
    fun testHotelNameEmpty() {
        val shortlistItem = makeHotelShortlistItem()
        givenHotelNameEmpty(shortlistItem)

        hotelFavoritesItemViewHolder.bind(shortlistItem)

        Assert.assertEquals(View.GONE, hotelFavoritesItemViewHolder.hotelNameStarAmenityDistance.hotelNameTextView.visibility)
        Assert.assertEquals("", hotelFavoritesItemViewHolder.hotelNameStarAmenityDistance.hotelNameTextView.text)
    }

    @Test
    fun testHotelNameNull() {
        val shortlistItem = makeHotelShortlistItem()
        givenHotelNameNull(shortlistItem)

        hotelFavoritesItemViewHolder.bind(shortlistItem)

        Assert.assertEquals(View.GONE, hotelFavoritesItemViewHolder.hotelNameStarAmenityDistance.hotelNameTextView.visibility)
        Assert.assertEquals("", hotelFavoritesItemViewHolder.hotelNameStarAmenityDistance.hotelNameTextView.text)
    }

    private fun makeHotelShortlistItem(): HotelShortlistItem {
        val hotelShortlistItem = HotelShortlistItem()

        hotelShortlistItem.name = "happy"
        hotelShortlistItem.guestRating = "4.5833335"
        hotelShortlistItem.price = "123.45"
        hotelShortlistItem.currency = "USD"

        val shortlistItemMetadata = ShortlistItemMetadata()
        shortlistItemMetadata.chkIn = "20181011"
        shortlistItemMetadata.chkOut = "20181012"

        val shortlistItem = ShortlistItem()
        shortlistItem.metaData = shortlistItemMetadata

        hotelShortlistItem.shortlistItem = shortlistItem

        return hotelShortlistItem
    }

    private fun givenHotelCheckInOutDatePassed(hotelShortlistItem: HotelShortlistItem) {
        hotelShortlistItem.shortlistItem!!.metaData!!.chkIn = "20171122"
        hotelShortlistItem.shortlistItem!!.metaData!!.chkOut = "20171123"
    }

    private fun givenHotelCheckInOutDateEmpty(hotelShortlistItem: HotelShortlistItem) {
        hotelShortlistItem.shortlistItem!!.metaData!!.chkIn = ""
        hotelShortlistItem.shortlistItem!!.metaData!!.chkOut = ""
    }

    private fun givenHotelCheckInOutDateInvalid(hotelShortlistItem: HotelShortlistItem) {
        hotelShortlistItem.shortlistItem!!.metaData!!.chkIn = "as#d123"
        hotelShortlistItem.shortlistItem!!.metaData!!.chkOut = "#$2abc"
    }

    private fun givenHotelCheckInOutDateNull(hotelShortlistItem: HotelShortlistItem) {
        hotelShortlistItem.shortlistItem!!.metaData!!.chkIn = null
        hotelShortlistItem.shortlistItem!!.metaData!!.chkOut = null
    }

    private fun givenHotelPricePerNightEmpty(hotelShortlistItem: HotelShortlistItem) {
        hotelShortlistItem.price = ""
    }

    private fun givenHotelPricePerNightZero(hotelShortlistItem: HotelShortlistItem) {
        hotelShortlistItem.price = "0"
    }

    private fun givenHotelPricePerNightInvalid(hotelShortlistItem: HotelShortlistItem) {
        hotelShortlistItem.price = "@1w"
    }

    private fun givenHotelPricePerNightNull(hotelShortlistItem: HotelShortlistItem) {
        hotelShortlistItem.price = null
    }

    private fun givenGuestRatingInvalid(hotelShortlistItem: HotelShortlistItem) {
        hotelShortlistItem.guestRating = "@1w"
    }

    private fun givenGuestRatingEmpty(hotelShortlistItem: HotelShortlistItem) {
        hotelShortlistItem.guestRating = ""
    }

    private fun givenGuestRatingNull(hotelShortlistItem: HotelShortlistItem) {
        hotelShortlistItem.guestRating = null
    }

    private fun givenHotelNameEmpty(hotelShortlistItem: HotelShortlistItem) {
        hotelShortlistItem.name = ""
    }

    private fun givenHotelNameNull(hotelShortlistItem: HotelShortlistItem) {
        hotelShortlistItem.name = null
    }
}
