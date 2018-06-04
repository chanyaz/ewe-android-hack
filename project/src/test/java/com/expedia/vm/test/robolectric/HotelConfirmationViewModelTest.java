package com.expedia.vm.test.robolectric;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowApplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.test.MultiBrand;
import com.expedia.bookings.test.RunForBrands;
import com.expedia.bookings.test.robolectric.AddToCalendarUtilsTests;
import com.expedia.bookings.test.robolectric.RobolectricRunner;
import com.expedia.bookings.utils.Ui;
import com.expedia.ui.FlightActivity;
import com.expedia.ui.LOBWebViewActivity;
import com.expedia.vm.HotelConfirmationViewModel;

import kotlin.Unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricRunner.class)
public class HotelConfirmationViewModelTest {

	private HotelConfirmationViewModel vm;
	private ShadowApplication shadowApplication;
	private LocalDate checkInDate;
	private LocalDate checkOutDate;
	private Location hotelLocation;
	private String hotelAddress;
	private String hotelName;
	private String hotelCity;

	private String itineraryNumber;

	@Before
	public void before() {
		Ui.getApplication(getContext()).defaultHotelComponents();
		vm = new HotelConfirmationViewModel(getContext(), false);
		shadowApplication = Shadows.shadowOf(RuntimeEnvironment.application);
	}

	private Context getContext() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();
		return activity;
	}

	@Test
	@RunForBrands(brands = {MultiBrand.EXPEDIA})
	public void addToCalendarBtnObserver() {
		boolean isCheckIn = true;
		givenHotelName();
		givenItineraryNumber();
		givenHotelLocation();
		givenCheckInDate();
		givenCheckOutDate();

		vm.getAddToCalendarBtnObserver(getContext()).onNext(Unit.INSTANCE);

		Intent checkInIntentToAssert = shadowApplication.getNextStartedActivity();
		AddToCalendarUtilsTests.makeAddToCalendarIntentAssertions(checkInIntentToAssert, isCheckIn, hotelName, hotelAddress, "", itineraryNumber,checkInDate);
	}

	@Test
	@RunForBrands(brands = {MultiBrand.EXPEDIA})
	public void addCheckOutEventToCalendar() {
		boolean isCheckIn = false;
		givenHotelName();
		givenItineraryNumber();
		givenHotelLocation();
		givenCheckInDate();
		givenCheckOutDate();

		vm.showAddToCalendarIntent(isCheckIn, getContext());

		Intent checkInIntent = shadowApplication.getNextStartedActivity();
		AddToCalendarUtilsTests.makeAddToCalendarIntentAssertions(checkInIntent, isCheckIn, hotelName, hotelAddress, "", itineraryNumber, checkInDate);
	}

	@Test
	public void getDirectionsToHotelBtnObserver() {
		givenHotelLocation();
		vm.getDirectionsToHotelBtnObserver(getContext()).onNext(Unit.INSTANCE);

		Intent intent = shadowApplication.getNextStartedActivity();
		Uri data = intent.getData();

		assertEquals("http://maps.google.com/maps?daddr=" + hotelAddress, data.toString());
	}

	@Test
	public void getAddCarBtnObserver() {
		givenCheckInDate();
		givenCheckOutDate();
		givenHotelLocation();

		vm.getHotelLocation().onNext(hotelLocation);
		vm.getAddCarBtnObserver(getContext()).onNext(Unit.INSTANCE);
		Intent intent = shadowApplication.getNextStartedActivity();

		assertEquals(LOBWebViewActivity.class.getName(), intent.getComponent().getClassName());
		assertTrue(intent.getStringExtra("ARG_URL").startsWith(PointOfSale.getPointOfSale().getCarsTabWebViewURL()));
	}

	@Test
	public void getAddFlightBtnObserver() {
		givenHotelLocation();
		givenCheckInDate();
		givenCheckOutDate();

		vm.getAddFlightBtnObserver(getContext()).onNext(Unit.INSTANCE);
		Intent intent = shadowApplication.getNextStartedActivity();

		assertEquals(FlightActivity.class.getName(), intent.getComponent().getClassName());
	}

	private void givenHotelName() {
		hotelName = "The Cabin";
		vm.getHotelName().onNext(hotelName);
	}

	private void givenItineraryNumber() {
		itineraryNumber = "112358132134";
		vm.getItineraryNumber().onNext(itineraryNumber);
	}

	private void givenCheckInDate() {
		checkInDate = new LocalDate();
		vm.getCheckInDate().onNext(checkInDate);
	}

	private void givenCheckOutDate() {
		checkOutDate = new LocalDate();
		vm.getCheckOutDate().onNext(checkOutDate);
	}

	private void givenHotelLocation() {
		hotelAddress = "114 Sansome St";
		hotelLocation = Mockito.mock(Location.class);
		Mockito.when(hotelLocation.toShortFormattedString()).thenReturn(hotelAddress);
		Mockito.when(hotelLocation.toLongFormattedString()).thenReturn(hotelAddress);
		vm.getHotelLocation().onNext(hotelLocation);
	}
}
