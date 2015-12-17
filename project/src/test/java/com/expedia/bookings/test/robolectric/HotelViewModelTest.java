package com.expedia.bookings.test.robolectric;

import android.app.Application;

import com.expedia.bookings.R;
import com.expedia.bookings.data.hotels.Hotel;
import com.expedia.bookings.data.hotels.HotelRate;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.widget.HotelViewModel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

import rx.observers.TestSubscriber;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricRunner.class)
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

	private void setupSystemUnderTest() {
		Application applicationContext = RuntimeEnvironment.application;
		vm = new HotelViewModel(applicationContext, hotel);
	}
}
