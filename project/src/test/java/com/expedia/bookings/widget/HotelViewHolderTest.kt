package com.expedia.bookings.widget

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.launch.RecommendedHotelViewModel
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
@RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
class HotelViewHolderTest {

    lateinit var testHotelViewHolder: HotelViewHolder
    private lateinit var rootView: ViewGroup
    private lateinit var hotel: Hotel
    private val testHotelSelectedSubject = PublishSubject.create<Hotel>()
    private lateinit var recommendedHotelViewModel: RecommendedHotelViewModel
    var activity: Activity by Delegates.notNull()

    @Before
    fun setUp() {
        rootView = LayoutInflater.from(RuntimeEnvironment.application).inflate(R.layout.section_launch_list_card, null, false) as ViewGroup
        hotel = Hotel()
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
    }

    @Test
    fun bindListData_showsSaleText_givenDiscountIsAtLeastTenPercentOff() {
        createBaseTestHotel(20F, 4F, false, false, false)
        testHotelViewHolder = HotelViewHolder(rootView)

        recommendedHotelViewModel = RecommendedHotelViewModel(activity, hotel)
        testHotelViewHolder.bindListData(hotel, true, testHotelSelectedSubject, recommendedHotelViewModel)

        assertEquals(testHotelViewHolder.saleTextView.visibility, View.VISIBLE)
    }

    @Test
    fun bindListData_hidesSubtitle() {
        createBaseTestHotel(20F, 4F, false, false, false)
        testHotelViewHolder = HotelViewHolder(rootView)
        recommendedHotelViewModel = RecommendedHotelViewModel(activity, hotel)
        testHotelViewHolder.bindListData(hotel, true, testHotelSelectedSubject, recommendedHotelViewModel)

        assertEquals(testHotelViewHolder.subtitle.visibility, View.GONE)
    }

    @Test
    fun bindListData_hidesSaleText_givenDiscountIsAtLeastTenPercentOff() {
        createBaseTestHotel(9F, 4F, false, false, false)
        testHotelViewHolder = HotelViewHolder(rootView)
        recommendedHotelViewModel = RecommendedHotelViewModel(activity, hotel)
        testHotelViewHolder.bindListData(hotel, true, testHotelSelectedSubject, recommendedHotelViewModel)

        assertEquals(testHotelViewHolder.saleTextView.visibility, View.GONE)
    }

    @Test
    fun bindListData_updatesFullPriceText_givenTileIsFullWidth() {
        createBaseTestHotel(9F, 4F, false, false, false)
        testHotelViewHolder = HotelViewHolder(rootView)
        recommendedHotelViewModel = RecommendedHotelViewModel(activity, hotel)
        testHotelViewHolder.bindListData(hotel, true, testHotelSelectedSubject, recommendedHotelViewModel)

        assertEquals(testHotelViewHolder.fullTilePrice.text, "$80")
    }

    @Test
    fun bindListData_updatesFullPriceText_givenTileIsHalfWidth() {
        createBaseTestHotel(20F, 4F, false, false, false)
        testHotelViewHolder = HotelViewHolder(rootView)
        recommendedHotelViewModel = RecommendedHotelViewModel(activity, hotel)
        testHotelViewHolder.bindListData(hotel, false, testHotelSelectedSubject, recommendedHotelViewModel)

        assertEquals(testHotelViewHolder.halfTilePrice.text, "$80")
    }

    @Test
    fun bindListData_hidesGuestRatingAndShowsNoRatingText_givenRatingIsZeroAndFullTile() {
        createBaseTestHotel(9F, 0F, false, false, false)
        testHotelViewHolder = HotelViewHolder(rootView)
        recommendedHotelViewModel = RecommendedHotelViewModel(activity, hotel)
        testHotelViewHolder.bindListData(hotel, true, testHotelSelectedSubject, recommendedHotelViewModel)

        assertEquals(testHotelViewHolder.ratingInfo.visibility, View.GONE)
        assertEquals(testHotelViewHolder.noRatingText.visibility, View.VISIBLE)
    }

    @Test
    fun bindListData_showRatingText_givenRatingIsAboveZeroAndFullTile() {
        createBaseTestHotel(9F, 2.0F, false, false, false)
        testHotelViewHolder = HotelViewHolder(rootView)
        recommendedHotelViewModel = RecommendedHotelViewModel(activity, hotel)
        testHotelViewHolder.bindListData(hotel, true, testHotelSelectedSubject, recommendedHotelViewModel)

        assertEquals(testHotelViewHolder.ratingText.visibility, View.VISIBLE)
        assertEquals(testHotelViewHolder.rating.text, "2.0")
    }

    @Test
    fun bindListData_setsTextToMobileExclusive_givenDiscountIsRestrictedtoCurrentSourceType() {
        createBaseTestHotel(20F, 4F, true, false, false)
        testHotelViewHolder = HotelViewHolder(rootView)
        recommendedHotelViewModel = RecommendedHotelViewModel(activity, hotel)
        testHotelViewHolder.bindListData(hotel, true, testHotelSelectedSubject, recommendedHotelViewModel)

        assertEquals(testHotelViewHolder.saleTextView.text, "mobile only!")
        assertEquals(testHotelViewHolder.saleTextView.background, ColorDrawable(R.color.launch_mobile_exclusive))
    }

    @Test
    fun bindListData_setsTextToPercentOff_givenDiscountIsRestrictedToCurrentSourceTypeButHalfTile() {
        createBaseTestHotel(20F, 4F, true, false, false)

        testHotelViewHolder = HotelViewHolder(rootView)
        recommendedHotelViewModel = RecommendedHotelViewModel(activity, hotel)
        testHotelViewHolder.bindListData(hotel, false, testHotelSelectedSubject, recommendedHotelViewModel)

        assertEquals(testHotelViewHolder.saleTextView.text, "-20%")
        assertEquals(testHotelViewHolder.saleTextView.background, ColorDrawable(R.color.launch_mobile_exclusive))
    }

    @Test
    fun bindListData_setsTextToTonightOnlyMessage_givenDiscountIsSameDayDRRAndFullWidth() {
        createBaseTestHotel(20F, 4F, false, true, false)
        testHotelViewHolder = HotelViewHolder(rootView)
        recommendedHotelViewModel = RecommendedHotelViewModel(activity, hotel)
        testHotelViewHolder.bindListData(hotel, true, testHotelSelectedSubject, recommendedHotelViewModel)

        assertEquals(testHotelViewHolder.saleTextView.text, "tonight only!")
        assertEquals(testHotelViewHolder.saleTextView.background, ColorDrawable(R.color.launch_tonight_only))
    }

    @Test
    fun bindListData_setsTextPercentDiscount_givenDiscountIsSameDayDRRAndButHalfWidth() {
        createBaseTestHotel(20F, 4F, false, true, false)
        testHotelViewHolder = HotelViewHolder(rootView)
        recommendedHotelViewModel = RecommendedHotelViewModel(activity, hotel)
        testHotelViewHolder.bindListData(hotel, false, testHotelSelectedSubject, recommendedHotelViewModel)

        assertEquals(testHotelViewHolder.saleTextView.text, "-20%")
        assertEquals(testHotelViewHolder.saleTextView.background, ColorDrawable(R.color.launch_tonight_only))
    }

    @Test
    fun bindListData_setsSalesTextViewToDiscountAsDefault_givenDiscountIsAtLeastTenPercent() {
        createBaseTestHotel(20F, 4F, false, false, false)
        testHotelViewHolder = HotelViewHolder(rootView)
        recommendedHotelViewModel = RecommendedHotelViewModel(activity, hotel)
        testHotelViewHolder.bindListData(hotel, true, testHotelSelectedSubject, recommendedHotelViewModel)

        assertEquals(testHotelViewHolder.saleTextView.text, "-20%")
        assertEquals(testHotelViewHolder.saleTextView.background, ColorDrawable(R.color.launch_discount))
    }

    @Test
    fun bindListData_setsBackgroundToOrange_givenDiscountIsAirAttached() {
        createBaseTestHotel(20F, 4F, false, false, true)
        testHotelViewHolder = HotelViewHolder(rootView)
        recommendedHotelViewModel = RecommendedHotelViewModel(activity, hotel)
        testHotelViewHolder.bindListData(hotel, true, testHotelSelectedSubject, recommendedHotelViewModel)

        assertEquals(testHotelViewHolder.saleTextView.text, "-20%")
        assertEquals(testHotelViewHolder.saleTextView.background, ColorDrawable(R.color.launch_air_attach))
    }

    @Test
    fun bindListData_makesFullTileStrikethroughPriceVisible_givenDiscountIsAtLeastTenPercentAndFullWidth() {
        createBaseTestHotel(20F, 4F, false, false, false)
        testHotelViewHolder = HotelViewHolder(rootView)
        recommendedHotelViewModel = RecommendedHotelViewModel(activity, hotel)
        testHotelViewHolder.bindListData(hotel, true, testHotelSelectedSubject, recommendedHotelViewModel)

        assertEquals(testHotelViewHolder.fullTileStrikethroughPrice.visibility, View.VISIBLE)
    }

    @Test
    fun bindListData_makesFullTileStrikethroughPriceInvisible_givenDiscountIsLessThanTenPercentAndFullWidth() {
        createBaseTestHotel(9F, 4F, false, true, false)
        testHotelViewHolder = HotelViewHolder(rootView)
        recommendedHotelViewModel = RecommendedHotelViewModel(activity, hotel)
        testHotelViewHolder.bindListData(hotel, true, testHotelSelectedSubject, recommendedHotelViewModel)

        assertEquals(testHotelViewHolder.fullTileStrikethroughPrice.visibility, View.GONE)
    }

    @Test
    fun bindListData_makesHalfTileStrikethroughPriceVisible_givenHalfWidthAndDiscountGreaterThanTenPercent() {
        createBaseTestHotel(20F, 4F, false, false, false)
        testHotelViewHolder = HotelViewHolder(rootView)
        recommendedHotelViewModel = RecommendedHotelViewModel(activity, hotel)
        testHotelViewHolder.bindListData(hotel, false, testHotelSelectedSubject, recommendedHotelViewModel)

        assertEquals(testHotelViewHolder.halfTileStrikethroughPrice.visibility, View.VISIBLE)
    }

    @Test
    fun bindListData_hidesStrikethroughPrice_givenHalfWidthAndDiscountLessThanTen() {
        createBaseTestHotel(9F, 4F, false, false, false)
        testHotelViewHolder = HotelViewHolder(rootView)
        recommendedHotelViewModel = RecommendedHotelViewModel(activity, hotel)
        testHotelViewHolder.bindListData(hotel, false, testHotelSelectedSubject, recommendedHotelViewModel)

        assertEquals(testHotelViewHolder.halfTileStrikethroughPrice.visibility, View.GONE)
    }

    @Test
    fun bindListData_hidesRatingText_givenHalfWidth() {
        createBaseTestHotel(9F, 4F, false, false, false)
        testHotelViewHolder = HotelViewHolder(rootView)
        recommendedHotelViewModel = RecommendedHotelViewModel(activity, hotel)
        testHotelViewHolder.bindListData(hotel, false, testHotelSelectedSubject, recommendedHotelViewModel)

        assertEquals(testHotelViewHolder.ratingText.visibility, View.GONE)
    }

    @Test
    fun bindListData_updatesRating_givenHalfWidth() {
        createBaseTestHotel(9F, 4F, false, false, false)
        testHotelViewHolder = HotelViewHolder(rootView)
        recommendedHotelViewModel = RecommendedHotelViewModel(activity, hotel)
        testHotelViewHolder.bindListData(hotel, false, testHotelSelectedSubject, recommendedHotelViewModel)

        assertEquals(testHotelViewHolder.rating.text, "4.0")
    }

    @Test
    fun bindListData_hidesRatingInfo_givenHotelRatingOfZeroAndHalfWidth() {
        createBaseTestHotel(9F, 0F, false, false, false)
        testHotelViewHolder = HotelViewHolder(rootView)
        recommendedHotelViewModel = RecommendedHotelViewModel(activity, hotel)
        testHotelViewHolder.bindListData(hotel, false, testHotelSelectedSubject, recommendedHotelViewModel)

        assertEquals(testHotelViewHolder.ratingInfo.visibility, View.INVISIBLE)
    }

    private fun createBaseTestHotel(discountPercent: Float, guestRating: Float, isDiscountRestrictedtoCurrentSourceType: Boolean, isSameDayDDR: Boolean, isAirAttached: Boolean) {
        hotel.lowRateInfo = HotelRate()
        hotel.lowRateInfo.currencyCode = "USD"
        hotel.localizedName = "Test localized name"
        hotel.lowRateInfo.strikethroughPriceToShowUsers = 100F
        hotel.lowRateInfo.priceToShowUsers = 80F
        hotel.lowRateInfo.discountPercent = discountPercent
        hotel.hotelGuestRating = guestRating
        hotel.isDiscountRestrictedToCurrentSourceType = isDiscountRestrictedtoCurrentSourceType
        hotel.isSameDayDRR = isSameDayDDR
        hotel.lowRateInfo.airAttached = isAirAttached
    }
}
