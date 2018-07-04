package com.expedia.bookings.hotel.widget

import android.app.Activity
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.hotel.util.HotelInfoManager
import com.expedia.bookings.hotel.util.HotelSearchManager
import com.expedia.bookings.packages.vm.PackageHotelDetailViewModel
import com.expedia.bookings.services.HotelServices
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.PointOfSaleTestConfiguration
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.CurrencyUtils
import com.expedia.testutils.AndroidAssert.Companion.assertGone
import com.expedia.testutils.AndroidAssert.Companion.assertViewContDescEquals
import com.expedia.testutils.AndroidAssert.Companion.assertViewTextEquals
import com.expedia.testutils.AndroidAssert.Companion.assertVisible
import com.expedia.bookings.hotel.vm.HotelDetailViewModel
import com.squareup.phrase.Phrase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelDetailContentViewTest {

    private var activity: Activity by Delegates.notNull()
    private var testVM: HotelDetailViewModel by Delegates.notNull()
    private var contentView: HotelDetailContentView by Delegates.notNull()

    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Default)
        CurrencyUtils.initMap(activity)
        contentView = android.view.LayoutInflater.from(activity).inflate(R.layout.test_hotel_detail_content_view, null) as HotelDetailContentView
        testVM = HotelDetailViewModel(activity,
                HotelInfoManager(Mockito.mock(HotelServices::class.java)),
                Mockito.mock(HotelSearchManager::class.java))
        contentView.viewModel = testVM
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testDiscountPercentageVisibility() {
        testVM.showDiscountPercentageObservable.onNext(true)
        assertVisible(contentView.discountPercentage)

        testVM.showDiscountPercentageObservable.onNext(false)
        assertGone(contentView.discountPercentage)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testDiscountText() {
        val expectedDiscountMessage = Phrase.from(activity.resources, R.string.hotel_discount_percent_Template)
                .put("discount", 20).format().toString()
        val expectedContDesc = "ACCESSIBILITY IS AWESOME"
        testVM.discountPercentageObservable.onNext(Pair(expectedDiscountMessage, expectedContDesc))

        assertViewTextEquals(expectedDiscountMessage, contentView.discountPercentage)
        assertViewContDescEquals(expectedContDesc, contentView.discountPercentage)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPromoMessaging() {
        val expectedPromoText = activity.getString(R.string.member_pricing)
        testVM.promoMessageObservable.onNext(expectedPromoText)

        assertViewTextEquals(expectedPromoText, contentView.promoMessage)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPromoContainerVisibility() {
        triggerMessageContainer(visible = false)
        assertGone(contentView.hotelMessagingContainer)

        triggerMessageContainer(visible = true)
        assertVisible(contentView.hotelMessagingContainer)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testVipAccessVisibility() {
        testVM.hasVipAccessObservable.onNext(false)
        assertGone(contentView.vipAccessMessageContainer)

        testVM.hasVipAccessObservable.onNext(true)
        assertVisible(contentView.vipAccessMessageContainer)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testAirAttachVisibility() {
        testVM.showAirAttachedObservable.onNext(false)
        assertGone(contentView.airAttachImage)

        testVM.showAirAttachedObservable.onNext(true)
        assertVisible(contentView.airAttachImage)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testSearchInfoText() {
        val expectedInfo = "1 Guest, May 10"
        testVM.searchInfoObservable.onNext(expectedInfo)

        assertViewTextEquals(expectedInfo, contentView.searchInfo)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testSoldOutVisibilities() {
        testVM.hotelSoldOut.onNext(false)
        assertGone(contentView.detailsSoldOut)
        assertVisible(contentView.price)
        assertVisible(contentView.roomContainer)

        testVM.hotelSoldOut.onNext(true)
        assertVisible(contentView.detailsSoldOut)
        assertEquals(activity.getString(R.string.trip_bucket_sold_out), contentView.detailsSoldOut.text)
        assertGone(contentView.price)
        assertGone(contentView.roomContainer)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testStrikeThroughPriceWithStrikeThroughPriceGreaterThanPriceToShowUsers() {
        val strikeThroughPrice = "100"

        testVM.strikeThroughPriceObservable.onNext(strikeThroughPrice)
        testVM.strikeThroughPriceGreaterThanPriceToShowUsersObservable.onNext(true)
        testVM.hotelSoldOut.onNext(false)
        testVM.shopWithPointsObservable.onNext(true)
        testVM.showAirAttachedObservable.onNext(false)

        assertVisible(contentView.strikeThroughPrice)
        assertViewTextEquals(strikeThroughPrice, contentView.strikeThroughPrice)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testStrikeThroughPriceWithoutStrikeThroughPriceGreaterThanPriceToShowUsers() {
        val strikeThroughPrice = "100"

        testVM.strikeThroughPriceObservable.onNext(strikeThroughPrice)

        testVM.strikeThroughPriceGreaterThanPriceToShowUsersObservable.onNext(false)
        testVM.hotelSoldOut.onNext(false)
        testVM.shopWithPointsObservable.onNext(true)
        testVM.showAirAttachedObservable.onNext(false)

        assertGone(contentView.strikeThroughPrice)
        assertViewTextEquals(strikeThroughPrice, contentView.strikeThroughPrice)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testStrikeThroughPriceWithHotelSoldOut() {
        val strikeThroughPrice = "100"

        testVM.strikeThroughPriceObservable.onNext(strikeThroughPrice)
        testVM.strikeThroughPriceGreaterThanPriceToShowUsersObservable.onNext(true)
        testVM.hotelSoldOut.onNext(true)
        testVM.shopWithPointsObservable.onNext(true)
        testVM.showAirAttachedObservable.onNext(false)

        assertGone(contentView.strikeThroughPrice)
        assertViewTextEquals(strikeThroughPrice, contentView.strikeThroughPrice)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testStrikeThroughPriceWithHotelNotSoldOut() {
        val strikeThroughPrice = "100"

        testVM.strikeThroughPriceObservable.onNext(strikeThroughPrice)
        testVM.strikeThroughPriceGreaterThanPriceToShowUsersObservable.onNext(true)
        testVM.hotelSoldOut.onNext(false)
        testVM.shopWithPointsObservable.onNext(true)
        testVM.showAirAttachedObservable.onNext(false)

        assertVisible(contentView.strikeThroughPrice)
        assertViewTextEquals(strikeThroughPrice, contentView.strikeThroughPrice)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testStrikeThroughPriceWithShopWithPoints() {
        testVM.strikeThroughPriceGreaterThanPriceToShowUsersObservable.onNext(true)
        testVM.hotelSoldOut.onNext(false)
        testVM.shopWithPointsObservable.onNext(true)
        testVM.showAirAttachedObservable.onNext(true)
        assertVisible(contentView.strikeThroughPrice)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testStrikeThroughPriceWithoutShopWithPoints() {
        testVM.strikeThroughPriceGreaterThanPriceToShowUsersObservable.onNext(true)
        testVM.hotelSoldOut.onNext(false)
        testVM.shopWithPointsObservable.onNext(false)
        testVM.showAirAttachedObservable.onNext(true)
        assertGone(contentView.strikeThroughPrice)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testStrikeThroughPriceWithAirAttached() {
        testVM.strikeThroughPriceGreaterThanPriceToShowUsersObservable.onNext(true)
        testVM.hotelSoldOut.onNext(false)
        testVM.shopWithPointsObservable.onNext(false)
        testVM.showAirAttachedObservable.onNext(true)
        assertGone(contentView.strikeThroughPrice)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testStrikeThroughPriceWithoutAirAttached() {
        testVM.strikeThroughPriceGreaterThanPriceToShowUsersObservable.onNext(true)
        testVM.hotelSoldOut.onNext(false)
        testVM.shopWithPointsObservable.onNext(false)
        testVM.showAirAttachedObservable.onNext(false)
        assertVisible(contentView.strikeThroughPrice)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testFreeCancellationAndEtp() {
        testVM.hasETPObservable.onNext(true)
        testVM.hasFreeCancellationObservable.onNext(true)
        testVM.hotelSoldOut.onNext(false)
        assertVisible(contentView.freeCancellationAndETPMessaging)

        testVM.hotelSoldOut.onNext(true)
        assertGone(contentView.freeCancellationAndETPMessaging)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testSingleMessageContainer() {
        testVM.hasETPObservable.onNext(false)
        testVM.hasFreeCancellationObservable.onNext(false)
        testVM.hotelSoldOut.onNext(false)
        assertVisible(contentView.singleMessageContainer)

        testVM.hasETPObservable.onNext(true)
        assertVisible(contentView.singleMessageContainer)

        testVM.hasFreeCancellationObservable.onNext(true)
        assertGone(contentView.singleMessageContainer)

        testVM.hasFreeCancellationObservable.onNext(false)
        testVM.hasETPObservable.onNext(false)
        testVM.hotelSoldOut.onNext(true)
        assertGone(contentView.singleMessageContainer)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testDateless() {
        testVM.isDatelessObservable.onNext(true)
        assertGone(contentView.priceContainer)
        testVM.isDatelessObservable.onNext(false)
        assertVisible(contentView.priceContainer)
    }

    @Test
    fun testDetailedPriceViewsNotShown() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(activity, AbacusUtils.EBAndroidAppPackagesHSRPriceDisplay)
        assertGone(contentView.detailedPriceType)
        assertGone(contentView.detailedPriceIncludesMessage)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testDetailedPriceViewsShownForPackages() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(activity, AbacusUtils.EBAndroidAppPackagesHSRPriceDisplay)
        contentView.viewModel = PackageHotelDetailViewModel(activity)

        assertVisible(contentView.detailedPriceType)
        assertGone(contentView.detailedPriceIncludesMessage)

        // Both price type and includes taxes message shown
        val initialPOSID = PointOfSale.getPointOfSale().pointOfSaleId
        setPointOfSale(PointOfSaleId.JAPAN)

        contentView.viewModel = PackageHotelDetailViewModel(activity)
        assertVisible(contentView.detailedPriceType)
        assertVisible(contentView.detailedPriceIncludesMessage)
        setPointOfSale(initialPOSID)
    }

    private fun setPointOfSale(posId: PointOfSaleId) {
        PointOfSaleTestConfiguration.configurePOS(activity, "ExpediaSharedData/ExpediaPointOfSaleConfig.json", Integer.toString(posId.id), false)
    }

    private fun triggerMessageContainer(visible: Boolean) {
        // Yea need to trigger each individual observable to trigger hotelMessageContainerVisibility.
        // View doesn't care about any of this so hardcoding all the values
        testVM.showDiscountPercentageObservable.onNext(true)
        testVM.hasVipAccessObservable.onNext(false)
        testVM.promoMessageObservable.onNext("")
        testVM.hotelSoldOut.onNext(!visible) // sold out easiest way to change the output
        testVM.hasRegularLoyaltyPointsAppliedObservable.onNext(false)
        testVM.showAirAttachedObservable.onNext(false)
    }
}
