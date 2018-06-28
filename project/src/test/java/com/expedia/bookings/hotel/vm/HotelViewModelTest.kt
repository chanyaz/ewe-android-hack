package com.expedia.bookings.hotel.vm

import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.LoyaltyMembershipTier
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.data.payment.LoyaltyEarnInfo
import com.expedia.bookings.data.payment.LoyaltyInformation
import com.expedia.bookings.data.payment.PointsEarnInfo
import com.expedia.bookings.data.payment.PriceEarnInfo
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.data.user.User
import com.expedia.bookings.data.user.UserLoyaltyMembershipInformation
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.features.Features
import com.expedia.bookings.hotel.util.HotelFavoritesCache
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.ExcludeForBrands
import com.expedia.bookings.test.MockPackageServiceTestRule
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.PointOfSaleTestConfiguration
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.FeatureTestUtils
import com.expedia.bookings.utils.Images
import com.expedia.bookings.utils.Strings
import com.expedia.util.LoyaltyUtil
import com.mobiata.android.util.SettingUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.test.assertNotEquals

@RunWith(RobolectricRunner::class)
@Config(shadows = [ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class])
class HotelViewModelTest {

    var mockPackageServiceTestRule = MockPackageServiceTestRule()
        @Rule get

    private val context = RuntimeEnvironment.application

    private lateinit var vm: HotelViewModel
    private lateinit var hotel: Hotel
    private lateinit var user: User

    @Before
    fun before() {
        hotel = Hotel().apply {
            hotelId = "hotelId"
            lowRateInfo = HotelRate()
            lowRateInfo.currencyCode = "USD"
            val pointEarnInfo = PointsEarnInfo(320, 0, 320)
            val loyaltyEarnInfo = LoyaltyEarnInfo(pointEarnInfo, null)
            lowRateInfo.loyaltyInfo = LoyaltyInformation(null, loyaltyEarnInfo, false)
            distanceUnit = "Miles"
            hotelStarRating = 2.0f
            localizedName = "Test Hotel"
        }

        user = User().apply {
            val traveler = Traveler()
            primaryTraveler = traveler

            val loyaltyInfo = UserLoyaltyMembershipInformation()
            loyaltyInfo.loyaltyMembershipTier = LoyaltyMembershipTier.TOP
            loyaltyMembershipInformation = loyaltyInfo
        }
    }

    @Test
    fun testHotelInFavoriteSubject() {
        assertHotelFavoriteSubject(setOf("hotelId"), true)
    }

    @Test
    fun testHotelInFavoriteSubjectMultipleFavorites() {
        assertHotelFavoriteSubject(setOf("hotelId3", "hotelId", "hotelId1", "hotelId2"), true)
    }

    @Test
    fun testHotelNotInFavoriteSubject() {
        assertHotelFavoriteSubject(setOf("hotelId1"), false)
    }

    @Test
    fun tesHotelNotInFavoriteSubjectEmptyCache() {
        assertHotelFavoriteSubject(emptySet(), false)
    }

    @Test
    fun testHotelNotInFavoriteSubjectMultipleFavorites() {
        assertHotelFavoriteSubject(setOf("hotelId3", "hotelId2", "hotelId1", "hotelId0"), false)
    }

    @Test
    fun testCacheChangedSubjectHotelNotInitialized() {
        vm = HotelViewModel(context)
        val inFavoriteTestObserver = TestObserver<Unit>()
        val notInFavoriteTestObserver = TestObserver<Unit>()
        vm.hotelInFavoriteSubject.subscribe(inFavoriteTestObserver)
        vm.hotelNotInFavoriteSubject.subscribe(notInFavoriteTestObserver)
        HotelFavoritesCache.cacheChangedSubject.onNext(setOf("hotelId"))
        inFavoriteTestObserver.assertValueCount(0)
        notInFavoriteTestObserver.assertValueCount(0)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA, MultiBrand.ORBITZ])
    fun strikeThroughPriceShow() {
        hotel.lowRateInfo.priceToShowUsers = 10f
        hotel.lowRateInfo.strikeThroughPrice = 12f

        setupHotelViewModel()

        assertEquals("$12", vm.hotelStrikeThroughPriceFormatted!!.toString())
        assertEquals("Test Hotel with 2 stars of 5 rating.\u0020Earn 320 points Regularly $12, now $10.\u0020Button",
                vm.getHotelContentDesc().toString())
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA, MultiBrand.ORBITZ])
    fun contentDescriptionWithStrikeThroughPercent() {
        hotel.lowRateInfo.priceToShowUsers = 10f
        hotel.lowRateInfo.strikeThroughPrice = 12f
        hotel.lowRateInfo.discountPercent = 10f
        hotel.lowRateInfo.airAttached = false
        hotel.hotelStarRating = 4f
        hotel.hotelGuestRating = 3f

        setupHotelViewModel()

        assertTrue(vm.showDiscount)
        assertEquals(
                "Test Hotel with 4 stars of 5 rating. 3.0 of 5 guest rating.\u0020Original price discounted 10%. Earn 320 points\u0020Regularly $12, now $10.\u0020Button",
                vm.getHotelContentDesc().toString())
    }

    @Test
    fun contentDescriptionWithZeroStarRating() {
        hotel.hotelStarRating = 0f
        hotel.hotelGuestRating = 3f
        setupHotelViewModel()
        assertEquals("Test Hotel with 3.0 of 5 guest rating.\u0020", vm.getRatingContentDesc(hotel))
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA, MultiBrand.ORBITZ])
    fun contentDescriptionWithZeroGuestRating() {
        hotel.hotelStarRating = 4f
        hotel.hotelGuestRating = 0f
        setupHotelViewModel()
        assertEquals("Test Hotel with 4 stars of 5 rating.\u0020", vm.getRatingContentDesc(hotel))
    }

    @Test
    fun contentDescriptionWithZeroStarRatingAndZeroGuestRating() {
        hotel.hotelStarRating = 0f
        hotel.hotelGuestRating = 0f
        setupHotelViewModel()
        assertEquals("Test Hotel.\u0020", vm.getRatingContentDesc(hotel))
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA, MultiBrand.ORBITZ])
    fun contentDescriptionWithNonZeroRatings() {
        hotel.hotelStarRating = 4f
        hotel.hotelGuestRating = 3f
        setupHotelViewModel()
        assertEquals("Test Hotel with 4 stars of 5 rating. 3.0 of 5 guest rating.\u0020",
                vm.getRatingContentDesc(hotel))
    }

    @Test
    fun strikeThroughPriceShownForPackages() {
        val response = mockPackageServiceTestRule.getMIDHotelResponse()
        val firstHotel = response.getHotels()[1]

        val vm = HotelViewModel(context, false)
        vm.bindHotelData(firstHotel)
        assertEquals("$2,098", vm.hotelStrikeThroughPriceFormatted!!.toString())
    }

    @Test
    fun strikeThroughPriceZeroDontShow() {
        hotel.lowRateInfo.strikeThroughPrice = 0f

        setupHotelViewModel()

        assertNull(vm.hotelStrikeThroughPriceFormatted)
    }

    @Test
    fun strikeThroughPriceLessThanPriceToShowUsers() {
        hotel.lowRateInfo.priceToShowUsers = 10f
        hotel.lowRateInfo.strikeThroughPrice = 2f

        setupHotelViewModel()

        assertNull(vm.hotelStrikeThroughPriceFormatted)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY])
    fun zeroIsDisplayedWhenPriceToShowUsersIsNegative() {
        hotel.lowRateInfo.priceToShowUsers = -10f
        hotel.lowRateInfo.strikeThroughPrice = 12f
        setupHotelViewModel()

        assertEquals("$12", vm.hotelStrikeThroughPriceFormatted!!.toString())
        assertEquals("$0", vm.hotelPriceFormatted)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY])
    fun distanceFromLocationObservableNonZeroDistance() {
        val distanceInMiles = 0.42
        givenHotelWithProximityDistance(distanceInMiles)
        setupHotelViewModel()

        val expectedDistance = "0.4 mi"
        assertEquals(expectedDistance, vm.distanceFromCurrentLocation())
    }

    @Test
    fun distanceFromLocationObservableZeroDistance() {
        val distanceInMiles = 0.0
        givenHotelWithProximityDistance(distanceInMiles)
        setupHotelViewModel()

        assertTrue(Strings.isEmpty(vm.distanceFromCurrentLocation()))
    }

    @Test
    fun testNoUrgencyMessageIfHotelIsSoldOut() {
        givenSoldOutHotel()
        givenHotelWithAddOnAttach()
        givenHotelWithMemberDeal()
        givenHotelWithFewRoomsLeft()
        givenHotelWithTonightOnly()
        givenHotelWithMobileExclusive()

        setupHotelViewModel()
        val msg = vm.getHighestPriorityUrgencyMessage()
        assertNull(msg)
    }

    @Test
    fun testUrgencyMessageAddOnAttachHasFirstPriority() {
        givenHotelWithAddOnAttach()
        givenHotelWithMemberDeal()
        givenHotelWithFewRoomsLeft()
        givenHotelWithTonightOnly()
        givenHotelWithMobileExclusive()

        assertUrgencyMessage(R.drawable.ic_add_on_attach_for_yellow_bg,
                R.color.exp_yellow,
                "",
                R.color.member_pricing_text_color)
    }

    @Test
    @ExcludeForBrands(brands = [MultiBrand.ORBITZ])
    fun testUrgencyMessageMemberDealsHasSecondPriority() {
        givenHotelWithMemberDeal()
        givenHotelWithFewRoomsLeft()
        givenHotelWithTonightOnly()
        givenHotelWithMobileExclusive()

        assertUrgencyMessage(R.drawable.ic_member_only_tag,
                R.color.member_pricing_bg_color,
                "Member Pricing",
                R.color.member_pricing_text_color)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.ORBITZ])
    fun testUrgencyMessageInsiderPricesHasSecondPriority() {
        givenHotelWithMemberDeal()
        givenHotelWithFewRoomsLeft()
        givenHotelWithTonightOnly()
        givenHotelWithMobileExclusive()

        assertUrgencyMessage(R.drawable.ic_member_only_tag,
                R.color.member_pricing_bg_color,
                "Insider Prices",
                R.color.member_pricing_text_color)
    }

    @Test
    fun testUrgencyMessageFewRoomsLeftHasThirdPriority() {
        givenHotelWithFewRoomsLeft()
        givenHotelWithTonightOnly()
        givenHotelWithMobileExclusive()

        assertUrgencyMessage(R.drawable.urgency,
                R.color.hotel_urgency_message_color,
                "We have 3 rooms left",
                R.color.gray900)
    }

    @Test
    fun testUrgencyMessageTonightOnlyHasFourthPriority() {
        givenHotelWithTonightOnly()
        givenHotelWithMobileExclusive()

        assertUrgencyMessage(R.drawable.tonight_only,
                R.color.hotel_tonight_only_color,
                "Tonight Only!",
                R.color.white)
    }

    @Test
    fun testUrgencyMessageMobileExclusiveHasFifthPriority() {
        givenHotelWithMobileExclusive()

        assertUrgencyMessage(R.drawable.mobile_exclusive,
                R.color.hotel_mobile_exclusive_color,
                "Mobile Exclusive",
                R.color.white)
    }

    @Test
    fun testNoUrgencyMessage() {
        setupHotelViewModel()
        val msg = vm.getHighestPriorityUrgencyMessage()
        assertNull(msg)
    }

    @Test
    fun testShouldShowFavoriteIcon() {
        AbacusTestUtils.bucketTests(AbacusUtils.HotelShortlist)
        UserLoginTestUtil.setupUserAndMockLogin(user)
        hotel.isPackage = false
        setupHotelViewModel()
        assertTrue(vm.shouldShowFavoriteIcon())
    }

    @Test
    fun testShouldShowFavoriteIconNotBucketed() {
        AbacusTestUtils.unbucketTests(AbacusUtils.HotelShortlist)
        UserLoginTestUtil.setupUserAndMockLogin(user)
        hotel.isPackage = false
        setupHotelViewModel()
        assertFalse(vm.shouldShowFavoriteIcon())
    }

    @Test
    fun testShouldShowFavoriteIconSignedOut() {
        AbacusTestUtils.unbucketTests(AbacusUtils.HotelShortlist)
        UserLoginTestUtil.setupUserAndMockLogin(user)
        hotel.isPackage = false
        setupHotelViewModel()
        assertFalse(vm.shouldShowFavoriteIcon())
    }

    @Test
    fun testShouldShowFavoriteIconPackage() {
        AbacusTestUtils.unbucketTests(AbacusUtils.HotelShortlist)
        UserLoginTestUtil.setupUserAndMockLogin(user)
        hotel.isPackage = true
        setupHotelViewModel()
        assertFalse(vm.shouldShowFavoriteIcon())
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA, MultiBrand.ORBITZ])
    fun vipMessageWithNoLoyaltyMessage() {
        givenHotelWithVipAccess()
        UserLoginTestUtil.setupUserAndMockLogin(user)
        setupHotelViewModel()

        assertTrue(vm.showVipMessage())
        assertFalse(vm.showVipLoyaltyMessage())
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA, MultiBrand.ORBITZ])
    fun vipLoyaltyMessageVisible() {
        givenHotelWithVipAccess()
        givenHotelWithShopWithPointsAvailable()
        UserLoginTestUtil.setupUserAndMockLogin(user)
        setupHotelViewModel()

        assertTrue(vm.showVipLoyaltyMessage())
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA, MultiBrand.ORBITZ])
    fun vipLoyaltyMessageDisplayedOnMaps() {
        givenHotelWithVipAccess()
        givenHotelWithShopWithPointsAvailable()
        UserLoginTestUtil.setupUserAndMockLogin(user)
        setupHotelViewModel()

        assertTrue(vm.loyaltyAvailable)
        assertEquals(
                HtmlCompat.fromHtml(RuntimeEnvironment.application.getString(R.string.vip_loyalty_applied_map_message))
                        .toString(),
                vm.getMapLoyaltyMessageText().toString())
    }

    @Test
    fun regularLoyaltyMessageDisplayedOnMaps() {
        givenHotelWithShopWithPointsAvailable()
        UserLoginTestUtil.setupUserAndMockLogin(user)
        setupHotelViewModel()

        assertTrue(vm.loyaltyAvailable)
        assertEquals(RuntimeEnvironment.application.getString(R.string.regular_loyalty_applied_message),
                vm.getMapLoyaltyMessageText().toString())
    }

    @Test
    fun testGetMapLoyaltyMessageVisibilityShopWithPoints() {
        setupHotelViewModel()

        assertEquals(View.INVISIBLE, vm.getMapLoyaltyMessageVisibility(true))
    }

    @Test
    fun testGetMapLoyaltyMessageVisibilityGone() {
        setupHotelViewModel()

        assertEquals(View.GONE, vm.getMapLoyaltyMessageVisibility(false))
    }

    @Test
    fun testGetMapLoyaltyMessageVisibilityLoyaltyAvailable() {
        val pointEarnInfo = PointsEarnInfo(320, 0, 320)
        val loyaltyEarnInfo = LoyaltyEarnInfo(pointEarnInfo, null)
        hotel.lowRateInfo.loyaltyInfo = LoyaltyInformation(null, loyaltyEarnInfo, true)

        setupHotelViewModel()

        assertEquals(View.VISIBLE, vm.getMapLoyaltyMessageVisibility(false))
    }

    @Test
    fun discountPercentageIsShown() {
        hotel.lowRateInfo.discountPercent = -12f
        setupHotelViewModel()

        assertTrue(vm.showDiscount)
        assertFalse(vm.loyaltyAvailable)
    }

    @Test
    fun zeroDiscountPercentageIsNotShown() {
        hotel.lowRateInfo.discountPercent = 0f
        setupHotelViewModel()

        assertFalse(vm.showDiscount)
    }

    @Test
    fun discountPercentageIsNotShownForSWP() {
        hotel.lowRateInfo.discountPercent = -12f
        givenHotelWithShopWithPointsAvailable()
        setupHotelViewModel()

        assertFalse(vm.showDiscount)
        assertTrue(vm.loyaltyAvailable)
    }

    @Test
    fun discountPercentageIsShownForGenericAttachEnabledWithAirAttached() {
        hotel.lowRateInfo.discountPercent = 12f
        hotel.lowRateInfo.airAttached = true
        setupHotelViewModel()

        assertTrue(vm.showDiscount)
        assertFalse(vm.loyaltyAvailable)
    }

    @Test
    fun discountPercentageIsNotShownForGenericAttachDisabledWithAirAttached() {
        hotel.lowRateInfo.discountPercent = 12f
        hotel.lowRateInfo.airAttached = true
        setupHotelViewModelGenericAttachNotEnabled()

        assertFalse(vm.showDiscount)
        assertFalse(vm.loyaltyAvailable)
    }

    @Test
    fun discountPercentageIsNotShownOnGenericAttachEnabledWithoutAirAttached() {
        hotel.lowRateInfo.discountPercent = 12f
        hotel.lowRateInfo.airAttached = false
        setupHotelViewModel()

        assertTrue(vm.showDiscount)
        assertFalse(vm.loyaltyAvailable)
    }

    @Test
    fun discountPercentageIsShownOnGenericAttachDisabledWithoutAirAttached() {
        hotel.lowRateInfo.discountPercent = 12f
        hotel.lowRateInfo.airAttached = false
        setupHotelViewModelGenericAttachNotEnabled()

        assertTrue(vm.showDiscount)
        assertFalse(vm.loyaltyAvailable)
    }

    @Test
    fun notShowAirAttachWithDiscountLabelTestOnSWPWithAirAttached() {
        hotel.lowRateInfo.airAttached = true
        hotel.lowRateInfo.discountPercent = 11f
        givenHotelWithShopWithPointsAvailable()
        setupHotelViewModel()

        assertFalse(vm.showAirAttachWithDiscountLabel)
        assertTrue(vm.loyaltyAvailable)
    }

    @Test
    fun notShowAirAttachWithDiscountLabelTestOnSWPWithoutAirAttached() {
        hotel.lowRateInfo.airAttached = false
        hotel.lowRateInfo.discountPercent = 11f
        givenHotelWithShopWithPointsAvailable()
        setupHotelViewModel()

        assertFalse(vm.showAirAttachWithDiscountLabel)
        assertTrue(vm.loyaltyAvailable)
    }

    @Test
    fun showAirAttachWithDiscountLabelTestWithAirAttached() {
        hotel.lowRateInfo.airAttached = true
        hotel.lowRateInfo.discountPercent = 11f
        setupHotelViewModel()

        assertTrue(vm.showAirAttachWithDiscountLabel)
        assertFalse(vm.loyaltyAvailable)
    }

    @Test
    fun notShowAirAttachWithDiscountLabelTestWithoutAirAttached() {
        hotel.lowRateInfo.airAttached = false
        hotel.lowRateInfo.discountPercent = 11f
        setupHotelViewModel()

        assertFalse(vm.showAirAttachWithDiscountLabel)
        assertFalse(vm.loyaltyAvailable)
    }

    @Test
    fun showAirAttachIconWithoutDiscountLabelTestOnSWPWithAirAttached() {
        hotel.lowRateInfo.airAttached = true
        hotel.lowRateInfo.discountPercent = 11f
        givenHotelWithShopWithPointsAvailable()
        setupHotelViewModel()

        assertTrue(vm.showAirAttachIconWithoutDiscountLabel)
        assertTrue(vm.loyaltyAvailable)
    }

    @Test
    fun notShowAirAttachIconWithoutDiscountLabelTestOnSWPWithoutAirAttached() {
        hotel.lowRateInfo.airAttached = false
        hotel.lowRateInfo.discountPercent = 11f
        givenHotelWithShopWithPointsAvailable()
        setupHotelViewModel()

        assertFalse(vm.showAirAttachIconWithoutDiscountLabel)
        assertTrue(vm.loyaltyAvailable)
    }

    @Test
    fun notShowShowAirAttachIconWithoutDiscountLabelTestWithAirAttached() {
        hotel.lowRateInfo.airAttached = true
        hotel.lowRateInfo.discountPercent = 11f
        setupHotelViewModel()

        assertFalse(vm.showAirAttachIconWithoutDiscountLabel)
        assertFalse(vm.loyaltyAvailable)
    }

    @Test
    fun notShowAirAttachIconWithoutDiscountLabelTestWithoutAirAttached() {
        hotel.lowRateInfo.airAttached = false
        hotel.lowRateInfo.discountPercent = 11f
        setupHotelViewModel()

        assertFalse(vm.showAirAttachIconWithoutDiscountLabel)
        assertFalse(vm.loyaltyAvailable)
    }

    @Test
    fun packageHotelThumbnailNotSetIfMissing() {
        hotel.isPackage = true
        setupHotelViewModel()
        assertTrue(Strings.isEmpty(vm.getHotelLargeThumbnailUrl()))
    }

    @Test
    fun hotelThumbnailErrorIsMissing() {
        hotel.isPackage = false
        setupHotelViewModel()
        assertEquals(Images.getMediaHost() + null.toString(), vm.getHotelLargeThumbnailUrl())
    }

    @Test
    fun packageThumbnailIsSet() {
        givenHotelWithThumbnail(true)
        setupHotelViewModel()
        assertEquals("http://some_awesome_hotel_pix.png", vm.getHotelLargeThumbnailUrl())
    }

    @Test
    fun hotelThumbnailIsSet() {
        givenHotelWithThumbnail(false)
        setupHotelViewModel()
        assertEquals(Images.getMediaHost() + "some_awesome_hotel_pix", vm.getHotelLargeThumbnailUrl())
    }

    @Test
    fun hotelIsSponsored() {
        givenIsSponsoredListing(true)
        setupHotelViewModel()
        assertEquals(RuntimeEnvironment.application.resources.getString(R.string.sponsored),
                vm.topAmenityTitle)
    }

    @Test
    fun bothSponsoredEarnMessagingShow() {
        givenIsSponsoredListing(true)
        setupHotelViewModel()

        PointOfSaleTestConfiguration
                .configurePointOfSale(RuntimeEnvironment.application, "MockSharedData/pos_test_config.json", false)
        val pos = PointOfSale.getPointOfSale()
        assertTrue(pos.isEarnMessageEnabledForHotels)
        if (ProductFlavorFeatureConfiguration.getInstance().showHotelLoyaltyEarnMessage()) {
            assertEquals(View.VISIBLE, vm.earnMessageVisibility)
        } else {
            assertNotEquals(View.VISIBLE, vm.earnMessageVisibility)
        }
        assertTrue(!vm.topAmenityTitle.isEmpty())
    }

    @Test
    @RunForBrands(brands = [MultiBrand.ORBITZ])
    fun testEarnMessagingVisibility() {
        setPOS(PointOfSaleId.ORBITZ)

        val base = Money("11.03", "USD")
        val bonus = Money("00.00", "USD")
        val total = Money("11.03", "USD")
        hotel.lowRateInfo.loyaltyInfo = LoyaltyInformation(null,
                LoyaltyEarnInfo(null, PriceEarnInfo(base, bonus, total)), false)
        setupHotelViewModel()
        assertEquals(View.VISIBLE, vm.earnMessageVisibility)
    }

    @Test
    fun testEarnMessageVisibilityVisible() {
        setupHotelViewModel()
        if (LoyaltyUtil.isEarnMessageEnabled(hotel.isPackage)) {
            assertEquals(View.VISIBLE, vm.earnMessageVisibility)
        } else {
            assertEquals(View.GONE, vm.earnMessageVisibility)
        }
    }

    @Test
    fun testEarnMessageVisibilityInvisible() {
        givenHotelWithShopWithPointsAvailable()
        setupHotelViewModelAlwaysShowEarnMessageSpace()
        if (LoyaltyUtil.isEarnMessageEnabled(hotel.isPackage)) {
            assertEquals(View.INVISIBLE, vm.earnMessageVisibility)
        } else {
            assertEquals(View.GONE, vm.earnMessageVisibility)
        }
    }

    private fun givenSoldOutHotel() {
        hotel.isSoldOut = true
    }

    private fun givenHotelWithAddOnAttach() {
        FeatureTestUtils.enableFeature(context, Features.all.genericAttach)
        hotel.lowRateInfo.airAttached = true
        hotel.lowRateInfo.discountPercent = 10f
    }

    private fun givenHotelWithMemberDeal() {
        hotel.isMemberDeal = true
        UserLoginTestUtil.setupUserAndMockLogin(user)
    }

    private fun givenHotelWithFewRoomsLeft() {
        hotel.roomsLeftAtThisRate = 3
    }

    private fun givenHotelWithTonightOnly() {
        hotel.isSameDayDRR = true
    }

    private fun givenHotelWithMobileExclusive() {
        hotel.isDiscountRestrictedToCurrentSourceType = true
    }

    private fun givenHotelWithProximityDistance(distanceInMiles: Double) {
        hotel.proximityDistanceInMiles = distanceInMiles
    }

    private fun givenHotelWithVipAccess() {
        hotel.isVipAccess = true
    }

    private fun givenHotelWithShopWithPointsAvailable() {
        val loyaltyInformation = LoyaltyInformation(null, LoyaltyEarnInfo(null, null), true)
        hotel.lowRateInfo.loyaltyInfo = loyaltyInformation
    }

    private fun givenHotelWithThumbnail(isPackage: Boolean) {
        hotel.isPackage = isPackage
        hotel.thumbnailUrl = "http://some_awesome_hotel_pix.png"
        hotel.largeThumbnailUrl = "some_awesome_hotel_pix"
    }

    private fun givenIsSponsoredListing(isSponsored: Boolean) {
        hotel.isSponsoredListing = isSponsored
    }

    private fun setupHotelViewModel() {
        vm = HotelViewModel(context, isAddOnAttachEnabled = true)
        vm.bindHotelData(hotel)
    }

    private fun setupHotelViewModelGenericAttachNotEnabled() {
        vm = HotelViewModel(context, isAddOnAttachEnabled = false)
        vm.bindHotelData(hotel)
    }

    private fun setupHotelViewModelAlwaysShowEarnMessageSpace() {
        vm = HotelViewModel(context, true, true)
        vm.bindHotelData(hotel)
    }

    private fun setPOS(pos: PointOfSaleId) {
        SettingUtils.save(context, R.string.PointOfSaleKey, pos.id.toString() + "")
        PointOfSale.onPointOfSaleChanged(context)
    }

    private fun assertUrgencyMessage(iconDrawableId: Int?, backgroundColorId: Int?, message: String, messageTextColorId: Int?) {
        setupHotelViewModel()
        val msg = vm.getHighestPriorityUrgencyMessage()

        assertEquals(iconDrawableId, msg!!.iconDrawableId)
        assertEquals(backgroundColorId, msg.backgroundColorId)
        assertEquals(message, msg.message)
        assertEquals(messageTextColorId, msg.messageTextColorId)
    }

    private fun assertHotelFavoriteSubject(favorites: Set<String>, isInFavorite: Boolean) {
        setupHotelViewModel()
        val inFavoriteTestObserver = TestObserver<Unit>()
        val notInFavoriteTestObserver = TestObserver<Unit>()
        vm.hotelInFavoriteSubject.subscribe(inFavoriteTestObserver)
        vm.hotelNotInFavoriteSubject.subscribe(notInFavoriteTestObserver)
        HotelFavoritesCache.cacheChangedSubject.onNext(favorites)
        if (isInFavorite) {
            inFavoriteTestObserver.assertValueCount(1)
            notInFavoriteTestObserver.assertValueCount(0)
        } else {
            inFavoriteTestObserver.assertValueCount(0)
            notInFavoriteTestObserver.assertValueCount(1)
        }
    }
}
