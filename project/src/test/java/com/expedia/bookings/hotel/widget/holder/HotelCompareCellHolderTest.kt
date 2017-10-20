package com.expedia.bookings.hotel.widget.holder

import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.TextView
import com.expedia.testutils.AndroidAssert.Companion.assertGone
import com.expedia.testutils.JSONResourceReader
import com.squareup.phrase.Phrase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelCompareCellHolderTest {
    private val context = RuntimeEnvironment.application
    private val testOffer = getGenericInfo()

    private lateinit var testView: View
    private lateinit var testHolder: HotelCompareCellHolder

    private val priceView by lazy {
        testView.findViewById<TextView>(R.id.detailed_compare_price)
    }

    private val superlativeView by lazy {
        testView.findViewById<TextView>(R.id.detailed_compare_guest_rating_superlative)
    }

    private val reviewCountView by lazy {
        testView.findViewById<TextView>(R.id.detailed_compare_rating_count)
    }

    private val percentRecommendView by lazy {
        testView.findViewById<TextView>(R.id.detailed_compare_recommend_rating)
    }

    @Before
    fun setup() {
        testView = LayoutInflater.from(context)
                .inflate(R.layout.hotel_detailed_compare_cell, null, false)
        testHolder = HotelCompareCellHolder(testView)
    }

    @Test
    fun testPriceNullRooms() {
        testOffer.hotelRoomResponse = null
        testHolder.bind(testOffer)
        assertGone(priceView)
        assertEquals("", priceView.text)
    }

    @Test
    fun testPriceNoRooms() {
        testOffer.hotelRoomResponse = emptyList()
        testHolder.bind(testOffer)
        assertGone(priceView)
        assertEquals("", priceView.text)
    }

    @Test
    fun testSuperlativesExceptional() {
        testOffer.hotelGuestRating = 4.7
        testHolder.bind(testOffer)
        assertEquals(context.getString(R.string.exceptional_guest_rating_description),
                superlativeView.text)
    }

    @Test
    fun testSuperlativesWonderful() {
        testOffer.hotelGuestRating = 4.5
        testHolder.bind(testOffer)
        assertEquals(context.getString(R.string.wonderful_guest_rating_description),
                superlativeView.text)

        testOffer.hotelGuestRating = 4.69
        testHolder.bind(testOffer)
        assertEquals(context.getString(R.string.wonderful_guest_rating_description),
                superlativeView.text)
    }

    @Test
    fun testSuperlativesExcellent() {
        testOffer.hotelGuestRating = 4.3
        testHolder.bind(testOffer)
        assertEquals(context.getString(R.string.excellent_guest_rating_description),
                superlativeView.text)

        testOffer.hotelGuestRating = 4.49
        testHolder.bind(testOffer)
        assertEquals(context.getString(R.string.excellent_guest_rating_description),
                superlativeView.text)
    }

    @Test
    fun testSuperlativesVeryGood() {
        testOffer.hotelGuestRating = 4.0
        testHolder.bind(testOffer)
        assertEquals(context.getString(R.string.very_good_guest_rating_description),
                superlativeView.text)

        testOffer.hotelGuestRating = 4.29
        testHolder.bind(testOffer)
        assertEquals(context.getString(R.string.very_good_guest_rating_description),
                superlativeView.text)
    }

    @Test
    fun testSuperlativesGood() {
        testOffer.hotelGuestRating = 3.5
        testHolder.bind(testOffer)
        assertEquals(context.getString(R.string.good_guest_rating_description),
                superlativeView.text)

        testOffer.hotelGuestRating = 3.99
        testHolder.bind(testOffer)
        assertEquals(context.getString(R.string.good_guest_rating_description),
                superlativeView.text)
    }

    @Test
    fun testSuperlativesGeneric() {
        testOffer.hotelGuestRating = 3.49
        testHolder.bind(testOffer)
        assertEquals(context.getString(R.string.generic_guest_rating_description),
                superlativeView.text)

        testOffer.hotelGuestRating = 0.0
        testHolder.bind(testOffer)
        assertEquals(context.getString(R.string.generic_guest_rating_description),
                superlativeView.text)
    }

    @Test
    fun testReviewCount() {
        val expectedText = Phrase.from(context, R.string.n_reviews_TEMPLATE)
                .put("review_count", "3,302").toString()
        testOffer.totalReviews = 3302
        testHolder.bind(testOffer)

        assertEquals(expectedText, reviewCountView.text)
    }

    @Test
    fun testPercentRecommend() {
        testOffer.percentRecommended = "50"
        testHolder.bind(testOffer)

        assertEquals("50%", percentRecommendView.text)
    }

    private fun getGenericInfo() : HotelOffersResponse {
        val resourceReader = JSONResourceReader("src/test/resources/raw/hotel/happy_info.json")
        return resourceReader.constructUsingGson(HotelOffersResponse::class.java)
    }
}