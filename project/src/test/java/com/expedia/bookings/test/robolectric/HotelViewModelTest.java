package com.expedia.bookings.test.robolectric;

import java.util.concurrent.TimeUnit;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import android.app.Application;
import android.content.Context;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LoyaltyMembershipTier;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.SuggestionV4;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.hotels.Hotel;
import com.expedia.bookings.data.hotels.HotelRate;
import com.expedia.bookings.data.multiitem.BundleSearchResponse;
import com.expedia.bookings.data.packages.PackageSearchParams;
import com.expedia.bookings.data.payment.LoyaltyEarnInfo;
import com.expedia.bookings.data.payment.LoyaltyInformation;
import com.expedia.bookings.data.payment.PointsEarnInfo;
import com.expedia.bookings.data.payment.PriceEarnInfo;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.data.user.User;
import com.expedia.bookings.data.user.UserLoyaltyMembershipInformation;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.services.PackageServices;
import com.expedia.bookings.services.ProductSearchType;
import com.expedia.bookings.test.MultiBrand;
import com.expedia.bookings.test.PointOfSaleTestConfiguration;
import com.expedia.bookings.test.RunForBrands;
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB;
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM;
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager;
import com.expedia.bookings.testrule.ServicesRule;
import com.expedia.bookings.text.HtmlCompat;
import com.expedia.bookings.utils.Images;
import com.expedia.bookings.utils.Strings;
import com.expedia.vm.hotel.HotelViewModel;
import com.mobiata.android.util.SettingUtils;

import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricRunner.class)
@Config(shadows = { ShadowGCM.class, ShadowUserManager.class, ShadowAccountManagerEB.class })
public class HotelViewModelTest {
	@Rule
	public ServicesRule<PackageServices> serviceRule = new ServicesRule<>(PackageServices.class, Schedulers.immediate(),
		"../lib/mocked/templates", true);

	HotelViewModel vm;
	Hotel hotel;

	private Context getContext() {
		return RuntimeEnvironment.application;
	}

	@Before
	public void before() {
		hotel = new Hotel();
		hotel.lowRateInfo = new HotelRate();
		hotel.distanceUnit = "Miles";
		hotel.lowRateInfo.currencyCode = "USD";
		hotel.hotelStarRating = 2.0f;
		hotel.localizedName = "Test Hotel";
		hotel.lowRateInfo.loyaltyInfo = new LoyaltyInformation(null,
			new LoyaltyEarnInfo(new PointsEarnInfo(320, 0, 320), null), false);
	}

	@Test
	@RunForBrands(brands = { MultiBrand.EXPEDIA, MultiBrand.ORBITZ })
	public void strikeThroughPriceShow() {
		hotel.lowRateInfo.priceToShowUsers = 10f;
		hotel.lowRateInfo.strikethroughPriceToShowUsers = 12f;

		setupSystemUnderTest();

		assertEquals("$12", vm.getHotelStrikeThroughPriceFormatted().toString());
		assertEquals("Test Hotel with 2 stars of 5 rating.\u0020Earn 320 points Regularly $12, now $10.\u0020Button",
			vm.getHotelContentDesc().toString());
	}

	@Test
	@RunForBrands(brands = { MultiBrand.EXPEDIA, MultiBrand.ORBITZ })
	public void contentDescriptionWithStrikeThroughPercent() {
		hotel.lowRateInfo.priceToShowUsers = 10f;
		hotel.lowRateInfo.strikethroughPriceToShowUsers = 12f;
		hotel.lowRateInfo.discountPercent = 10;
		hotel.lowRateInfo.airAttached = false;
		hotel.hotelStarRating = 4;
		hotel.hotelGuestRating = 3;

		setupSystemUnderTest();

		assertTrue(vm.getShowDiscount());
		assertEquals(
			"Test Hotel with 4 stars of 5 rating. 3.0 of 5 guest rating.\u0020Original price discounted 10%. Earn 320 points\u0020Regularly $12, now $10.\u0020Button",
			vm.getHotelContentDesc().toString());
	}

	@Test
	public void contentDescriptionWithZeroStarRating() {
		hotel.hotelStarRating = 0;
		hotel.hotelGuestRating = 3;
		setupSystemUnderTest();
		assertEquals("Test Hotel with 3.0 of 5 guest rating.\u0020", vm.getRatingContentDesc(hotel));
	}

	@Test
	@RunForBrands(brands = { MultiBrand.EXPEDIA, MultiBrand.ORBITZ })
	public void contentDescriptionWithZeroGuestRating() {
		hotel.hotelStarRating = 4;
		hotel.hotelGuestRating = 0;
		setupSystemUnderTest();
		assertEquals("Test Hotel with 4 stars of 5 rating.\u0020", vm.getRatingContentDesc(hotel));
	}

	@Test
	public void contentDescriptionWithZeroStarRatingAndZeroGuestRating() {
		hotel.hotelStarRating = 0;
		hotel.hotelGuestRating = 0;
		setupSystemUnderTest();
		assertEquals("Test Hotel.\u0020", vm.getRatingContentDesc(hotel));
	}

	@Test
	@RunForBrands(brands = { MultiBrand.EXPEDIA, MultiBrand.ORBITZ })
	public void contentDescriptionWithNonZeroRatings() {
		hotel.hotelStarRating = 4;
		hotel.hotelGuestRating = 3;
		setupSystemUnderTest();
		assertEquals("Test Hotel with 4 stars of 5 rating. 3.0 of 5 guest rating.\u0020",
			vm.getRatingContentDesc(hotel));
	}

	@Test
	@RunForBrands(brands = { MultiBrand.EXPEDIA, MultiBrand.ORBITZ })
	public void strikeThroughPriceShowForPackages() {
		TestSubscriber<BundleSearchResponse> observer = new TestSubscriber<>();
		PackageSearchParams params = (PackageSearchParams) new PackageSearchParams.Builder(26, 329)
			.origin(getDummySuggestion())
			.destination(getDummySuggestion())
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(1))
			.build();

		serviceRule.getServices().packageSearch(params, ProductSearchType.OldPackageSearch).subscribe(observer);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);

		observer.assertValueCount(1);
		BundleSearchResponse response = observer.getOnNextEvents().get(0);
		Hotel firstHotel = response.getHotels().get(0);

		HotelViewModel vm = new HotelViewModel(getContext());
		vm.bindHotelData(firstHotel);
		assertEquals("$538", vm.getHotelStrikeThroughPriceFormatted().toString());
	}

	@Test
	public void strikeThroughPriceDontShowForPackages() {
		TestSubscriber<BundleSearchResponse> observer = new TestSubscriber<>();
		PackageSearchParams params = (PackageSearchParams) new PackageSearchParams.Builder(26, 329)
			.origin(getDummySuggestion())
			.destination(getDummySuggestion())
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(1))
			.build();

		serviceRule.getServices().packageSearch(params, ProductSearchType.OldPackageSearch).subscribe(observer);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);

		observer.assertValueCount(1);
		BundleSearchResponse response = observer.getOnNextEvents().get(0);
		Hotel firstHotel = response.getHotels().get(1);

		HotelViewModel vm = new HotelViewModel(getContext());
		vm.bindHotelData(firstHotel);
		assertNull(vm.getHotelStrikeThroughPriceFormatted());
	}

	private SuggestionV4 getDummySuggestion() {
		SuggestionV4 suggestion = new SuggestionV4();
		suggestion.gaiaId = "";
		suggestion.regionNames = new SuggestionV4.RegionNames();
		suggestion.regionNames.displayName = "";
		suggestion.regionNames.fullName = "";
		suggestion.regionNames.shortName = "";
		suggestion.hierarchyInfo = new SuggestionV4.HierarchyInfo();
		suggestion.hierarchyInfo.airport = new SuggestionV4.Airport();
		suggestion.hierarchyInfo.airport.airportCode = "";
		suggestion.hierarchyInfo.airport.multicity = "happy";
		return suggestion;
	}

	@Test
	public void strikeThroughPriceZeroDontShow() {
		hotel.lowRateInfo.strikethroughPriceToShowUsers = 0f;

		setupSystemUnderTest();

		assertNull(vm.getHotelStrikeThroughPriceFormatted());
	}

	@Test
	public void strikeThroughPriceLessThanPriceToShowUsers() {
		hotel.lowRateInfo.priceToShowUsers = 10f;
		hotel.lowRateInfo.strikethroughPriceToShowUsers = 2f;

		setupSystemUnderTest();

		assertNull(vm.getHotelStrikeThroughPriceFormatted());
	}

	@Test
	@RunForBrands(brands = { MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY })
	public void zeroIsDisplayedWhenPriceToShowUsersIsNegative() {
		hotel.lowRateInfo.priceToShowUsers = -10f;
		hotel.lowRateInfo.strikethroughPriceToShowUsers = 12f;
		setupSystemUnderTest();

		assertEquals("$12", vm.getHotelStrikeThroughPriceFormatted().toString());
		assertEquals("$0", vm.getHotelPriceFormatted());
	}

	@Test
	@RunForBrands(brands = { MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY })
	public void distanceFromLocationObservableNonZeroDistance() {
		double distanceInMiles = 0.42;
		givenHotelWithProximityDistance(distanceInMiles);
		setupSystemUnderTest();

		String expectedDistance = "0.4 mi";
		assertEquals(expectedDistance, vm.distanceFromCurrentLocation());
	}

	@Test
	public void distanceFromLocationObservableZeroDistance() {
		double distanceInMiles = 0;
		givenHotelWithProximityDistance(distanceInMiles);
		setupSystemUnderTest();

		assertTrue(Strings.isEmpty(vm.distanceFromCurrentLocation()));
	}

	@Test
	public void noUrgencyMessageIfHotelIsSoldOut() {
		givenHotelWithFewRoomsLeft();
		givenHotelMobileExclusive();
		givenSoldOutHotel();
		givenHotelTonightOnly();

		setupSystemUnderTest();
		HotelViewModel.UrgencyMessage msg = vm.getHighestPriorityUrgencyMessage();
		assertNull(msg);
	}

	@Test
	public void urgencyMessageFewRoomsLeftHasFirstPriority() {
		givenHotelWithFewRoomsLeft();
		givenHotelMobileExclusive();
		givenHotelTonightOnly();
		setupSystemUnderTest();

		HotelViewModel.UrgencyMessage msg = vm.getHighestPriorityUrgencyMessage();
		HotelViewModel.UrgencyMessage compareTo = new HotelViewModel.UrgencyMessage(
			R.drawable.urgency,
			R.color.hotel_urgency_message_color,
			RuntimeEnvironment.application.getResources()
				.getQuantityString(R.plurals.num_rooms_left, hotel.roomsLeftAtThisRate, hotel.roomsLeftAtThisRate),
			R.color.white);

		assertEquals(compareTo, msg);
	}

	@Test
	public void urgencyMessageTonightOnlyHasSecondPriority() {
		givenHotelTonightOnly();
		givenHotelMobileExclusive();

		setupSystemUnderTest();
		HotelViewModel.UrgencyMessage msg = vm.getHighestPriorityUrgencyMessage();

		HotelViewModel.UrgencyMessage compareTo = new HotelViewModel.UrgencyMessage(
			R.drawable.tonight_only,
			R.color.hotel_tonight_only_color,
			RuntimeEnvironment.application.getResources().getString(R.string.tonight_only),
			R.color.white);

		assertEquals(compareTo, msg);
	}

	@Test
	@RunForBrands(brands = { MultiBrand.EXPEDIA })
	public void urgencyMessageMobileExclusiveHasFourthPriority() {
		givenHotelMobileExclusive();

		setupSystemUnderTest();

		HotelViewModel.UrgencyMessage msg = vm.getHighestPriorityUrgencyMessage();

		HotelViewModel.UrgencyMessage compareTo = new HotelViewModel.UrgencyMessage(
			R.drawable.mobile_exclusive,
			R.color.hotel_mobile_exclusive_color,
			RuntimeEnvironment.application.getResources().getString(R.string.mobile_exclusive),
			R.color.white);

		assertEquals(compareTo, msg);
	}

	@Test
	@RunForBrands(brands = { MultiBrand.EXPEDIA, MultiBrand.ORBITZ })
	public void vipMessageWithNoLoyaltyMessage() {
		givenHotelWithVipAccess();
		UserLoginTestUtil.setupUserAndMockLogin(getUser());
		setupSystemUnderTest();

		assertTrue(vm.showVipMessage());
		assertFalse(vm.showVipLoyaltyMessage());
	}

	@Test
	@RunForBrands(brands = { MultiBrand.EXPEDIA, MultiBrand.ORBITZ })
	public void vipLoyaltyMessageVisible() {
		givenHotelWithVipAccess();
		givenHotelWithShopWithPointsAvailable();
		UserLoginTestUtil.setupUserAndMockLogin(getUser());
		setupSystemUnderTest();

		assertTrue(vm.showVipLoyaltyMessage());
	}

	@Test
	@RunForBrands(brands = { MultiBrand.EXPEDIA, MultiBrand.ORBITZ })
	public void vipLoyaltyMessageDisplayedOnMaps() {
		givenHotelWithVipAccess();
		givenHotelWithShopWithPointsAvailable();
		UserLoginTestUtil.setupUserAndMockLogin(getUser());
		setupSystemUnderTest();

		assertTrue(vm.getLoyaltyAvailable());
		assertEquals(
			HtmlCompat.fromHtml(RuntimeEnvironment.application.getString(R.string.vip_loyalty_applied_map_message))
				.toString(),
			vm.getMapLoyaltyMessageText().toString());
	}

	@Test
	public void regularLoyaltyMessageDisplayedOnMaps() {
		givenHotelWithShopWithPointsAvailable();
		UserLoginTestUtil.setupUserAndMockLogin(getUser());
		setupSystemUnderTest();

		assertTrue(vm.getLoyaltyAvailable());
		assertEquals(RuntimeEnvironment.application.getString(R.string.regular_loyalty_applied_message),
			vm.getMapLoyaltyMessageText().toString());
	}

	@Test
	public void discountPercentageIsShown() {
		hotel.lowRateInfo.discountPercent = -12;
		setupSystemUnderTest();

		assertTrue(vm.getShowDiscount());
		assertFalse(vm.getLoyaltyAvailable());
	}

	@Test
	public void zeroDiscountPercentageIsNotShown() {
		hotel.lowRateInfo.discountPercent = 0;
		setupSystemUnderTest();

		assertFalse(vm.getShowDiscount());
	}

	@Test
	public void discountPercentageIsShownForSWP() {
		hotel.lowRateInfo.discountPercent = -12;
		givenHotelWithShopWithPointsAvailable();
		setupSystemUnderTest();

		assertTrue(vm.getShowDiscount());
		assertTrue(vm.getLoyaltyAvailable());
	}

	@Test
	public void testHotelDiscountPercentageClipsAboveHundred() {
		hotel.lowRateInfo.discountPercent = 1000;
		setupSystemUnderTest();

		assertEquals("100%", vm.getHotelDiscountPercentage());
	}

	@Test
	public void testHotelDiscountPercentageClipsBelowNegativeHundred() {
		hotel.lowRateInfo.discountPercent = -1000;
		setupSystemUnderTest();

		assertEquals("-100%", vm.getHotelDiscountPercentage());
	}

	@Test
	public void packageHotelThumbnailNotSetIfMissing() {
		hotel.isPackage = true;
		setupSystemUnderTest();
		assertTrue(Strings.isEmpty(vm.getHotelLargeThumbnailUrl()));
	}

	@Test
	public void hotelThumbnailErrorIsMissing() {
		hotel.isPackage = false;
		setupSystemUnderTest();
		assertEquals(Images.getMediaHost() + null, vm.getHotelLargeThumbnailUrl());
	}

	@Test
	public void packageThumbnailIsSet() {
		givenHotelWithThumbnail(true);
		setupSystemUnderTest();
		assertEquals("http://some_awesome_hotel_pix.png", vm.getHotelLargeThumbnailUrl());
	}

	@Test
	public void hotelThumbnailIsSet() {
		givenHotelWithThumbnail(false);
		setupSystemUnderTest();
		assertEquals(Images.getMediaHost() + "some_awesome_hotel_pix", vm.getHotelLargeThumbnailUrl());
	}

	@Test
	public void hotelIsSponsored() {
		givenIsSponsoredListing(true);
		setupSystemUnderTest();
		assertEquals(RuntimeEnvironment.application.getResources().getString(R.string.sponsored),
			vm.getTopAmenityTitle());
	}

	@Test
	public void bothSponsoredEarnMessagingShow() {
		givenIsSponsoredListing(true);
		setupSystemUnderTest();

		PointOfSaleTestConfiguration
			.configurePointOfSale(RuntimeEnvironment.application, "MockSharedData/pos_test_config.json", false);
		PointOfSale pos = PointOfSale.getPointOfSale();
		assertTrue(pos.isEarnMessageEnabledForHotels());
		if (ProductFlavorFeatureConfiguration.getInstance().showHotelLoyaltyEarnMessage()) {
			assertTrue(vm.getShowEarnMessage());
		}
		else {
			assertFalse(vm.getShowEarnMessage());
		}
		assertTrue(!vm.getTopAmenityTitle().isEmpty());
	}

	@Test
	@RunForBrands(brands = { MultiBrand.ORBITZ })
	public void testEarnMessagingVisibility() {
		setPOS(PointOfSaleId.ORBITZ);

		Money base = new Money("11.03", "USD");
		Money bonus = new Money("00.00", "USD");
		Money total = new Money("11.03", "USD");
		hotel.lowRateInfo.loyaltyInfo = new LoyaltyInformation(null,
			new LoyaltyEarnInfo(null, new PriceEarnInfo(base, bonus, total)), false);
		setupSystemUnderTest();
		assertTrue(vm.getShowEarnMessage());
	}

	private void givenSoldOutHotel() {
		hotel.isSoldOut = true;
	}

	private void givenHotelTonightOnly() {
		hotel.isSameDayDRR = true;
	}

	private void givenHotelMobileExclusive() {
		hotel.isDiscountRestrictedToCurrentSourceType = true;
	}

	private void givenHotelWithFewRoomsLeft() {
		hotel.roomsLeftAtThisRate = 3;
	}

	private void givenHotelWithProximityDistance(double distanceInMiles) {
		hotel.proximityDistanceInMiles = distanceInMiles;
	}

	private void givenHotelWithVipAccess() {
		hotel.isVipAccess = true;
	}

	private void givenHotelWithShopWithPointsAvailable() {
		LoyaltyInformation loyaltyInformation = new LoyaltyInformation(null, new LoyaltyEarnInfo(null, null), true);
		hotel.lowRateInfo.loyaltyInfo = loyaltyInformation;
	}

	private void givenHotelWithThumbnail(boolean isPackage) {
		hotel.isPackage = isPackage;
		hotel.thumbnailUrl = "http://some_awesome_hotel_pix.png";
		hotel.largeThumbnailUrl = "some_awesome_hotel_pix";
	}

	private void givenIsSponsoredListing(boolean isSponsored) {
		hotel.isSponsoredListing = isSponsored;
	}

	private void setupSystemUnderTest() {
		Application applicationContext = RuntimeEnvironment.application;
		vm = new HotelViewModel(applicationContext);
		vm.bindHotelData(hotel);
	}

	private User getUser() {
		User user = new User();
		Traveler traveler = new Traveler();
		user.setPrimaryTraveler(traveler);
		UserLoyaltyMembershipInformation loyaltyInfo = new UserLoyaltyMembershipInformation();
		loyaltyInfo.setLoyaltyMembershipTier(LoyaltyMembershipTier.TOP);
		user.setLoyaltyMembershipInformation(loyaltyInfo);
		return user;
	}

	private void setPOS(PointOfSaleId pos) {
		SettingUtils.save(getContext(), R.string.PointOfSaleKey, pos.getId() + "");
		PointOfSale.onPointOfSaleChanged(getContext());
	}
}
