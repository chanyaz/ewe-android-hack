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

import com.expedia.bookings.R;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.cars.CarSearchParam;
import com.expedia.bookings.lob.lx.ui.activity.LXBaseActivity;
import com.expedia.bookings.services.CarServices;
import com.expedia.bookings.test.MultiBrand;
import com.expedia.bookings.test.RunForBrands;
import com.expedia.bookings.test.robolectric.AddToCalendarUtilsTests;
import com.expedia.bookings.test.robolectric.RobolectricRunner;
import com.expedia.bookings.utils.CarDataUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.ui.FlightActivity;
import com.expedia.vm.HotelConfirmationViewModel;
import com.google.gson.Gson;

import rx.observers.TestSubscriber;

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

		vm.getAddToCalendarBtnObserver(getContext()).onNext(null);

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
		vm.getDirectionsToHotelBtnObserver(getContext()).onNext(null);

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
		Gson gson = CarServices.generateGson();
		CarSearchParam expectedSearchParams = (CarSearchParam) new CarSearchParam.Builder()
			.origin(CarDataUtils.getSuggestionFromLocation(hotelAddress, null, hotelAddress))
			.startDate(checkInDate).endDate(checkOutDate).build();
		vm.getAddCarBtnObserver(getContext()).onNext(null);
		Intent intent = shadowApplication.getNextStartedActivity();

		assertEquals(gson.toJson(expectedSearchParams), intent.getStringExtra("carSearchParams"));
		assertTrue(intent.getBooleanExtra(Codes.EXTRA_OPEN_SEARCH, false));
	}

	@Test
	public void getAddFlightBtnObserver() {
		givenHotelLocation();
		givenCheckInDate();
		givenCheckOutDate();

		vm.getAddFlightBtnObserver(getContext()).onNext(null);
		Intent intent = shadowApplication.getNextStartedActivity();

		assertEquals(FlightActivity.class.getName(), intent.getComponent().getClassName());
	}

	@Test
	public void lxCrossSellButtonClickTakeUsToLXLOB() {
		givenHotelLocation();
		givenCheckInDate();
		givenCheckOutDate();

		vm.getAddLXBtnObserver(getContext()).onNext(null);
		Intent intent = shadowApplication.getNextStartedActivity();

		assertEquals(LXBaseActivity.class.getName(), intent.getComponent().getClassName());
		assertTrue(intent.getBooleanExtra(Codes.EXTRA_OPEN_RESULTS, false));
	}

	@Test
	public void lxCrossSellButtonText() {
		hotelCity = "San francisco";
		givenHotelLocation();
		givenCheckInDate();
		givenCheckOutDate();

		TestSubscriber testSubscriber = new TestSubscriber<>();
		Db.getAbacusResponse().updateABTestForDebug(AbacusUtils.EBAndroidAppLXCrossSellOnHotelConfirmationTest.getKey(),
			AbacusUtils.DefaultVariant.BUCKETED.ordinal());
		vm.getAddLXBtn().subscribe(testSubscriber);
		vm.getAddLXBtn().onNext(getContext().getResources().getString(R.string.add_lx_TEMPLATE, hotelCity));

		testSubscriber.assertValueCount(1);
		testSubscriber.assertNoTerminalEvent();
		assertEquals(testSubscriber.getOnNextEvents().get(0),
			"Find activities while in San francisco");
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
