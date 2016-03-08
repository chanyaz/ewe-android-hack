package com.expedia.bookings.test.robolectric;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import android.app.Application;
import android.text.Html;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.hotels.Hotel;
import com.expedia.bookings.data.hotels.HotelRate;
import com.expedia.bookings.data.payment.LoyaltyEarnInfo;
import com.expedia.bookings.data.payment.LoyaltyInformation;
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB;
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM;
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.widget.HotelViewModel;

import rx.observers.TestSubscriber;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricRunner.class)
@Config(shadows = { ShadowGCM.class, ShadowUserManager.class, ShadowAccountManagerEB.class})
public class HotelViewModelTest {

	HotelViewModel vm;
	Hotel hotel;

	@Before
	public void before() {
		hotel = new Hotel();
		hotel.lowRateInfo = new HotelRate();
		hotel.distanceUnit = "Miles";
		hotel.lowRateInfo.currencyCode = "USD";
		hotel.percentRecommended = 2;
	}

	@Test
	public void strikeThroughPriceShow() {
		hotel.lowRateInfo.priceToShowUsers = 10f;
		hotel.lowRateInfo.strikethroughPriceToShowUsers = 12f;

		setupSystemUnderTest();

		assertTrue(vm.getHotelStrikeThroughPriceVisibility().getValue());
		assertEquals("$12", vm.getHotelStrikeThroughPriceFormatted().getValue().toString());
	}

	@Test
	public void strikeThroughPriceZeroDontShow() {
		hotel.lowRateInfo.strikethroughPriceToShowUsers = 0f;

		setupSystemUnderTest();

		assertFalse(vm.getHotelStrikeThroughPriceVisibility().getValue());
		assertEquals("", vm.getHotelStrikeThroughPriceFormatted().getValue());
	}

	@Test
	public void strikeThroughPriceLessThanPriceToShowUsers() {
		hotel.lowRateInfo.priceToShowUsers = 10f;
		hotel.lowRateInfo.strikethroughPriceToShowUsers = 2f;

		setupSystemUnderTest();

		assertFalse(vm.getHotelStrikeThroughPriceVisibility().getValue());
		assertEquals("", vm.getHotelStrikeThroughPriceFormatted().getValue());
	}

	@Test
	public void distanceFromLocationObservableNonZeroDistance() {
		double distanceInMiles = 0.42;
		givenHotelWithProximityDistance(distanceInMiles);
		setupSystemUnderTest();

		String expectedDistance = "0.4 mi";
		assertEquals(expectedDistance, vm.getDistanceFromCurrentLocation().getValue());
	}

	@Test
	public void distanceFromLocationObservableZeroDistance() {
		double distanceInMiles = 0;
		givenHotelWithProximityDistance(distanceInMiles);
		setupSystemUnderTest();

		assertTrue(Strings.isEmpty(vm.getDistanceFromCurrentLocation().getValue()));
	}

	@Test
	public void urgencyMessageSoldOutHasFirstPriority() {
		givenHotelWithFewRoomsLeft();
		givenHotelMobileExclusive();
		givenSoldOutHotel();
		givenHotelTonightOnly();

		setupSystemUnderTest();

		TestSubscriber<HotelViewModel.UrgencyMessage> urgencyMessageTestSubscriber = TestSubscriber.create();
		vm.getHighestPriorityUrgencyMessageObservable().subscribe(urgencyMessageTestSubscriber);

		urgencyMessageTestSubscriber.assertValue(new HotelViewModel.UrgencyMessage(null,
				R.color.hotel_sold_out_color,
				RuntimeEnvironment.application.getResources().getString(R.string.trip_bucket_sold_out)));
	}

	@Test
	public void urgencyMessageFewRoomsLeftHasSecondPriority() {
		givenHotelWithFewRoomsLeft();
		givenHotelMobileExclusive();
		givenHotelTonightOnly();
		setupSystemUnderTest();

		TestSubscriber<HotelViewModel.UrgencyMessage> urgencyMessageTestSubscriber = TestSubscriber.create();
		vm.getHighestPriorityUrgencyMessageObservable().subscribe(urgencyMessageTestSubscriber);

		urgencyMessageTestSubscriber.assertValue(new HotelViewModel.UrgencyMessage(R.drawable.urgency,
				R.color.hotel_urgency_message_color,
				RuntimeEnvironment.application.getResources().getQuantityString(R.plurals.num_rooms_left, hotel.roomsLeftAtThisRate, hotel.roomsLeftAtThisRate)));
	}

	@Test
	public void urgencyMessageTonightOnlyHasThirdPriority() {
		givenHotelTonightOnly();
		givenHotelMobileExclusive();

		setupSystemUnderTest();

		TestSubscriber<HotelViewModel.UrgencyMessage> urgencyMessageTestSubscriber = TestSubscriber.create();
		vm.getHighestPriorityUrgencyMessageObservable().subscribe(urgencyMessageTestSubscriber);

		urgencyMessageTestSubscriber.assertValue(new HotelViewModel.UrgencyMessage(R.drawable.tonight_only,
				R.color.hotel_tonight_only_color,
				RuntimeEnvironment.application.getResources().getString(R.string.tonight_only)));
	}

	@Test
	public void urgencyMessageMobileExclusiveHasFourthPriority() {
		givenHotelMobileExclusive();

		setupSystemUnderTest();

		TestSubscriber<HotelViewModel.UrgencyMessage> urgencyMessageTestSubscriber = TestSubscriber.create();
		vm.getHighestPriorityUrgencyMessageObservable().subscribe(urgencyMessageTestSubscriber);

		urgencyMessageTestSubscriber.assertValue(new HotelViewModel.UrgencyMessage(R.drawable.mobile_exclusive,
				R.color.hotel_mobile_exclusive_color,
				RuntimeEnvironment.application.getResources().getString(R.string.mobile_exclusive)));
	}

	@Test
	public void vipMessageWithNoLoyaltyMessage() {
		givenHotelWithVipAccess();
		UserLoginTestUtil.Companion.setupUserAndMockLogin(getUser());
		setupSystemUnderTest();

		assertTrue(vm.getVipMessageVisibilityObservable().getValue());
		assertFalse(vm.getVipLoyaltyMessageVisibilityObservable().getValue());
	}

	@Test
	public void vipLoyaltyMessageVisible() {
		givenHotelWithVipAccess();
		givenHotelWithShopWithPointsAvailable();
		UserLoginTestUtil.Companion.setupUserAndMockLogin(getUser());
		setupSystemUnderTest();

		assertTrue(vm.getVipLoyaltyMessageVisibilityObservable().getValue());
	}

	@Test
	public void vipLoyaltyMessageDisplayedOnMaps() {
		givenHotelWithVipAccess();
		givenHotelWithShopWithPointsAvailable();
		UserLoginTestUtil.Companion.setupUserAndMockLogin(getUser());
		setupSystemUnderTest();

		assertTrue(vm.getMapLoyaltyMessageVisibilityObservable().getValue());
		assertEquals(Html.fromHtml(RuntimeEnvironment.application.getString(R.string.vip_loyalty_applied_map_message)), vm.getMapLoyaltyMessageTextObservable().getValue());
	}

	@Test
	public void regularLoyaltyMessageDisplayedOnMaps() {
		givenHotelWithShopWithPointsAvailable();
		UserLoginTestUtil.Companion.setupUserAndMockLogin(getUser());
		setupSystemUnderTest();

		assertTrue(vm.getMapLoyaltyMessageVisibilityObservable().getValue());
		assertEquals(RuntimeEnvironment.application.getString(R.string.regular_loyalty_applied_message), vm.getMapLoyaltyMessageTextObservable().getValue().toString());
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

	private void setupSystemUnderTest() {
		Application applicationContext = RuntimeEnvironment.application;
		vm = new HotelViewModel(applicationContext, hotel);
	}

	private User getUser() {
		User user = new User();
		Traveler traveler = new Traveler();
		user.setPrimaryTraveler(traveler);
		user.getPrimaryTraveler().setLoyaltyMembershipTier(Traveler.LoyaltyMembershipTier.GOLD);
		return user;
	}

}
